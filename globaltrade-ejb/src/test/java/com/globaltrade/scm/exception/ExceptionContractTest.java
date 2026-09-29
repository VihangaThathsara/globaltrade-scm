package com.globaltrade.scm.exception;

import jakarta.ejb.ApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionContractTest {
    @Test
    void insufficientInventoryForcesRollback() {
        ApplicationException annotation = InsufficientInventoryException.class.getAnnotation(ApplicationException.class);
        assertNotNull(annotation);
        assertTrue(annotation.rollback());
    }

    @Test
    void complianceFailureForcesRollback() {
        ApplicationException annotation = CustomsComplianceException.class.getAnnotation(ApplicationException.class);
        assertNotNull(annotation);
        assertTrue(annotation.rollback());
    }

    @Test
    void ordinarySupplyChainFaultDoesNotForceRollback() {
        ApplicationException annotation = SupplyChainException.class.getAnnotation(ApplicationException.class);
        assertNotNull(annotation);
        assertFalse(annotation.rollback());
    }
}
