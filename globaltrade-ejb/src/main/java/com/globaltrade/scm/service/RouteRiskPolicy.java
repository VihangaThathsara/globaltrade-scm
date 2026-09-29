package com.globaltrade.scm.service;

import com.globaltrade.scm.enums.ShipmentStatus;

import java.time.LocalDateTime;

public final class RouteRiskPolicy {
    private RouteRiskPolicy() { }

    public static int calculateScore(ShipmentStatus status, LocalDateTime eta, LocalDateTime now) {
        int score = 92;
        if (eta != null && now != null && eta.isBefore(now)) score -= 35;
        if (status == ShipmentStatus.DELAYED) score -= 25;
        return Math.max(20, score);
    }

    public static boolean needsAttention(int score) {
        return score < 60;
    }

    public static String recommendedAction(int score, ShipmentStatus status) {
        if (score < 60) {
            return status == ShipmentStatus.DELAYED
                    ? "review the carrier or an alternative route to reduce further delay"
                    : "review the carrier or an alternative route before the next dispatch step";
        }
        return "continue the current route under normal monitoring";
    }
}
