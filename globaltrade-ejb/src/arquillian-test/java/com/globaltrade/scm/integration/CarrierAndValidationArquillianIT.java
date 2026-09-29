package com.globaltrade.scm.integration;

import com.globaltrade.scm.exception.CarrierIntegrationException;
import com.globaltrade.scm.exception.SupplyChainException;
import com.globaltrade.scm.interceptor.VendorValidationInterceptor;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
class CarrierAndValidationArquillianIT {

    @Deployment
    static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class, "globaltrade-arquillian-it.jar")
                .addClasses(
                        CarrierIntegrationService.class,
                        CarrierIntegrationException.class,
                        SupplyChainException.class,
                        VendorValidationInterceptor.class,
                        ValidationProbeBean.class
                )
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private CarrierIntegrationService carrierIntegrationService;

    @EJB
    private ValidationProbeBean validationProbeBean;

    @Test
    void carrierGatewayRunsInsidePayara() throws Exception {
        assertEquals(
                "CAR-DHL-EXPRESS",
                carrierIntegrationService.confirmBookingWindow(
                        "DHL Express",
                        "Colombo",
                        "Singapore"
                )
        );

        assertThrows(
                CarrierIntegrationException.class,
                () -> carrierIntegrationService.confirmBookingWindow(
                        "SIMULATED_OFFLINE",
                        "Colombo",
                        "Singapore"
                )
        );
    }

    @Test
    void vendorValidationInterceptorRunsInsidePayara() throws Exception {
        assertEquals(
                "GTSC-DEMO",
                validationProbeBean.create(
                        "GTSC-DEMO",
                        "Demo Supplier",
                        "Sri Lanka",
                        "ops@example.com"
                )
        );

        EJBException exception = assertThrows(
                EJBException.class,
                () -> validationProbeBean.create(
                        "GTSC-DEMO",
                        "Demo Supplier",
                        "Sri Lanka",
                        "invalid-email"
                )
        );

        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals(
                "Enter a valid operations email address",
                exception.getCause().getMessage()
        );

        EJBException supplyException = assertThrows(
                EJBException.class,
                () -> validationProbeBean.reviewSupply(
                        1L,
                        4.5,
                        "not allowed"
                )
        );

        assertTrue(supplyException.getCause() instanceof SupplyChainException);
    }
}
