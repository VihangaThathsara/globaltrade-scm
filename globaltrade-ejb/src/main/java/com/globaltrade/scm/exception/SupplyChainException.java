package com.globaltrade.scm.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = false, inherited = true)
public class SupplyChainException extends Exception {
    public SupplyChainException(String message) { super(message); }
}
