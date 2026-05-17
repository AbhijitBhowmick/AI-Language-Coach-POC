package com.platform.auth.entity;

public enum Role {
    SUPER_ADMIN,
    BUSINESS_ADMIN,
    ADMIN_TEACHER,
    USER_STUDENT;

    public String getAuthority() {
        return "ROLE_" + name();
    }

    public boolean canCreateRole(Role targetRole) {
        return switch (this) {
            case SUPER_ADMIN -> true;
            case BUSINESS_ADMIN -> targetRole != SUPER_ADMIN;
            case ADMIN_TEACHER -> targetRole == USER_STUDENT;
            case USER_STUDENT -> false;
        };
    }

    public boolean requiresApproval() {
        return this == BUSINESS_ADMIN;
    }

    public static Role fromString(String role) {
        if (role == null || role.isBlank()) {
            return USER_STUDENT;
        }
        try {
            return valueOf(role.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return USER_STUDENT;
        }
    }
}