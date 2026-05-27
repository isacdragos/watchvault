package com.watchvault.watchvaultbackendspringboot.service;

import com.watchvault.watchvaultbackendspringboot.dto.AuthRequest;
import com.watchvault.watchvaultbackendspringboot.dto.AuthResponse;
import com.watchvault.watchvaultbackendspringboot.entity.RoleEntity;
import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import com.watchvault.watchvaultbackendspringboot.error.BadRequestException;
import com.watchvault.watchvaultbackendspringboot.error.UnauthorizedException;
import com.watchvault.watchvaultbackendspringboot.repository.RoleRepository;
import com.watchvault.watchvaultbackendspringboot.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserAccessService userAccessService;
    private final AuthSessionService authSessionService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserAccessService userAccessService,
            AuthSessionService authSessionService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userAccessService = userAccessService;
        this.authSessionService = authSessionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse signup(AuthRequest request) {
        String username = request.getUsername().trim();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BadRequestException("That username is already taken.");
        }

        if (request.getConfirmPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(defaultRoleForNewUser());

        UserEntity savedUser = userRepository.save(user);
        return toAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String username = request.getUsername().trim();

        UserEntity user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UnauthorizedException("Incorrect username or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Incorrect username or password.");
        }

        return toAuthResponse(user);
    }

    private RoleEntity defaultRoleForNewUser() {
        String roleName = userRepository.count() == 0
                ? UserAccessService.ADMIN_ROLE
                : UserAccessService.USER_ROLE;

        return roleRepository.findByNameIgnoreCase(roleName)
                .orElseThrow(() -> new IllegalStateException("Missing role seed data for " + roleName + "."));
    }

    private AuthResponse toAuthResponse(UserEntity user) {
        String token = authSessionService.createSession(user);
        return new AuthResponse(
                user.getUsername(),
                userAccessService.roleNames(user),
                userAccessService.permissionNames(user),
                token,
                authSessionService.getSessionTimeoutMinutes()
        );
    }
}
