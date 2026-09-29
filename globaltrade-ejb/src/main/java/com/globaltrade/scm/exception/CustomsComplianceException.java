package com.globaltrade.scm.exception;
import jakarta.ejb.ApplicationException;
@ApplicationException(rollback = true)
public class CustomsComplianceException extends SupplyChainException {
    public CustomsComplianceException(String message){ super(message); }
}
