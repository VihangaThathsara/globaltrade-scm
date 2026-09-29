package com.globaltrade.scm.service;

import com.globaltrade.scm.api.BusinessFault;
import com.globaltrade.scm.api.OperationsFacadeLocal;
import com.globaltrade.scm.entity.*;
import com.globaltrade.scm.enums.*;
import com.globaltrade.scm.exception.*;
import jakarta.ejb.*;

import java.time.LocalDateTime;
import java.util.*;

@Stateless
public class OperationsFacade implements OperationsFacadeLocal {
    @EJB private DashboardService dashboardService;
    @EJB private ShipmentService shipmentService;
    @EJB private InventoryService inventoryService;
    @EJB private VendorService vendorService;
    @EJB private CustomsService customsService;
    @EJB private CustomsBatchService customsBatchService;
    @EJB private AlertService alertService;
    @EJB private AuditService auditService;
    @EJB private AutomationRunService automationRunService;
    @EJB private PerformanceService performanceService;

    @Override
    public Map<String,Object> dashboard() throws BusinessFault {
        try {
            Map<String,Object> data = new LinkedHashMap<>(dashboardService.getSnapshot());
            data.put("recentShipments", dashboardService.recentShipments(6).stream().map(this::shipmentMap).toList());
            data.put("alerts", alertService.findOpen().stream().limit(6).map(this::alertMap).toList());
            return data;
        } catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public List<Map<String,Object>> shipments() throws BusinessFault {
        try { return shipmentService.findAll().stream().map(this::shipmentMap).toList(); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> shipment(long id) throws BusinessFault {
        try { return shipmentMap(shipmentService.findRequired(id)); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> createShipment(Map<String,Object> body) throws BusinessFault {
        try {
            String productName = text(body,"productName");
            int quantity = (int) number(body.get("quantity"));
            LocalDateTime eta = blank(body.get("eta")) ? null : LocalDateTime.parse(String.valueOf(body.get("eta")));
            return shipmentMap(shipmentService.create(
                    productName,
                    text(body,"carrier"),
                    text(body,"origin"),
                    text(body,"destination"),
                    eta,
                    quantity));
        } catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> updateShipmentStatus(long id, String status) throws BusinessFault {
        try { return shipmentMap(shipmentService.updateStatus(id, ShipmentStatus.valueOf(status.toUpperCase(Locale.ROOT)))); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> cancelShipment(long id) throws BusinessFault {
        try { return shipmentMap(shipmentService.cancel(id)); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public List<Map<String,Object>> inventory() throws BusinessFault {
        try { return inventoryService.findAllForStockState().stream().map(this::inventoryMap).toList(); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public List<Map<String,Object>> warehouses() throws BusinessFault {
        try { return inventoryService.findWarehouses().stream().map(this::warehouseMap).toList(); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> createInventory(Map<String,Object> body) throws BusinessFault {
        try {
            InventoryItem created = inventoryService.create(
                    text(body,"sku"), text(body,"itemName"),
                    (int)number(body.get("quantity")), (int)number(body.get("reorderLevel")),
                    number(body.get("warehouseId")), number(body.get("vendorId")));
            return inventoryMap(created);
        } catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> adjustInventory(long id, int delta) throws BusinessFault {
        try { return inventoryMap(inventoryService.adjust(id, delta)); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public List<Map<String,Object>> vendors() throws BusinessFault {
        try { return vendorService.findAll().stream().map(this::vendorMap).toList(); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> createVendor(Map<String,Object> body) throws BusinessFault {
        try { return vendorMap(vendorService.create(text(body,"vendorCode"), text(body,"name"), text(body,"country"), text(body,"contactEmail"))); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public List<Map<String,Object>> vendorReviews(long id) throws BusinessFault {
        try { return vendorService.reviewHistory(id).stream().map(this::vendorReviewMap).toList(); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> deactivateVendor(long id) throws BusinessFault {
        try { return vendorMap(vendorService.deactivate(id)); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> reactivateVendor(long id) throws BusinessFault {
        try { return vendorMap(vendorService.reactivate(id)); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> reviewInventorySupply(long inventoryItemId, double ratingStars, String notes) throws BusinessFault {
        try { return vendorMap(vendorService.reviewSupply(inventoryItemId, ratingStars, notes)); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public List<Map<String,Object>> customs() throws BusinessFault {
        try { return customsService.findAll().stream().map(this::customsMap).toList(); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> updateCustomsStatus(long id, String status) throws BusinessFault {
        try { return customsMap(customsService.updateStatus(id, CustomsStatus.valueOf(status.toUpperCase(Locale.ROOT)))); }
        catch (SupplyChainException ex) { throw fault(ex); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public int reviewCustomsDeadlines() throws BusinessFault {
        try { return customsBatchService.reviewDeadlines(); }
        catch (Exception ex) { throw new BusinessFault(500, "REVIEW_FAILED", safeMessage(ex)); }
    }

    @Override public List<Map<String,Object>> alerts() throws BusinessFault {
        try { return alertService.findOpen().stream().map(this::alertMap).toList(); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public void resolveAlert(long id) throws BusinessFault {
        try { alertService.resolve(id); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public List<Map<String,Object>> audit(int limit) throws BusinessFault {
        try { return auditService.recent(limit).stream().map(this::auditMap).toList(); }
        catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    @Override public Map<String,Object> monitor() throws BusinessFault {
        try {
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("automations", automationRunService.recent(20).stream().map(this::automationMap).toList());
            out.put("performance", performanceService.recent(40).stream().map(this::metricMap).toList());
            return out;
        } catch (RuntimeException ex) { throw runtimeFault(ex); }
    }

    private BusinessFault fault(SupplyChainException ex) {
        int status = (ex instanceof ShipmentNotFoundException || ex instanceof VendorNotFoundException) ? 404 : 409;
        return new BusinessFault(status, ex.getClass().getSimpleName(), ex.getMessage());
    }

    private BusinessFault runtimeFault(RuntimeException ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && (cause instanceof EJBException || cause.getClass().getName().contains("TransactionRolledback"))) {
            cause = cause.getCause();
        }
        if (cause instanceof EJBAccessException || cause instanceof SecurityException) return new BusinessFault(403, "ACCESS_DENIED", safeMessage(cause));
        if (cause instanceof IllegalArgumentException) return new BusinessFault(400, "INVALID_REQUEST", safeMessage(cause));
        return new BusinessFault(500, "SYSTEM_ERROR", "The operation could not be completed. Check the Payara server log for details.");
    }

    private String safeMessage(Throwable ex) { return ex.getMessage() == null || ex.getMessage().isBlank() ? ex.getClass().getSimpleName() : ex.getMessage(); }
    private static boolean blank(Object v){ return v == null || String.valueOf(v).isBlank(); }
    private static long number(Object v) {
        if (v == null || String.valueOf(v).isBlank()) throw new IllegalArgumentException("A required numeric value is missing");
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(v));
    }
    private static String text(Map<String,Object> body,String key){Object v=body.get(key);return v==null?"":String.valueOf(v);}

    private Map<String,Object> shipmentMap(Shipment s) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("trackingNumber", s.getTrackingNumber());
        m.put("carrier", s.getCarrier());
        m.put("origin", s.getOrigin());
        m.put("destination", s.getDestination());
        m.put("status", s.getStatus().name());
        m.put("eta", String.valueOf(s.getEta()));
        m.put("routeScore", s.getRouteScore());
        m.put("createdBy", s.getCreatedBy());
        m.put("createdAt", String.valueOf(s.getCreatedAt()));

        int totalQuantity = 0;
        String productName = null;
        Set<String> skus = new LinkedHashSet<>();
        Set<String> warehouses = new LinkedHashSet<>();
        if (s.getItems() != null) {
            for (ShipmentItem line : s.getItems()) {
                totalQuantity += line.getQuantity();
                InventoryItem stock = line.getInventoryItem();
                if (stock != null) {
                    if (productName == null) productName = stock.getItemName();
                    if (stock.getSku() != null) skus.add(stock.getSku());
                    if (stock.getWarehouse() != null && stock.getWarehouse().getName() != null) warehouses.add(stock.getWarehouse().getName());
                }
            }
        }
        m.put("quantity", totalQuantity);
        m.put("itemName", productName);
        m.put("stockRecordCount", skus.size());
        m.put("skus", new ArrayList<>(skus));
        m.put("sku", skus.size() == 1 ? skus.iterator().next() : null);
        m.put("warehouseCount", warehouses.size());
        m.put("warehouseName", warehouses.isEmpty() ? null : warehouses.size() == 1 ? warehouses.iterator().next() : warehouses.size() + " warehouses");

        CustomsStatus clearanceStatus = s.getId() == null ? null : customsService.statusForShipment(s.getId());
        m.put("clearanceStatus", clearanceStatus == null ? null : clearanceStatus.name());
        return m;
    }

    private Map<String,Object> inventoryMap(InventoryItem i) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("sku", i.getSku());
        m.put("itemName", i.getItemName());
        m.put("quantity", i.getQuantity());
        int productMinimum = inventoryService.productMinimum(i.getItemName());
        long productTotal = inventoryService.productTotal(i.getItemName());
        m.put("reorderLevel", productMinimum);
        m.put("productTotal", productTotal);
        m.put("lowStock", productMinimum > 0 && productTotal <= productMinimum);
        m.put("active", i.isActive());
        m.put("version", i.getVersion());
        m.put("createdAt", String.valueOf(i.getCreatedAt()));
        m.put("updatedAt", String.valueOf(i.getUpdatedAt()));
        if (i.getWarehouse() != null) {
            m.put("warehouseId", i.getWarehouse().getId());
            m.put("warehouseName", i.getWarehouse().getName());
        }

        StockReceipt latest = i.getId() == null ? null : inventoryService.latestReceipt(i.getId());
        StockReceipt pending = i.getId() == null ? null : inventoryService.latestPendingReceipt(i.getId());
        StockReceipt receiptContext = pending != null ? pending : latest;
        if (receiptContext != null) {
            m.put("receiptId", receiptContext.getId());
            m.put("lastReceivedQuantity", receiptContext.getQuantityReceived());
            m.put("lastReceivedAt", String.valueOf(receiptContext.getReceivedAt()));
            m.put("receivedBy", receiptContext.getReceivedBy());
            m.put("reviewed", pending == null && receiptContext.isReviewed());
            m.put("reviewPending", pending != null);
            if (receiptContext.getVendor() != null) {
                m.put("vendorId", receiptContext.getVendor().getId());
                m.put("vendorName", receiptContext.getVendor().getName());
                m.put("vendorStatus", receiptContext.getVendor().getStatus().name());
            }
        } else {
            m.put("reviewed", true);
            m.put("reviewPending", false);
        }

        VendorEvaluation review = i.getId() == null ? null : vendorService.reviewForInventory(i.getId());
        if (review != null) {
            m.put("reviewRating", review.getScore()/20.0);
            m.put("reviewScore", review.getScore());
            m.put("reviewedAt", String.valueOf(review.getEvaluatedAt()));
        }
        return m;
    }

    private Map<String,Object> vendorMap(Vendor v){
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("id",v.getId());
        m.put("vendorCode",v.getVendorCode());
        m.put("name",v.getName());
        m.put("country",v.getCountry());
        m.put("contactEmail",v.getContactEmail());
        m.put("performanceScore",v.getPerformanceScore());
        m.put("status",v.getStatus().name());
        long reviewCount = v.getId() == null ? 0L : vendorService.reviewCount(v.getId());
        m.put("reviewCount", reviewCount);
        m.put("averageRating", v.getPerformanceScore() == null ? null : v.getPerformanceScore() / 20.0);
        VendorEvaluation latest = v.getId() == null ? null : vendorService.latestReview(v.getId());
        if(latest!=null){
            m.put("latestReviewNotes",latest.getNotes());
            m.put("latestReviewAt",String.valueOf(latest.getEvaluatedAt()));
            m.put("latestRating", latest.getScore() / 20.0);
            m.put("latestReviewedBy", latest.getReviewedBy());
        } else {
            m.put("latestReviewNotes",null);
            m.put("latestReviewAt",null);
            m.put("latestRating", null);
        }
        return m;
    }

    private Map<String,Object> vendorReviewMap(VendorEvaluation e){
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("id",e.getId());
        m.put("score",e.getScore());
        m.put("rating",e.getScore() / 20.0);
        m.put("notes",e.getNotes());
        m.put("reviewedBy",e.getReviewedBy());
        m.put("supplyQuantity", e.getSupplyQuantity());
        m.put("evaluatedAt",String.valueOf(e.getEvaluatedAt()));
        if(e.getInventoryItem()!=null){
            m.put("inventoryItemId", e.getInventoryItem().getId());
            m.put("itemName", e.getInventoryItem().getItemName());
            m.put("sku", e.getInventoryItem().getSku());
        }
        if (e.getStockReceipt() != null) m.put("receiptId", e.getStockReceipt().getId());
        return m;
    }

    private Map<String,Object> warehouseMap(Warehouse w){Map<String,Object>m=new LinkedHashMap<>();m.put("id",w.getId());m.put("code",w.getWarehouseCode());m.put("name",w.getName());m.put("country",w.getCountry());return m;}
    private Map<String,Object> customsMap(CustomsDocument c){Map<String,Object>m=new LinkedHashMap<>();m.put("id",c.getId());m.put("type",c.getType());m.put("referenceNo",c.getReferenceNo());m.put("status",c.getStatus().name());m.put("deadline",String.valueOf(c.getDeadline()));m.put("submittedAt",c.getSubmittedAt()==null?null:String.valueOf(c.getSubmittedAt()));if(c.getShipment()!=null){m.put("shipmentId",c.getShipment().getId());m.put("trackingNumber",c.getShipment().getTrackingNumber());m.put("route",c.getShipment().getOrigin()+" → "+c.getShipment().getDestination());}return m;}

    private Map<String,Object> alertMap(Alert a){
        Map<String,Object>m=new LinkedHashMap<>();
        m.put("id",a.getId());m.put("type",a.getType().name());m.put("severity",a.getSeverity().name());m.put("message",a.getMessage());m.put("entityRef",a.getEntityRef());m.put("resolved",a.isResolved());m.put("createdAt",String.valueOf(a.getCreatedAt()));
        switch(a.getType()){
            case LOW_STOCK -> {m.put("title","Low stock");m.put("source","Inventory monitoring");}
            case SHIPMENT_DELAY -> {m.put("title","Shipment delayed");m.put("source","Shipment monitoring");}
            case CUSTOMS_DEADLINE -> {m.put("title","Customs deadline");m.put("source","Trade clearance");}
            case CUSTOMS_REJECTION -> {m.put("title","Clearance rejected");m.put("source","Trade clearance");}
            case VENDOR_PERFORMANCE -> {m.put("title","Partner performance");m.put("source","Partner monitoring");}
            case ROUTE_RISK -> {m.put("title","Route attention");m.put("source","Route monitoring");}
            default -> {m.put("title","System notice");m.put("source","System monitoring");}
        }
        return m;
    }

    private Map<String,Object> auditMap(AuditLog a){Map<String,Object>m=new LinkedHashMap<>();m.put("id",a.getId());m.put("username",a.getUsername());m.put("operation",a.getOperation());m.put("component",a.getComponentName());m.put("success",a.isSuccess());m.put("detail",a.getDetailMessage());m.put("createdAt",String.valueOf(a.getCreatedAt()));return m;}
    private Map<String,Object> metricMap(PerformanceMetric p){Map<String,Object>m=new LinkedHashMap<>();m.put("id",p.getId());m.put("component",p.getComponentName());m.put("operation",p.getOperationName());m.put("durationMs",p.getDurationMs());m.put("success",p.isSuccess());m.put("recordedAt",String.valueOf(p.getRecordedAt()));return m;}
    private Map<String,Object> automationMap(AutomationRun a){Map<String,Object>m=new LinkedHashMap<>();m.put("id",a.getId());m.put("name",a.getAutomationName());m.put("status",a.getStatus());m.put("durationMs",a.getDurationMs());m.put("message",a.getMessage());m.put("executedAt",String.valueOf(a.getExecutedAt()));return m;}
}
