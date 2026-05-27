package com.watchvault.watchvaultbackendspringboot.controller;

import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import com.watchvault.watchvaultbackendspringboot.service.AuthSessionService;
import com.watchvault.watchvaultbackendspringboot.service.StatsService;
import com.watchvault.watchvaultbackendspringboot.service.UserAccessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;
    private final AuthSessionService authSessionService;
    private final UserAccessService userAccessService;

    public StatsController(
            StatsService statsService,
            AuthSessionService authSessionService,
            UserAccessService userAccessService
    ) {
        this.statsService = statsService;
        this.authSessionService = authSessionService;
        this.userAccessService = userAccessService;
    }

    @GetMapping
    public Map<String, Object> getStats(@RequestHeader("Authorization") String authorization) {
        UserEntity user = authSessionService.requireAuthenticatedUser(authorization);
        userAccessService.requirePermission(user, UserAccessService.SHOW_READ_PERMISSION);
        return statsService.getStats(user.getUsername());
    }
}
