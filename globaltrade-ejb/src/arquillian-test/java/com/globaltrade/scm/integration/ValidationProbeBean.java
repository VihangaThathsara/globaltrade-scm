package com.globaltrade.scm.integration;

import com.globaltrade.scm.interceptor.VendorValidationInterceptor;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;

@Stateless
@Interceptors(VendorValidationInterceptor.class)
public class ValidationProbeBean {
    public String create(String code, String name, String country, String email) {
        return code;
    }

    public String reviewSupply(long inventoryItemId, double ratingStars, String notes) {
        return ratingStars + "/5";
    }
}
