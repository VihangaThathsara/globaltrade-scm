package com.globaltrade.scm.service;

import com.globaltrade.scm.enums.ShipmentStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RouteRiskPolicyTest {
    private final LocalDateTime now = LocalDateTime.of(2026, 8, 28, 10, 0);

    @Test
    void healthyFutureShipmentKeepsHighScore() {
        int score = RouteRiskPolicy.calculateScore(ShipmentStatus.IN_TRANSIT, now.plusHours(5), now);
        assertEquals(92, score);
        assertFalse(RouteRiskPolicy.needsAttention(score));
    }

    @Test
    void delayedOverdueShipmentCreatesRiskScore() {
        int score = RouteRiskPolicy.calculateScore(ShipmentStatus.DELAYED, now.minusHours(1), now);
        assertEquals(32, score);
        assertTrue(RouteRiskPolicy.needsAttention(score));
    }
    @Test
    void riskyRouteProducesOptimizationRecommendation() {
        int score = RouteRiskPolicy.calculateScore(ShipmentStatus.DELAYED, now.minusHours(1), now);
        assertTrue(RouteRiskPolicy.recommendedAction(score, ShipmentStatus.DELAYED).contains("alternative route"));
    }

}

