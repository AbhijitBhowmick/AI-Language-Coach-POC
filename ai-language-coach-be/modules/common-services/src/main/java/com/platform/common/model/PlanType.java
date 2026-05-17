package com.platform.common.model;

public enum PlanType {
    FREE,
    BASIC,
    PREMIUM,
    ENTERPRISE;

    public static PlanType fromCode(String code) {
        if (code == null || code.isBlank()) return FREE;
        return switch (code.toUpperCase()) {
            case "BASIC" -> BASIC;
            case "PREMIUM" -> PREMIUM;
            case "ENTERPRISE" -> ENTERPRISE;
            default -> FREE;
        };
    }
}