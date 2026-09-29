package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.AuditLog;
import jakarta.ejb.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.*;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AuditService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public void record(String username, String component, String operation, boolean success, String detail) {
        AuditLog log = new AuditLog();
        log.setUsername(username == null || username.isBlank() ? "SYSTEM" : username);
        log.setComponentName(component);
        log.setOperation(operation);
        log.setSuccess(success);
        log.setDetailMessage(detail != null && detail.length() > 500 ? detail.substring(0, 500) : detail);
        em.persist(log);
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<AuditLog> recent(int limit) {
        return em.createQuery("select a from AuditLog a order by a.createdAt desc", AuditLog.class)
                .setMaxResults(Math.max(1, Math.min(limit, 200)))
                .getResultList();
    }
}
