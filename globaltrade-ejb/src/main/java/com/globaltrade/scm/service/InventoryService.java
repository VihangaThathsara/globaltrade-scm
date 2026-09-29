package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.*;
import com.globaltrade.scm.exception.InsufficientInventoryException;
import com.globaltrade.scm.enums.*;
import com.globaltrade.scm.interceptor.*;
import jakarta.annotation.Resource;
import jakarta.annotation.security.*;
import jakarta.ejb.*;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Stateless
@DeclareRoles({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
@Interceptors(PerformanceInterceptor.class)
public class InventoryService {
    @PersistenceContext(unitName="GlobalTradePU") private EntityManager em;
    @EJB private AlertService alertService;
    @Resource private SessionContext sessionContext;

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.WAREHOUSE})
    @Interceptors(AuditInterceptor.class)
    public InventoryItem create(String sku, String name, int quantity, int reorderLevel, long warehouseId, long vendorId) {
        Warehouse warehouse = em.find(Warehouse.class, warehouseId);
        Vendor supplier = em.find(Vendor.class, vendorId);
        if (warehouse == null) throw new IllegalArgumentException("Select a warehouse");
        if (supplier == null) throw new IllegalArgumentException("Select the supplier that delivered this stock");
        if (supplier.getStatus() != VendorStatus.ACTIVE) throw new IllegalArgumentException("Only active partners can supply new stock");
        if (quantity <= 0) throw new IllegalArgumentException("Received quantity must be greater than zero");
        if (reorderLevel < 0) throw new IllegalArgumentException("Preferred minimum cannot be negative");

        String normalizedSku = sku == null ? "" : sku.trim().toUpperCase(Locale.ROOT);
        String cleanedName = name == null ? "" : name.trim();
        if (normalizedSku.isBlank()) throw new IllegalArgumentException("SKU is required");
        if (cleanedName.isBlank()) throw new IllegalArgumentException("Item name is required");

        Long duplicateSku = em.createQuery("select count(i) from InventoryItem i where upper(i.sku)=:sku", Long.class)
                .setParameter("sku", normalizedSku).getSingleResult();
        if (duplicateSku != null && duplicateSku > 0) {
            throw new IllegalArgumentException("SKU " + normalizedSku + " is already in use. Use a new SKU for this stock receipt.");
        }

        List<InventoryItem> sameProduct = em.createQuery(
                        "select i from InventoryItem i where lower(i.itemName)=:name",
                        InventoryItem.class)
                .setParameter("name", cleanedName.toLowerCase(Locale.ROOT))
                .getResultList();
        int productMinimum = sameProduct.isEmpty() ? reorderLevel : sameProduct.stream().mapToInt(InventoryItem::getReorderLevel).max().orElse(reorderLevel);

        InventoryItem item = new InventoryItem();
        item.setSku(normalizedSku);
        item.setItemName(cleanedName);
        item.setQuantity(quantity);
        item.setReorderLevel(productMinimum);
        item.setWarehouse(warehouse);
        item.setActive(quantity > 0);
        em.persist(item);
        em.flush();

        StockReceipt receipt = new StockReceipt();
        receipt.setInventoryItem(item);
        receipt.setVendor(supplier);
        receipt.setQuantityReceived(quantity);
        receipt.setReceivedAt(LocalDateTime.now());
        receipt.setReceivedBy(sessionContext != null && sessionContext.getCallerPrincipal() != null
                ? sessionContext.getCallerPrincipal().getName() : "SYSTEM");
        receipt.setReviewed(false);
        em.persist(receipt);
        em.flush();

        syncProductStockAlert(cleanedName);
        return item;
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<InventoryItem> findAll() {
        return em.createQuery(
                "select i from InventoryItem i join fetch i.warehouse order by i.createdAt desc, i.id desc",
                InventoryItem.class).getResultList();
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<InventoryItem> findAllForStockState() {
        return em.createQuery(
                "select i from InventoryItem i join fetch i.warehouse order by i.createdAt desc, i.id desc",
                InventoryItem.class).getResultList();
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public StockReceipt latestReceipt(long inventoryItemId) {
        List<StockReceipt> rows = em.createQuery(
                        "select r from StockReceipt r join fetch r.vendor where r.inventoryItem.id=:id order by r.receivedAt desc, r.id desc",
                        StockReceipt.class)
                .setParameter("id", inventoryItemId).setMaxResults(1).getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public StockReceipt latestPendingReceipt(long inventoryItemId) {
        List<StockReceipt> rows = em.createQuery(
                        "select r from StockReceipt r join fetch r.vendor where r.inventoryItem.id=:id and r.reviewed=false order by r.receivedAt desc, r.id desc",
                        StockReceipt.class)
                .setParameter("id", inventoryItemId).setMaxResults(1).getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.WAREHOUSE})
    @Interceptors(AuditInterceptor.class)
    public InventoryItem adjust(long id, int delta) throws InsufficientInventoryException {
        if (delta == 0) throw new IllegalArgumentException("Enter a stock adjustment greater than zero");
        InventoryItem item = em.find(InventoryItem.class, id, LockModeType.OPTIMISTIC);
        if (item == null) throw new IllegalArgumentException("Inventory item was not found");
        int next = item.getQuantity() + delta;
        if (delta < 0 && next < 0) {
            int requestedRemoval = Math.abs(delta);
            throw new InsufficientInventoryException(
                    "Cannot remove " + requestedRemoval + " units from " + item.getSku()
                            + ". Only " + item.getQuantity() + " units are currently available.");
        }
        item.setQuantity(next);
        item.setActive(next > 0);

        StockReceipt receipt = latestReceipt(id);
        if (receipt != null) {
            receipt.setQuantityReceived(receipt.getQuantityReceived() + delta);
        }

        syncProductStockAlert(item.getItemName());
        return item;
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public InventoryItem reserveStock(long id, int requested) throws InsufficientInventoryException {
        if (requested <= 0) throw new IllegalArgumentException("Shipment quantity must be greater than zero");
        InventoryItem item = em.find(InventoryItem.class, id, LockModeType.OPTIMISTIC);
        if (item == null) throw new IllegalArgumentException("Inventory item was not found");
        if (item.getQuantity() <= 0) throw new InsufficientInventoryException(item.getItemName() + " stock record " + item.getSku() + " is empty");
        if (item.getQuantity() < requested) {
            throw new InsufficientInventoryException("Only " + item.getQuantity() + " units are available in stock record " + item.getSku());
        }
        int next = item.getQuantity() - requested;
        item.setQuantity(next);
        item.setActive(next > 0);
        syncProductStockAlert(item.getItemName());
        return item;
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void releaseStock(long id, int quantity) {
        if (quantity <= 0) return;
        InventoryItem item = em.find(InventoryItem.class, id, LockModeType.OPTIMISTIC);
        if (item == null) throw new IllegalArgumentException("Inventory item was not found");
        item.setQuantity(item.getQuantity() + quantity);
        item.setActive(true);
        syncProductStockAlert(item.getItemName());
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public long productTotal(String itemName) {
        if (itemName == null || itemName.isBlank()) return 0L;
        Object result = em.createQuery(
                        "select coalesce(sum(i.quantity),0) from InventoryItem i where lower(i.itemName)=:name")
                .setParameter("name", itemName.trim().toLowerCase(Locale.ROOT))
                .getSingleResult();
        return result instanceof Number number ? number.longValue() : 0L;
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public int productMinimum(String itemName) {
        if (itemName == null || itemName.isBlank()) return 0;
        Object result = em.createQuery(
                        "select max(i.reorderLevel) from InventoryItem i where lower(i.itemName)=:name")
                .setParameter("name", itemName.trim().toLowerCase(Locale.ROOT))
                .getSingleResult();
        return result instanceof Number number ? number.intValue() : 0;
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Object[]> productStockSummarySystem() {
        return em.createQuery(
                        "select lower(i.itemName), min(i.itemName), coalesce(sum(i.quantity),0), coalesce(max(i.reorderLevel),0), count(i) " +
                                "from InventoryItem i group by lower(i.itemName) order by min(i.itemName)",
                        Object[].class)
                .getResultList();
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<InventoryItem> productStockRecordsForShipment(String productName) {
        if (productName == null || productName.isBlank()) return List.of();
        return em.createQuery(
                        "select i from InventoryItem i join fetch i.warehouse where lower(i.itemName)=:name and i.quantity>0 order by i.createdAt asc, i.id asc",
                        InventoryItem.class)
                .setParameter("name", productName.trim().toLowerCase(Locale.ROOT))
                .getResultList();
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Warehouse> findWarehouses() {
        return em.createQuery("select w from Warehouse w order by w.name", Warehouse.class).getResultList();
    }

    private void syncProductStockAlert(String itemName) {
        if (itemName == null || itemName.isBlank()) return;
        long total = productTotal(itemName);
        int minimum = productMinimum(itemName);
        String ref = productAlertRef(itemName);
        if (minimum > 0 && total <= minimum) {
            long target = InventoryReplenishmentPolicy.targetStock(minimum);
            long suggested = InventoryReplenishmentPolicy.suggestedQuantity(total, minimum);
            alertService.create(AlertType.LOW_STOCK, AlertSeverity.WARNING,
                    itemName.trim() + " has " + total + " units available across GlobalTrade inventory; preferred minimum is " + minimum
                            + ". Replenishment check recommends adding " + suggested + " units to restore the target level of " + target + ".",
                    ref);
        } else {
            alertService.resolveByTypeAndEntity(AlertType.LOW_STOCK, ref);
        }
    }

    public String productAlertRef(String itemName) {
        String normalized = itemName == null ? "PRODUCT" : itemName.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-").replaceAll("^-|-$", "");
        if (normalized.isBlank()) normalized = "PRODUCT";
        if (normalized.length() > 60) normalized = normalized.substring(0, 60);
        return "PROD-" + normalized;
    }
}
