package com.watchvault.watchvaultbackendspringboot.dto;

import java.util.List;

public class AdminUserResponse {
    private final Long id;
    private final String username;
    private final List<String> roles;
    private final List<String> permissions;
    private final boolean promotable;
    private final boolean deletable;

    public AdminUserResponse(
            Long id,
            String username,
            List<String> roles,
            List<String> permissions,
            boolean promotable,
            boolean deletable
    ) {
        this.id = id;
        this.username = username;
        this.roles = roles;
        this.permissions = permissions;
        this.promotable = promotable;
        this.deletable = deletable;
    }

    public Long getId() {
        return id;
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

    public boolean isPromotable() {
        return promotable;
    }

    public boolean isDeletable() {
        return deletable;
    }
}
