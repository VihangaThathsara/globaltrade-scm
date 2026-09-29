package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.*;
import com.globaltrade.scm.enums.*;
import com.globaltrade.scm.exception.*;
import com.globaltrade.scm.interceptor.*;
import com.globaltrade.scm.integration.CarrierIntegrationService;
import jakarta.annotation.Resource;
import jakarta.annotation.security.*;
import jakarta.ejb.*;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Stateless
@DeclareRoles({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
@Interceptors(PerformanceInterceptor.class)
public class ShipmentService {
    @PersistenceContext(unitName="GlobalTradePU") private EntityManager em;
    @EJB private InventoryService inventoryService;
    @EJB private CustomsService customsService;
    @EJB private AlertService alertService;
    @EJB private CarrierIntegrationService carrierIntegrationService;
    @Resource private SessionContext sessionContext;

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS})
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Interceptors(AuditInterceptor.class)
    public Shipment create(String productName, String carrier, String origin, String destination,
                           LocalDateTime eta, int quantity) throws SupplyChainException {
        String product = required(productName, "Product");
        String carrierName = required(carrier, "Carrier");
        String originName = required(origin, "Origin");
        String destinationName = required(destination, "Destination");
        if (eta == null || !eta.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Expected arrival must be a future date and time");
        if (quantity <= 0) throw new IllegalArgumentException("Shipment quantity must be greater than zero");
        carrierIntegrationService.confirmBookingWindow(carrierName, originName, destinationName);

        List<InventoryItem> stockRecords = inventoryService.productStockRecordsForShipment(product);
        long available = stockRecords.stream().mapToLong(InventoryItem::getQuantity).sum();
        if (available < quantity) {
            throw new InsufficientInventoryException("Only " + available + " units of " + product
                    + " are available across GlobalTrade inventory. Requested " + quantity + " units.");
        }

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setCarrier(carrierName);
        shipment.setOrigin(originName);
        shipment.setDestination(destinationName);
        shipment.setEta(eta);
        shipment.setCreatedBy(sessionContext.getCallerPrincipal() == null ? "SYSTEM" : sessionContext.getCallerPrincipal().getName());
        shipment.setRouteScore(90);

        int remaining = quantity;
        for (InventoryItem stock : stockRecords) {
            if (remaining <= 0) break;
            int take = Math.min(stock.getQuantity(), remaining);
            inventoryService.reserveStock(stock.getId(), take);
            ShipmentItem line = new ShipmentItem();
            line.setInventoryItem(em.getReference(InventoryItem.class, stock.getId()));
            line.setQuantity(take);
            shipment.addItem(line);
            remaining -= take;
        }

        if (remaining != 0) throw new InsufficientInventoryException("The requested product quantity could not be fully reserved");

        em.persist(shipment);
        customsService.createForShipment(shipment);
        em.flush();
        return shipment;
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> findAll() {
        return em.createQuery(
                "select distinct s from Shipment s left join fetch s.items si left join fetch si.inventoryItem ii left join fetch ii.warehouse order by s.createdAt desc",
                Shipment.class).getResultList();
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Shipment findRequired(long id) throws ShipmentNotFoundException {
        List<Shipment> result = em.createQuery(
                        "select distinct s from Shipment s left join fetch s.items si left join fetch si.inventoryItem ii left join fetch ii.warehouse where s.id=:id",
                        Shipment.class)
                .setParameter("id", id).getResultList();
        if (result.isEmpty()) throw new ShipmentNotFoundException("Shipment " + id + " was not found");
        return result.get(0);
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS})
    @Interceptors(AuditInterceptor.class)
    public Shipment updateStatus(long id, ShipmentStatus status) throws ShipmentNotFoundException {
        if (status == null) throw new IllegalArgumentException("Status is required");
        if (status == ShipmentStatus.CANCELLED) return cancel(id);
        Shipment shipment = findRequired(id);
        ShipmentStatus current = shipment.getStatus();
        if (current == status) return shipment;
        if (current == ShipmentStatus.CANCELLED) throw new IllegalArgumentException("A cancelled shipment cannot be reopened");
        if (current == ShipmentStatus.DELIVERED) throw new IllegalArgumentException("A delivered shipment cannot be changed");
        if (!isTransitionAllowed(current, status)) throw new IllegalArgumentException("Shipment cannot move from " + current + " to " + status);
        if ((status == ShipmentStatus.IN_TRANSIT || status == ShipmentStatus.DELIVERED) && !customsService.isApprovedForShipment(id)) {
            throw new IllegalArgumentException("Customs clearance must be approved before the shipment can leave the facility");
        }
        shipment.setStatus(status);
        if (status == ShipmentStatus.DELIVERED) {
            alertService.resolveByTypeAndEntity(AlertType.SHIPMENT_DELAY, "SHP-" + shipment.getId());
            alertService.resolveByTypeAndEntity(AlertType.ROUTE_RISK, "ROUTE-" + shipment.getId());
        }
        return shipment;
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS})
    @Interceptors(AuditInterceptor.class)
    public Shipment cancel(long id) throws ShipmentNotFoundException {
        Shipment shipment = findRequired(id);
        if (shipment.getStatus() == ShipmentStatus.CANCELLED) return shipment;
        if (shipment.getStatus() == ShipmentStatus.DELIVERED) throw new IllegalArgumentException("A delivered shipment cannot be cancelled");
        boolean admin = sessionContext.isCallerInRole(SecurityRoles.ADMIN);
        if (!admin && shipment.getStatus() == ShipmentStatus.IN_TRANSIT) throw new SecurityException("Only an administrator can cancel a shipment after dispatch");
        for (ShipmentItem shipmentItem : shipment.getItems()) {
            if (shipmentItem.getInventoryItem() != null) inventoryService.releaseStock(shipmentItem.getInventoryItem().getId(), shipmentItem.getQuantity());
        }
        shipment.setStatus(ShipmentStatus.CANCELLED);
        customsService.cancelForShipment(id);
        alertService.resolveByTypeAndEntity(AlertType.SHIPMENT_DELAY, "SHP-" + shipment.getId());
        alertService.resolveByTypeAndEntity(AlertType.ROUTE_RISK, "ROUTE-" + shipment.getId());
        return shipment;
    }

    @PermitAll @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> findPotentiallyDelayedSystem() {
        return em.createQuery("select s from Shipment s where s.status in (:ready,:transit) and s.eta < :now", Shipment.class)
                .setParameter("ready", ShipmentStatus.READY).setParameter("transit", ShipmentStatus.IN_TRANSIT).setParameter("now", LocalDateTime.now()).getResultList();
    }

    @PermitAll
    public void markDelayedSystem(long id) {
        Shipment shipment = em.find(Shipment.class, id);
        if (shipment != null && shipment.getStatus() != ShipmentStatus.DELIVERED && shipment.getStatus() != ShipmentStatus.CANCELLED) shipment.setStatus(ShipmentStatus.DELAYED);
    }

    @PermitAll
    public int refreshRouteScoresSystem() {
        List<Shipment> active = em.createQuery("select s from Shipment s where s.status not in (:delivered,:cancelled)", Shipment.class)
                .setParameter("delivered", ShipmentStatus.DELIVERED).setParameter("cancelled", ShipmentStatus.CANCELLED).getResultList();
        LocalDateTime now = LocalDateTime.now();
        for (Shipment s : active) {
            int score = RouteRiskPolicy.calculateScore(s.getStatus(), s.getEta(), now);
            s.setRouteScore(score);
            String ref = "ROUTE-" + s.getId();
            if (RouteRiskPolicy.needsAttention(score)) {
                alertService.create(AlertType.ROUTE_RISK, AlertSeverity.WARNING,
                        "Shipment " + s.getTrackingNumber() + " has a route health score of " + score
                                + "%. Route optimization check recommends: "
                                + RouteRiskPolicy.recommendedAction(score, s.getStatus()) + ".", ref);
            } else {
                alertService.resolveByTypeAndEntity(AlertType.ROUTE_RISK, ref);
            }
        }
        return active.size();
    }

    private boolean isTransitionAllowed(ShipmentStatus current, ShipmentStatus next) {
        return switch (current) {
            case CREATED -> next == ShipmentStatus.READY;
            case READY -> next == ShipmentStatus.IN_TRANSIT;
            case IN_TRANSIT -> next == ShipmentStatus.DELIVERED;
            case DELAYED -> next == ShipmentStatus.IN_TRANSIT || next == ShipmentStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required");
        return value.trim();
    }

    private String generateTrackingNumber() {
        return "GT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + "-" + ThreadLocalRandom.current().nextInt(100, 999);
    }
}
