package com.coach.identity;

import java.util.UUID;

public interface SecurityPrincipalService {
    UUID getCurrentUserId();
    String getUsername();
    String getEmail();
    boolean hasRole(String role);
    boolean isStudent();
    boolean isAdmin();
}