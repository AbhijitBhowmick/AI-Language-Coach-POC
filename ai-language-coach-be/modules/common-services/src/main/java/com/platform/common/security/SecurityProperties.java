package com.platform.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private Cors cors = new Cors();
    private List<String> permitAllPaths = List.of();
    private Roles roles = new Roles();

    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }

    public List<String> getPermitAllPaths() { return permitAllPaths; }
    public void setPermitAllPaths(List<String> permitAllPaths) { this.permitAllPaths = permitAllPaths; }

    public Roles getRoles() { return roles; }
    public void setRoles(Roles roles) { this.roles = roles; }

    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000", "http://127.0.0.1:3000");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
        private String mapping = "/**";

        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> allowedOrigins) { this.allowedOrigins = allowedOrigins; }

        public List<String> getAllowedMethods() { return allowedMethods; }
        public void setAllowedMethods(List<String> allowedMethods) { this.allowedMethods = allowedMethods; }

        public String getMapping() { return mapping; }
        public void setMapping(String mapping) { this.mapping = mapping; }
    }

    public static class Roles {
        private String[] allAdmins = {"SUPER_ADMIN", "BUSINESS_ADMIN", "ADMIN_TEACHER"};
        private String[] elevatedAdmins = {"SUPER_ADMIN", "BUSINESS_ADMIN"};

        public String[] getAllAdmins() { return allAdmins; }
        public void setAllAdmins(String[] allAdmins) { this.allAdmins = allAdmins; }

        public String[] getElevatedAdmins() { return elevatedAdmins; }
        public void setElevatedAdmins(String[] elevatedAdmins) { this.elevatedAdmins = elevatedAdmins; }
    }
}
