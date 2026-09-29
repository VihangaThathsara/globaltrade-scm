package com.globaltrade.scm.service;

import com.globaltrade.scm.entity.AutomationRun;
import jakarta.ejb.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.*;
import java.util.List;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AutomationRunService {
    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public void record(String name, String status, long durationMs, String message) {
        AutomationRun run = new AutomationRun();
        run.setAutomationName(name);
        run.setStatus(status);
        run.setDurationMs(durationMs);
        run.setMessage(message);
        em.persist(run);
    }

    @RolesAllowed({SecurityRoles.ADMIN, SecurityRoles.LOGISTICS})
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<AutomationRun> recent(int limit) {
        return em.createQuery("select a from AutomationRun a order by a.executedAt desc", AutomationRun.class)
                .setMaxResults(Math.max(1, Math.min(limit, 100)))
                .getResultList();
    }
}
