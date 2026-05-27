package com.watchvault.watchvaultbackendspringboot.controller;

import com.watchvault.watchvaultbackendspringboot.dto.ShowRequest;
import com.watchvault.watchvaultbackendspringboot.dto.ShowResponse;
import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import com.watchvault.watchvaultbackendspringboot.service.AuthSessionService;
import com.watchvault.watchvaultbackendspringboot.service.ShowService;
import com.watchvault.watchvaultbackendspringboot.service.UserAccessService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;
    private final AuthSessionService authSessionService;
    private final UserAccessService userAccessService;

    public ShowController(
            ShowService showService,
            AuthSessionService authSessionService,
            UserAccessService userAccessService
    ) {
        this.showService = showService;
        this.authSessionService = authSessionService;
        this.userAccessService = userAccessService;
    }

    @GetMapping
    public Page<ShowResponse> listShows(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        UserEntity user = authSessionService.requireAuthenticatedUser(authorization);
        userAccessService.requirePermission(user, UserAccessService.SHOW_READ_PERMISSION);
        return showService.listShows(user.getUsername(), status, search, page, size);
    }

    @GetMapping("/{id}")
    public ShowResponse getShow(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id
    ) {
        UserEntity user = authSessionService.requireAuthenticatedUser(authorization);
        userAccessService.requirePermission(user, UserAccessService.SHOW_READ_PERMISSION);
        return showService.getShow(user.getUsername(), id);
    }

    @PostMapping
    public ShowResponse createShow(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ShowRequest request
    ) {
        UserEntity user = authSessionService.requireAuthenticatedUser(authorization);
        userAccessService.requirePermission(user, UserAccessService.SHOW_WRITE_PERMISSION);
        return showService.createShow(user.getUsername(), request);
    }

    @PutMapping("/{id}")
    public ShowResponse updateShow(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody ShowRequest request
    ) {
        UserEntity user = authSessionService.requireAuthenticatedUser(authorization);
        userAccessService.requirePermission(user, UserAccessService.SHOW_WRITE_PERMISSION);
        return showService.updateShow(user.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShow(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id
    ) {
        UserEntity user = authSessionService.requireAuthenticatedUser(authorization);
        userAccessService.requirePermission(user, UserAccessService.SHOW_WRITE_PERMISSION);
        showService.deleteShow(user.getUsername(), id);
    }
}
