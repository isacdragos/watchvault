package com.watchvault.watchvaultbackendspringboot.service;

import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import com.watchvault.watchvaultbackendspringboot.error.UnauthorizedException;
import com.watchvault.watchvaultbackendspringboot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class UserAccessService {

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";

    public static final String SHOW_READ_PERMISSION = "SHOW_READ";
    public static final String SHOW_WRITE_PERMISSION = "SHOW_WRITE";
    public static final String USER_MANAGE_PERMISSION = "USER_MANAGE";

    private final UserRepository userRepository;

    public UserAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserEntity requireExistingUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new UnauthorizedException("Authentication required."));
    }

    @Transactional(readOnly = true)
    public UserEntity requireAdmin(String username) {
        UserEntity user = requireExistingUser(username);
        requireAdmin(user);
        return user;
    }

    public void requireAdmin(UserEntity user) {
        if (!isAdmin(user)) {
            throw new UnauthorizedException("Admin permissions required.");
        }
    }

    public boolean isAdmin(UserEntity user) {
        return user.getRoles().stream()
                .map(role -> role.getName().toUpperCase())
                .anyMatch(ADMIN_ROLE::equals);
    }

    public List<String> roleNames(UserEntity user) {
        return user.getRoles().stream()
                .map(role -> role.getName().toUpperCase())
                .sorted()
                .toList();
    }

    public List<String> permissionNames(UserEntity user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName().toUpperCase())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public boolean hasPermission(UserEntity user, String permissionName) {
        return permissionNames(user).stream()
                .anyMatch(permission -> permission.equalsIgnoreCase(permissionName));
    }

    public void requirePermission(UserEntity user, String permissionName) {
        if (!hasPermission(user, permissionName)) {
            throw new UnauthorizedException("Permission required: " + permissionName + ".");
        }
    }
}
