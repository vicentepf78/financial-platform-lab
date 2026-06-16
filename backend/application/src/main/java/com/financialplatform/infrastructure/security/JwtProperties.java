package com.financialplatform.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private boolean enabled = false;
    private String secret = "";
    private long expirationSeconds = 3600;
    private Map<String, UserProperties> users = createDefaultUsers();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
    }

    public Map<String, UserProperties> getUsers() {
        return users;
    }

    public void setUsers(Map<String, UserProperties> users) {
        this.users = users;
    }

    private static Map<String, UserProperties> createDefaultUsers() {
        Map<String, UserProperties> defaults = new LinkedHashMap<>();

        UserProperties operator = new UserProperties();
        operator.setPassword("operator");
        operator.setRoles(List.of("OPERATOR"));
        defaults.put("operator", operator);

        UserProperties admin = new UserProperties();
        admin.setPassword("admin");
        admin.setRoles(List.of("ADMIN", "OPERATOR"));
        defaults.put("admin", admin);

        return defaults;
    }

    public static class UserProperties {

        private String password = "";
        private List<String> roles = List.of();

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
