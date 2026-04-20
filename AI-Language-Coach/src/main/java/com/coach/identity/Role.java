package com.coach.identity;

public enum Role {
    STUDENT,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}