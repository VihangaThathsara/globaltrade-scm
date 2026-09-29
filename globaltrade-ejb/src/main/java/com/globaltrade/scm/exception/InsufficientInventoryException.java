package com.globaltrade.scm.exception;
import jakarta.ejb.ApplicationException;
@ApplicationException(rollback = true)
public class InsufficientInventoryException extends SupplyChainException {
    public InsufficientInventoryException(String message){ super(message); }
}
