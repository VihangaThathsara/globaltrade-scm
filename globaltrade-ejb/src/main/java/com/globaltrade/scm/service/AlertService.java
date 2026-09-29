package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.Alert;
import com.globaltrade.scm.enums.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class AlertService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Alert create(AlertType type, AlertSeverity severity, String message, String entityRef) {
        List<Alert> existing = em.createQuery("select a from Alert a where a.resolved=false and a.type=:type and a.entityRef=:ref order by a.createdAt desc", Alert.class)
                .setParameter("type", type).setParameter("ref", entityRef).setMaxResults(1).getResultList();
        if (!existing.isEmpty()) {
            Alert alert = existing.get(0);
            alert.setSeverity(severity);
            alert.setMessage(message);
            return alert;
        }
        Alert alert = new Alert();
        alert.setType(type); alert.setSeverity(severity); alert.setMessage(message); alert.setEntityRef(entityRef);
        em.persist(alert);
        em.flush();
        return alert;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void resolveByTypeAndEntity(AlertType type, String entityRef) {
        List<Alert> open = em.createQuery("select a from Alert a where a.resolved=false and a.type=:type and a.entityRef=:ref", Alert.class)
                .setParameter("type", type).setParameter("ref", entityRef).getResultList();
        LocalDateTime now = LocalDateTime.now();
        for (Alert alert : open) {
            alert.setResolved(true);
            alert.setResolvedAt(now);
        }
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS, SecurityRoles.VENDOR})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Alert> findOpen() {
        return em.createQuery("select a from Alert a where a.resolved=false order by a.createdAt desc", Alert.class).getResultList();
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS, SecurityRoles.WAREHOUSE, SecurityRoles.CUSTOMS})
    public void resolve(long id) {
        Alert alert = em.find(Alert.class, id);
        if (alert != null && !alert.isResolved()) {
            alert.setResolved(true);
            alert.setResolvedAt(LocalDateTime.now());
        }
    }
}
