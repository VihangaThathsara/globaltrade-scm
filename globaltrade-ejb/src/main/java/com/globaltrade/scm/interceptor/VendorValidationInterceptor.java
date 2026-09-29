package com.globaltrade.scm.interceptor;

import com.globaltrade.scm.exception.SupplyChainException;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public class VendorValidationInterceptor {

    @AroundInvoke
    public Object validate(InvocationContext context) throws Exception {
        String operation = context.getMethod().getName();
        Object[] args = context.getParameters();

        if ("create".equals(operation)) {
            requireText(args, 0, "Partner code");
            requireText(args, 1, "Company name");
            requireText(args, 2, "Country");
            validateOptionalEmail(args, 3);
        } else if ("reviewSupply".equals(operation) && args.length > 1 && args[1] instanceof Number number) {
            double rating = number.doubleValue();
            if (rating < 1.0 || rating > 5.0 || Math.abs(rating - Math.rint(rating)) > 0.00001) {
                throw new SupplyChainException("Partner rating must be a whole number from 1 to 5 stars");
            }
        }

        return context.proceed();
    }

    private void requireText(Object[] args, int index, String label) {
        if (args.length <= index || args[index] == null || String.valueOf(args[index]).isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
    }

    private void validateOptionalEmail(Object[] args, int index) {
        if (args.length <= index || args[index] == null) return;
        String email = String.valueOf(args[index]).trim();
        if (email.isBlank()) return;
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        if (at <= 0 || dot <= at + 1 || dot >= email.length() - 1) {
            throw new IllegalArgumentException("Enter a valid operations email address");
        }
    }
}
