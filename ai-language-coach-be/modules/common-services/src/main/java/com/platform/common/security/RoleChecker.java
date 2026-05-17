package com.platform.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component("roleChecker")
public class RoleChecker {

    private final SecurityProperties securityProperties;

    public RoleChecker(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public boolean isAllAdmins() {
        return checkRoles(securityProperties.getRoles().getAllAdmins());
    }

    public boolean isElevatedAdmins() {
        return checkRoles(securityProperties.getRoles().getElevatedAdmins());
    }

    private boolean checkRoles(String[] allowedRoles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        List<String> allowedRoleList = Arrays.asList(allowedRoles);
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            if (allowedRoleList.contains(role)) {
                return true;
            }
        }
        return false;
    }
}
