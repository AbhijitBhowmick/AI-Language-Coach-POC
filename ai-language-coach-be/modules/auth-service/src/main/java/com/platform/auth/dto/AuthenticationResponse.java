package com.platform.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.platform.auth.entity.Role;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthenticationResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String userId,
        String email,
        String tenantId,
        String status,
        Set<Role> roles,
        String message
) {
    public static AuthenticationResponseBuilder builder() {
        return new AuthenticationResponseBuilder();
    }

    public static class AuthenticationResponseBuilder {
        private String accessToken;
        private String tokenType;
        private long expiresIn;
        private String userId;
        private String email;
        private String tenantId;
        private String status;
        private Set<Role> roles;
        private String message;

        public AuthenticationResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public AuthenticationResponseBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public AuthenticationResponseBuilder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public AuthenticationResponseBuilder userId(String userId) { this.userId = userId; return this; }
        public AuthenticationResponseBuilder email(String email) { this.email = email; return this; }
        public AuthenticationResponseBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public AuthenticationResponseBuilder status(String status) { this.status = status; return this; }
        public AuthenticationResponseBuilder roles(Set<Role> roles) { this.roles = roles; return this; }
        public AuthenticationResponseBuilder message(String message) { this.message = message; return this; }

        public AuthenticationResponse build() {
            return new AuthenticationResponse(accessToken, tokenType, expiresIn, userId, email, tenantId, status, roles, message);
        }
    }
}