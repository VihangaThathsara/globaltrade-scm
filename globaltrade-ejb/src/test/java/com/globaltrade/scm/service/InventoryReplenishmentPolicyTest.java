package com.globaltrade.scm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryReplenishmentPolicyTest {
    @Test
    void recommendsEnoughStockToRestoreSimpleBufferTarget() {
        assertEquals(20L, InventoryReplenishmentPolicy.targetStock(10));
        assertEquals(10L, InventoryReplenishmentPolicy.suggestedQuantity(10, 10));
        assertEquals(15L, InventoryReplenishmentPolicy.suggestedQuantity(5, 10));
    }

    @Test
    void noRecommendationWhenStockIsAbovePreferredMinimum() {
        assertEquals(0L, InventoryReplenishmentPolicy.suggestedQuantity(12, 10));
    }
}
