package com.globaltrade.scm.architecture;

import com.globaltrade.scm.exception.CarrierIntegrationException;
import com.globaltrade.scm.integration.CarrierIntegrationService;
import com.globaltrade.scm.interceptor.TradeComplianceInterceptor;
import com.globaltrade.scm.interceptor.VendorValidationInterceptor;
import com.globaltrade.scm.service.*;
import com.globaltrade.scm.timer.SupplyChainAutomationBean;
import jakarta.ejb.*;
import jakarta.interceptor.Interceptors;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedEjbContractTest {

    @Test
    void declarativeTimersArePersistent() throws Exception {
        for (String methodName : new String[]{"stockGuard", "shipmentWatch", "partnerPulse", "routeHealthRefresh"}) {
            Method method = SupplyChainAutomationBean.class.getMethod(methodName);
            Schedule schedule = method.getAnnotation(Schedule.class);
            assertNotNull(schedule, methodName + " must be a declarative EJB timer");
            assertTrue(schedule.persistent(), methodName + " must use persistent timer storage");
        }
        assertNotNull(SupplyChainAutomationBean.class.getMethod("customsDeadlineReminder", Timer.class).getAnnotation(Timeout.class));
    }

    @Test
    void transactionDemarcationCoversBeanAndContainerManagedScenarios() throws Exception {
        TransactionManagement tm = CustomsBatchService.class.getAnnotation(TransactionManagement.class);
        assertNotNull(tm);
        assertEquals(TransactionManagementType.BEAN, tm.value());

        TransactionAttribute reserve = InventoryService.class
                .getMethod("reserveStock", long.class, int.class)
                .getAnnotation(TransactionAttribute.class);
        assertNotNull(reserve);
        assertEquals(TransactionAttributeType.MANDATORY, reserve.value());

        TransactionAttribute externalCall = CarrierIntegrationService.class
                .getMethod("confirmBookingWindow", String.class, String.class, String.class)
                .getAnnotation(TransactionAttribute.class);
        assertNotNull(externalCall);
        assertEquals(TransactionAttributeType.NOT_SUPPORTED, externalCall.value());
    }

    @Test
    void validationAndComplianceInterceptorsAreAttached() throws Exception {
        Interceptors vendorInterceptors = VendorService.class.getAnnotation(Interceptors.class);
        assertNotNull(vendorInterceptors);
        assertTrue(Arrays.asList(vendorInterceptors.value()).contains(VendorValidationInterceptor.class));

        Interceptors customsInterceptors = CustomsService.class
                .getMethod("updateStatus", long.class, com.globaltrade.scm.enums.CustomsStatus.class)
                .getAnnotation(Interceptors.class);
        assertNotNull(customsInterceptors);
        assertTrue(Arrays.asList(customsInterceptors.value()).contains(TradeComplianceInterceptor.class));
    }

    @Test
    void carrierFailureIsRollbackApplicationException() {
        ApplicationException annotation = CarrierIntegrationException.class.getAnnotation(ApplicationException.class);
        assertNotNull(annotation);
        assertTrue(annotation.rollback());
    }
}
