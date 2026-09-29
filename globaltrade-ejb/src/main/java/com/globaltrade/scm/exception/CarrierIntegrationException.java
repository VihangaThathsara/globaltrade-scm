package com.globaltrade.scm.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CarrierIntegrationException extends SupplyChainException {
    public CarrierIntegrationException(String message){ super(message); }
}
