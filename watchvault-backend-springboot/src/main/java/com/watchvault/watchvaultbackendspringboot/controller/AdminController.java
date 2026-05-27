package com.watchvault.watchvaultbackendspringboot.controller;

import com.watchvault.watchvaultbackendspringboot.dto.AdminUserResponse;
import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import com.watchvault.watchvaultbackendspringboot.service.AuthSessionService;
import com.watchvault.watchvaultbackendspringboot.service.AdminService;
import com.watchvault.watchvaultbackendspringboot.service.UserAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService adminService;
    private final AuthSessionService authSessionService;
    private final UserAccessService userAccessService;

    public AdminController(
            AdminService adminService,
            AuthSessionService authSessionService,
            UserAccessService userAccessService
    ) {
        this.adminService = adminService;
        this.authSessionService = authSessionService;
        this.userAccessService = userAccessService;
    }

    @GetMapping
    public List<AdminUserResponse> listUsers(@RequestHeader("Authorization") String authorization) {
        UserEntity requester = authSessionService.requireAuthenticatedUser(authorization);
        userAccessService.requirePermission(requester, UserAccessService.USER_MANAGE_PERMISSION);
        return adminService.listUsers(requester.getUsername());
    }

    @PostMapping("/{userId}/promote")
    public AdminUserResponse promoteUser(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long userId
    ) {
        UserEntity requester = authSessionService.requireAuthenticatedUser(authorization);
        userAccessService.requirePermission(requester, UserAccessService.USER_MANAGE_PERMISSION);
        return adminService.promoteUserToAdmin(requester.getUsername(), userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long userId
    ) {
        UserEntity requester = authSessionService.requireAuthenticatedUser(authorization);
        userAccessService.requirePermission(requester, UserAccessService.USER_MANAGE_PERMISSION);
        adminService.deleteUser(requester.getUsername(), userId);
    }
}
