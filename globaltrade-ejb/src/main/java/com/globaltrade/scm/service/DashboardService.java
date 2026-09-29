package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.Shipment;
import com.globaltrade.scm.enums.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.*;
import jakarta.persistence.*;
import java.util.*;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class DashboardService {
    @PersistenceContext(unitName = "GlobalTradePU") private EntityManager em;

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    public Map<String, Object> getSnapshot() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("activeShipments", em.createQuery("select count(s) from Shipment s where s.status not in (:delivered,:cancelled)", Long.class)
                .setParameter("delivered", ShipmentStatus.DELIVERED).setParameter("cancelled", ShipmentStatus.CANCELLED).getSingleResult());
        data.put("delayedShipments", em.createQuery("select count(s) from Shipment s where s.status=:status", Long.class)
                .setParameter("status", ShipmentStatus.DELAYED).getSingleResult());
        List<Object[]> stock = em.createQuery(
                        "select lower(i.itemName), coalesce(sum(i.quantity),0), coalesce(max(i.reorderLevel),0) from InventoryItem i group by lower(i.itemName)",
                        Object[].class).getResultList();
        long lowStockProducts = stock.stream().filter(row -> ((Number) row[2]).intValue() > 0 && ((Number) row[1]).longValue() <= ((Number) row[2]).longValue()).count();
        data.put("lowStock", lowStockProducts);
        data.put("activeVendors", em.createQuery("select count(v) from Vendor v where v.status=:status", Long.class)
                .setParameter("status", VendorStatus.ACTIVE).getSingleResult());
        data.put("pendingCustoms", em.createQuery("select count(c) from CustomsDocument c where c.status in (:pending,:submitted)", Long.class)
                .setParameter("pending", CustomsStatus.PENDING).setParameter("submitted", CustomsStatus.SUBMITTED).getSingleResult());

        Map<String, Long> statuses = new LinkedHashMap<>();
        for (ShipmentStatus status : ShipmentStatus.values()) {
            statuses.put(status.name(), em.createQuery("select count(s) from Shipment s where s.status=:status", Long.class)
                    .setParameter("status", status).getSingleResult());
        }
        data.put("shipmentStatuses", statuses);
        return data;
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    public List<Shipment> recentShipments(int limit) {
        return em.createQuery("select distinct s from Shipment s left join fetch s.items si left join fetch si.inventoryItem ii left join fetch ii.warehouse order by s.createdAt desc", Shipment.class)
                .setMaxResults(Math.max(1, Math.min(limit, 20))).getResultList();
    }
}
