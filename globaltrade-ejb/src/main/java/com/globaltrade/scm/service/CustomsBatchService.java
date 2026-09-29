package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.CustomsDocument;
import com.globaltrade.scm.enums.AlertSeverity;
import com.globaltrade.scm.enums.AlertType;
import com.globaltrade.scm.enums.CustomsStatus;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CustomsBatchService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Resource
    private UserTransaction userTransaction;

    @EJB
    private AlertService alertService;

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.CUSTOMS})
    public int reviewDeadlines() throws Exception {
        userTransaction.begin();
        try {
            List<CustomsDocument> docs = em.createQuery(
                            "select c from CustomsDocument c join fetch c.shipment " +
                                    "where c.status in (:pending,:submitted) and c.deadline < :now order by c.deadline",
                            CustomsDocument.class)
                    .setParameter("pending", CustomsStatus.PENDING)
                    .setParameter("submitted", CustomsStatus.SUBMITTED)
                    .setParameter("now", LocalDateTime.now())
                    .getResultList();

            for (CustomsDocument doc : docs) {
                alertService.create(
                        AlertType.CUSTOMS_DEADLINE,
                        AlertSeverity.CRITICAL,
                        "Customs clearance for shipment " + doc.getShipment().getTrackingNumber() + " is overdue because its action deadline has passed",
                        "CUS-" + doc.getId()
                );
            }

            int count = docs.size();
            userTransaction.commit();
            return count;
        } catch (Exception ex) {
            try {
                userTransaction.rollback();
            } catch (Exception ignored) {

            }
            throw ex;
        }
    }
}
