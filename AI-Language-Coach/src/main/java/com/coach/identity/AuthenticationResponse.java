package com.coach.identity;

public class AuthenticationResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String userId;
    private String email;
    private String tenantId;

    public AuthenticationResponse() {}

    public AuthenticationResponse(String accessToken, String tokenType, long expiresIn, String userId, String email, String tenantId) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
        this.tenantId = tenantId;
    }

    public static AuthenticationResponseBuilder builder() {
        return new AuthenticationResponseBuilder();
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public static class AuthenticationResponseBuilder {
        private String accessToken;
        private String tokenType;
        private long expiresIn;
        private String userId;
        private String email;
        private String tenantId;

        public AuthenticationResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public AuthenticationResponseBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public AuthenticationResponseBuilder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public AuthenticationResponseBuilder userId(String userId) { this.userId = userId; return this; }
        public AuthenticationResponseBuilder email(String email) { this.email = email; return this; }
        public AuthenticationResponseBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public AuthenticationResponse build() {
            return new AuthenticationResponse(accessToken, tokenType, expiresIn, userId, email, tenantId);
        }
    }
}
