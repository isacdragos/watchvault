package com.watchvault.watchvaultbackendspringboot.controller;

import com.watchvault.watchvaultbackendspringboot.dto.AuthRequest;
import com.watchvault.watchvaultbackendspringboot.dto.AuthResponse;
import com.watchvault.watchvaultbackendspringboot.service.AuthSessionService;
import com.watchvault.watchvaultbackendspringboot.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthSessionService authSessionService;

    public AuthController(AuthService authService, AuthSessionService authSessionService) {
        this.authService = authService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody AuthRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader("Authorization") String authorization) {
        authSessionService.logout(authorization);
    }
}
