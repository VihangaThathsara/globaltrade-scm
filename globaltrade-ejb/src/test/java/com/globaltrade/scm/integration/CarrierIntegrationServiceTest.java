package com.globaltrade.scm.integration;

import com.globaltrade.scm.exception.CarrierIntegrationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarrierIntegrationServiceTest {
    private final CarrierIntegrationService service = new CarrierIntegrationService();

    @Test
    void normalCarrierReturnsIntegrationReference() throws Exception {
        String reference = service.confirmBookingWindow("DHL Express", "Colombo", "Singapore");
        assertEquals("CAR-DHL-EXPRESS", reference);
    }

    @Test
    void simulatedCarrierOutageProducesRecoverableApplicationException() {
        CarrierIntegrationException ex = assertThrows(CarrierIntegrationException.class,
                () -> service.confirmBookingWindow("SIMULATED_OFFLINE", "Colombo", "Singapore"));
        assertTrue(ex.getMessage().contains("No inventory was reserved"));
    }
}
