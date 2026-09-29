package com.globaltrade.scm.timer;

import com.globaltrade.scm.entity.*;
import com.globaltrade.scm.enums.*;
import com.globaltrade.scm.interceptor.PerformanceInterceptor;
import com.globaltrade.scm.service.*;
import jakarta.annotation.Resource;
import jakarta.ejb.*;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Singleton
@Startup
@Interceptors(PerformanceInterceptor.class)
public class SupplyChainAutomationBean {
    @PersistenceContext(unitName = "GlobalTradePU") private EntityManager em;
    @EJB private AlertService alertService;
    @EJB private AutomationRunService automationRunService;
    @Resource private TimerService timerService;

    @Schedule(hour = "*", minute = "*", second = "0", persistent = true)
    public void stockGuard() {
        long start = System.nanoTime();
        try {
            List<Object[]> products = em.createQuery(
                    "select lower(i.itemName), min(i.itemName), coalesce(sum(i.quantity),0), coalesce(max(i.reorderLevel),0) " +
                            "from InventoryItem i group by lower(i.itemName)", Object[].class).getResultList();
            int lowCount = 0;
            for (Object[] row : products) {
                String product = String.valueOf(row[1]);
                long total = ((Number) row[2]).longValue();
                int minimum = ((Number) row[3]).intValue();
                String normalized = product.trim().toUpperCase().replaceAll("[^A-Z0-9]+", "-").replaceAll("^-|-$", "");
                if (normalized.length() > 60) normalized = normalized.substring(0, 60);
                String ref = "PROD-" + (normalized.isBlank() ? "PRODUCT" : normalized);
                if (minimum > 0 && total <= minimum) {
                    lowCount++;
                    long target = InventoryReplenishmentPolicy.targetStock(minimum);
                    long suggested = InventoryReplenishmentPolicy.suggestedQuantity(total, minimum);
                    alertService.create(AlertType.LOW_STOCK, AlertSeverity.WARNING,
                            product + " has " + total + " units available across GlobalTrade inventory; preferred minimum is " + minimum
                                    + ". Replenishment check recommends adding " + suggested + " units to restore the target level of " + target + ".", ref);
                } else {
                    alertService.resolveByTypeAndEntity(AlertType.LOW_STOCK, ref);
                }
            }
            record("Stock Guard", "HEALTHY", start, lowCount + " product(s) require replenishment attention");
        } catch (Exception ex) { record("Stock Guard", "DEGRADED", start, ex.getMessage()); }
    }

    @Schedule(hour = "*", minute = "*", second = "0,15,30,45", persistent = true)
    public void shipmentWatch() {
        long start = System.nanoTime();
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Shipment> delayed = em.createQuery("select s from Shipment s where s.status in (:ready,:transit) and s.eta < :now", Shipment.class)
                    .setParameter("ready", ShipmentStatus.READY).setParameter("transit", ShipmentStatus.IN_TRANSIT).setParameter("now", now).getResultList();
            for (Shipment shipment : delayed) {
                shipment.setStatus(ShipmentStatus.DELAYED);
                alertService.create(AlertType.SHIPMENT_DELAY, AlertSeverity.CRITICAL,
                        "Shipment " + shipment.getTrackingNumber() + " is delayed because its expected arrival time has passed", "SHP-" + shipment.getId());
            }
            record("Shipment Watch", "HEALTHY", start, delayed.size() + " delayed shipment(s) detected");
        } catch (Exception ex) { record("Shipment Watch", "DEGRADED", start, ex.getMessage()); }
    }

    @Schedule(hour = "*", minute = "*", second = "20", persistent = true)
    public void partnerPulse() {
        long start = System.nanoTime();
        try {
            List<Vendor> vendors = em.createQuery("select v from Vendor v where v.performanceScore is not null and v.performanceScore < 70 and v.status <> :inactive", Vendor.class)
                    .setParameter("inactive", VendorStatus.INACTIVE).getResultList();
            for (Vendor vendor : vendors) {
                double stars = vendor.getPerformanceScore() / 20.0;
                alertService.create(AlertType.VENDOR_PERFORMANCE, AlertSeverity.WARNING,
                        vendor.getName() + " needs attention: average partner rating is " + formatStars(stars) + "/5 (" + Math.round(vendor.getPerformanceScore()) + "%)",
                        vendor.getVendorCode());
            }
            record("Partner Pulse", "HEALTHY", start, vendors.size() + " partner(s) below the performance threshold");
        } catch (Exception ex) { record("Partner Pulse", "DEGRADED", start, ex.getMessage()); }
    }

    @Schedule(hour = "*", minute = "*", second = "40", persistent = true)
    public void routeHealthRefresh() {
        long start = System.nanoTime();
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Shipment> active = em.createQuery("select s from Shipment s where s.status not in (:delivered,:cancelled)", Shipment.class)
                    .setParameter("delivered", ShipmentStatus.DELIVERED).setParameter("cancelled", ShipmentStatus.CANCELLED).getResultList();
            int risky = 0;
            for (Shipment shipment : active) {
                int score = RouteRiskPolicy.calculateScore(shipment.getStatus(), shipment.getEta(), now);
                shipment.setRouteScore(score);
                String ref = "ROUTE-" + shipment.getId();
                if (RouteRiskPolicy.needsAttention(score)) {
                    risky++;
                    alertService.create(AlertType.ROUTE_RISK, AlertSeverity.WARNING,
                            "Shipment " + shipment.getTrackingNumber() + " has a route health score of " + score
                                    + "%. Route optimization check recommends: "
                                    + RouteRiskPolicy.recommendedAction(score, shipment.getStatus()) + ".", ref);
                } else {
                    alertService.resolveByTypeAndEntity(AlertType.ROUTE_RISK, ref);
                }
            }
            record("Route Health", "HEALTHY", start, active.size() + " active route(s) checked; " + risky + " need attention");
        } catch (Exception ex) { record("Route Health", "DEGRADED", start, ex.getMessage()); }
    }

    @Lock(LockType.READ)
    public void scheduleCustomsReminder(Long documentId, LocalDateTime deadline) {
        LocalDateTime fireAt = deadline;
        if (fireAt == null || fireAt.isBefore(LocalDateTime.now().plusSeconds(15))) fireAt = LocalDateTime.now().plusSeconds(30);
        Date date = Date.from(fireAt.atZone(ZoneId.systemDefault()).toInstant());
        timerService.createSingleActionTimer(date, new TimerConfig(documentId, true));
    }

    @Timeout
    public void customsDeadlineReminder(Timer timer) {
        long start = System.nanoTime();
        try {
            Object info = timer.getInfo();
            if (!(info instanceof Long documentId)) { record("Customs Deadline", "DEGRADED", start, "Reminder did not contain a document reference"); return; }
            CustomsDocument doc = em.find(CustomsDocument.class, documentId);
            if (doc != null && (doc.getStatus() == CustomsStatus.PENDING || doc.getStatus() == CustomsStatus.SUBMITTED)) {
                alertService.create(AlertType.CUSTOMS_DEADLINE, AlertSeverity.CRITICAL,
                        "Customs clearance for shipment " + doc.getShipment().getTrackingNumber() + " is still " + doc.getStatus().name().toLowerCase() + " and its action deadline is due",
                        "CUS-" + doc.getId());
                record("Customs Deadline", "HEALTHY", start, "Reminder issued for " + doc.getReferenceNo());
            } else record("Customs Deadline", "HEALTHY", start, "No action required");
        } catch (Exception ex) { record("Customs Deadline", "DEGRADED", start, ex.getMessage()); }
    }

    private String formatStars(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        return rounded == Math.rint(rounded) ? String.valueOf((int) rounded) : String.valueOf(rounded);
    }

    private void record(String name, String status, long startNanos, String message) {
        long duration = (System.nanoTime() - startNanos) / 1_000_000L;
        automationRunService.record(name, status, duration, message == null ? "" : message);
    }
}
