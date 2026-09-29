package com.globaltrade.scm.entity;

import com.globaltrade.scm.enums.VendorStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VendorDefaultStateTest {
    @Test
    void newVendorStartsActiveAndUnreviewed() {
        Vendor vendor = new Vendor();
        assertNull(vendor.getPerformanceScore());
        assertEquals(VendorStatus.ACTIVE, vendor.getStatus());
    }
}
