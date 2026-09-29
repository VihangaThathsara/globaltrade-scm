package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.*;
import com.globaltrade.scm.enums.*;
import com.globaltrade.scm.exception.CustomsComplianceException;
import com.globaltrade.scm.interceptor.*;
import com.globaltrade.scm.timer.SupplyChainAutomationBean;
import jakarta.annotation.Resource;
import jakarta.annotation.security.*;
import jakarta.ejb.*;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@DeclareRoles({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
@Interceptors(PerformanceInterceptor.class)
public class CustomsService {
    @PersistenceContext(unitName="GlobalTradePU") private EntityManager em;
    @EJB private SupplyChainAutomationBean automationBean;
    @EJB private InventoryService inventoryService;
    @EJB private AlertService alertService;
    @Resource private SessionContext sessionContext;

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public CustomsDocument createForShipment(Shipment shipment) {
        CustomsDocument doc = new CustomsDocument();
        doc.setShipment(shipment); doc.setType("Commercial Clearance"); doc.setReferenceNo("CUS-" + shipment.getTrackingNumber());
        LocalDateTime deadline = shipment.getEta().minusHours(18);
        if (deadline.isBefore(LocalDateTime.now().plusMinutes(2))) deadline = LocalDateTime.now().plusMinutes(2);
        doc.setDeadline(deadline);
        em.persist(doc); em.flush(); automationBean.scheduleCustomsReminder(doc.getId(), deadline); return doc;
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<CustomsDocument> findAll() {
        return em.createQuery("select c from CustomsDocument c join fetch c.shipment s order by c.createdAt desc", CustomsDocument.class).getResultList();
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.CUSTOMS})
    @Interceptors({AuditInterceptor.class, TradeComplianceInterceptor.class})
    public CustomsDocument updateStatus(long id, CustomsStatus status) throws CustomsComplianceException {
        CustomsDocument doc = em.find(CustomsDocument.class, id);
        if (doc == null) throw new CustomsComplianceException("Customs document was not found");
        if (status == null || status == CustomsStatus.CANCELLED) throw new CustomsComplianceException("Choose a valid clearance status");
        if (doc.getStatus() == CustomsStatus.CANCELLED || doc.getStatus() == CustomsStatus.APPROVED || doc.getStatus() == CustomsStatus.REJECTED) {
            if (doc.getStatus() != status) throw new CustomsComplianceException("This clearance record is already closed");
            return doc;
        }
        if (doc.getStatus() == CustomsStatus.PENDING && status == CustomsStatus.APPROVED) {
            throw new CustomsComplianceException("Submit the document before approval");
        }
        if (doc.getStatus() == CustomsStatus.SUBMITTED && status == CustomsStatus.PENDING) {
            throw new CustomsComplianceException("A submitted document cannot return to pending");
        }

        Shipment shipment = doc.getShipment();
        if (status == CustomsStatus.REJECTED) {
            if (shipment != null && (shipment.getStatus() == ShipmentStatus.IN_TRANSIT || shipment.getStatus() == ShipmentStatus.DELIVERED)) {
                throw new CustomsComplianceException("Clearance cannot be rejected after the shipment has left the facility");
            }
            if (shipment != null && shipment.getStatus() != ShipmentStatus.CANCELLED) {
                for (ShipmentItem shipmentItem : shipment.getItems()) {
                    if (shipmentItem.getInventoryItem() != null) inventoryService.releaseStock(shipmentItem.getInventoryItem().getId(), shipmentItem.getQuantity());
                }
                shipment.setStatus(ShipmentStatus.CANCELLED);
                alertService.create(AlertType.CUSTOMS_REJECTION, AlertSeverity.CRITICAL,
                        "Shipment " + shipment.getTrackingNumber() + " was cancelled because customs clearance was rejected. Reserved stock was returned to inventory.",
                        "CUS-REJECT-" + doc.getId());
            }
        }

        doc.setStatus(status);
        if (status == CustomsStatus.SUBMITTED && doc.getSubmittedAt() == null) doc.setSubmittedAt(LocalDateTime.now());
        if (status == CustomsStatus.APPROVED || status == CustomsStatus.REJECTED) {
            alertService.resolveByTypeAndEntity(AlertType.CUSTOMS_DEADLINE, "CUS-" + doc.getId());
        }
        return doc;
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean isApprovedForShipment(long shipmentId) {
        Long count = em.createQuery("select count(c) from CustomsDocument c where c.shipment.id=:shipmentId and c.status=:approved", Long.class)
                .setParameter("shipmentId", shipmentId).setParameter("approved", CustomsStatus.APPROVED).getSingleResult();
        return count > 0;
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public CustomsStatus statusForShipment(long shipmentId) {
        List<CustomsStatus> rows = em.createQuery("select c.status from CustomsDocument c where c.shipment.id=:shipmentId", CustomsStatus.class)
                .setParameter("shipmentId", shipmentId).setMaxResults(1).getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void cancelForShipment(long shipmentId) {
        List<CustomsDocument> docs = em.createQuery("select c from CustomsDocument c where c.shipment.id=:shipmentId", CustomsDocument.class)
                .setParameter("shipmentId", shipmentId).getResultList();
        for (CustomsDocument doc : docs) {
            if (doc.getStatus() != CustomsStatus.APPROVED && doc.getStatus() != CustomsStatus.REJECTED) doc.setStatus(CustomsStatus.CANCELLED);
            alertService.resolveByTypeAndEntity(AlertType.CUSTOMS_DEADLINE, "CUS-" + doc.getId());
        }
    }

    @PermitAll
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<CustomsDocument> findUpcomingSystem(LocalDateTime until) {
        return em.createQuery("select c from CustomsDocument c join fetch c.shipment where c.status in (:pending,:submitted) and c.deadline <= :until order by c.deadline", CustomsDocument.class)
                .setParameter("pending", CustomsStatus.PENDING).setParameter("submitted", CustomsStatus.SUBMITTED).setParameter("until", until).getResultList();
    }
}
