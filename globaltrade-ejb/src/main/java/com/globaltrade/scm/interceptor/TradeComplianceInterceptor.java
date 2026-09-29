package com.globaltrade.scm.interceptor;

import com.globaltrade.scm.exception.CustomsComplianceException;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public class TradeComplianceInterceptor {

    @AroundInvoke
    public Object check(InvocationContext context) throws Exception {
        if ("updateStatus".equals(context.getMethod().getName())) {
            Object[] args = context.getParameters();
            if (args.length < 2 || !(args[0] instanceof Number) || args[1] == null) {
                throw new CustomsComplianceException("A valid customs record and clearance status are required");
            }
        }
        return context.proceed();
    }
}
