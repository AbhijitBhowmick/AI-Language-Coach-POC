package com.coach.identity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantAuthenticationFilter extends OncePerRequestFilter {

    private final TenantResolver tenantResolver;

    public TenantAuthenticationFilter(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                String tenantId = resolveTenantFromJwt(jwt);
                
                if (tenantId != null && tenantResolver.isValidTenant(tenantId)) {
                    TenantContext.setTenantId(tenantId);
                }
            }
            
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenantFromJwt(Jwt jwt) {
        if (jwt.hasClaim("tenant_id")) {
            return jwt.getClaimAsString("tenant_id");
        }
        if (jwt.hasClaim("tenantId")) {
            return jwt.getClaimAsString("tenantId");
        }
        
        return null;
    }
}