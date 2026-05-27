package com.watchvault.watchvaultbackendspringboot.service;

import com.watchvault.watchvaultbackendspringboot.entity.AuthSessionEntity;
import com.watchvault.watchvaultbackendspringboot.entity.UserEntity;
import com.watchvault.watchvaultbackendspringboot.error.UnauthorizedException;
import com.watchvault.watchvaultbackendspringboot.repository.AuthSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;

@Service
public class AuthSessionService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthSessionRepository authSessionRepository;
    private final Duration sessionTimeout;

    public AuthSessionService(
            AuthSessionRepository authSessionRepository,
            @Value("${app.security.session-timeout-minutes:15}") long sessionTimeoutMinutes
    ) {
        this.authSessionRepository = authSessionRepository;
        this.sessionTimeout = Duration.ofMinutes(sessionTimeoutMinutes);
    }

    @Transactional
    public String createSession(UserEntity user) {
        String rawToken = generateToken();

        AuthSessionEntity session = new AuthSessionEntity();
        session.setUser(user);
        session.setTokenHash(hashToken(rawToken));
        authSessionRepository.save(session);

        return rawToken;
    }

    @Transactional
    public UserEntity requireAuthenticatedUser(String authorizationHeader) {
        String tokenHash = hashToken(extractBearerToken(authorizationHeader));
        AuthSessionEntity session = authSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Authentication required."));

        OffsetDateTime now = OffsetDateTime.now();
        if (session.getLastActivityAt().plus(sessionTimeout).isBefore(now)) {
            authSessionRepository.delete(session);
            throw new UnauthorizedException("Session expired due to inactivity.");
        }

        session.setLastActivityAt(now);
        return session.getUser();
    }

    @Transactional
    public void logout(String authorizationHeader) {
        String tokenHash = hashToken(extractBearerToken(authorizationHeader));
        authSessionRepository.deleteByTokenHash(tokenHash);
    }

    @Transactional
    public void invalidateUserSessions(Long userId) {
        authSessionRepository.deleteByUserId(userId);
    }

    public long getSessionTimeoutMinutes() {
        return sessionTimeout.toMinutes();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException("Authentication required.");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authentication token must use Bearer format.");
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new UnauthorizedException("Authentication token is missing.");
        }

        return token;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}
