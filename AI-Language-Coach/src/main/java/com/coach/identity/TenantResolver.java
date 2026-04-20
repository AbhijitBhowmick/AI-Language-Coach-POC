package com.coach.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class TenantResolver {

    @Value("${platform.tenant.allowed-ids:1,2}")
    private List<String> allowedTenantIds;

    public String resolveTenantId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            
            Optional<String> tenantFromClaim = extractTenantFromJwt(jwt);
            if (tenantFromClaim.isPresent() && isTenantAllowed(tenantFromClaim.get())) {
                return tenantFromClaim.get();
            }
        }

        return null;
    }

    private Optional<String> extractTenantFromJwt(Jwt jwt) {
        if (jwt.hasClaim("tenant_id")) {
            return Optional.ofNullable(jwt.getClaimAsString("tenant_id"));
        }
        if (jwt.hasClaim("tenantId")) {
            return Optional.ofNullable(jwt.getClaimAsString("tenantId"));
        }
        
        return Optional.empty();
    }

    private boolean isTenantAllowed(String tenantId) {
        return allowedTenantIds.contains(tenantId);
    }

    public boolean isValidTenant(String tenantId) {
        return tenantId != null && !tenantId.isBlank() && allowedTenantIds.contains(tenantId);
    }
}