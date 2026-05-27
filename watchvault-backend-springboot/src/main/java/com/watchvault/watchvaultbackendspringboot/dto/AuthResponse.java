package com.watchvault.watchvaultbackendspringboot.dto;

import java.util.List;

public class AuthResponse {
    private final String username;
    private final List<String> roles;
    private final List<String> permissions;
    private final String token;
    private final long sessionTimeoutMinutes;

    public AuthResponse(
            String username,
            List<String> roles,
            List<String> permissions,
            String token,
            long sessionTimeoutMinutes
    ) {
        this.username = username;
        this.roles = roles;
        this.permissions = permissions;
        this.token = token;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public String getToken() {
        return token;
    }

    public long getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }
}
