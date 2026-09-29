package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.PerformanceMetric;
import jakarta.ejb.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.*;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class PerformanceService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public void record(String component, String operation, long durationMs, boolean success) {
        PerformanceMetric metric = new PerformanceMetric();
        metric.setComponentName(component);
        metric.setOperationName(operation);
        metric.setDurationMs(Math.max(0, durationMs));
        metric.setSuccess(success);
        em.persist(metric);
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<PerformanceMetric> recent(int limit) {
        return em.createQuery("select p from PerformanceMetric p order by p.recordedAt desc", PerformanceMetric.class)
                .setMaxResults(Math.max(1, Math.min(limit, 100)))
                .getResultList();
    }
}
