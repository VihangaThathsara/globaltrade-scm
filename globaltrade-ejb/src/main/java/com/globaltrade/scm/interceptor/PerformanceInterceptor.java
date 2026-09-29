package com.globaltrade.scm.interceptor;

import com.globaltrade.scm.service.PerformanceService;
import jakarta.ejb.EJB;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;

public class PerformanceInterceptor {
    @EJB
    private PerformanceService performanceService;

    @AroundInvoke
    public Object measure(InvocationContext context) throws Exception {
        return measureContext(context);
    }

    @AroundTimeout
    public Object measureTimeout(InvocationContext context) throws Exception {
        return measureContext(context);
    }

    private Object measureContext(InvocationContext context) throws Exception {
        long start = System.nanoTime();
        boolean success = false;
        try {
            Object result = context.proceed();
            success = true;
            return result;
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            performanceService.record(
                    context.getTarget().getClass().getSimpleName(),
                    context.getMethod().getName(),
                    elapsedMs,
                    success
            );
        }
    }
}
