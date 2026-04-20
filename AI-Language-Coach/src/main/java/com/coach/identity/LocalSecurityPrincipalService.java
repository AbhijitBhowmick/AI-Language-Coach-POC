package com.coach.identity;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class LocalSecurityPrincipalService implements SecurityPrincipalService {

    @Override
    public UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return user.getId();
        }
        return null;
    }

    @Override
    public String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails user) {
            return user.getUsername();
        }
        return null;
    }

    @Override
    public String getEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return user.getEmail();
        }
        return null;
    }

    @Override
    public boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }

    @Override
    public boolean isStudent() {
        return hasRole("STUDENT");
    }

    @Override
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}