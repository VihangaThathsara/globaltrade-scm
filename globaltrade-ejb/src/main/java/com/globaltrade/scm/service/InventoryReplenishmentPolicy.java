package com.globaltrade.scm.service;

public final class InventoryReplenishmentPolicy {
    private InventoryReplenishmentPolicy() { }

    public static long targetStock(int preferredMinimum) {
        return preferredMinimum <= 0 ? 0L : preferredMinimum * 2L;
    }

    public static long suggestedQuantity(long currentStock, int preferredMinimum) {
        if (preferredMinimum <= 0 || currentStock > preferredMinimum) return 0L;
        return Math.max(0L, targetStock(preferredMinimum) - Math.max(0L, currentStock));
    }
}
