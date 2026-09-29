package com.globaltrade.scm.integration;

import com.globaltrade.scm.exception.CarrierIntegrationException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.util.Locale;

@Stateless
public class CarrierIntegrationService {

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String confirmBookingWindow(String carrier, String origin, String destination)
            throws CarrierIntegrationException {
        String carrierName = required(carrier, "Carrier");
        required(origin, "Origin");
        required(destination, "Destination");
        if ("SIMULATED_OFFLINE".equalsIgnoreCase(carrierName)) {
            throw new CarrierIntegrationException(
                    "Carrier service is temporarily unavailable. No inventory was reserved; retry the shipment when the carrier is available.");
        }

        String normalized = carrierName.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (normalized.isBlank()) normalized = "CARRIER";
        return "CAR-" + normalized;
    }

    private String required(String value, String label) throws CarrierIntegrationException {
        if (value == null || value.isBlank()) {
            throw new CarrierIntegrationException(label + " is required for carrier integration");
        }
        return value.trim();
    }
}
