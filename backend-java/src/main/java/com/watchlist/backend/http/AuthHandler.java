package com.watchlist.backend.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.watchlist.backend.errors.ApiException;
import com.watchlist.backend.service.AuthService;
import com.watchlist.backend.validation.AuthValidator;
import com.watchlist.backend.validation.AuthValidator.Credentials;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AuthHandler implements HttpHandler {
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String SIGNUP_PATH = "/api/auth/signup";

    private final AuthService authService;
    private final AuthValidator authValidator;

    public AuthHandler(AuthService authService, AuthValidator authValidator) {
        this.authService = authService;
        this.authValidator = authValidator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (ExchangeUtils.handlePreflight(exchange)) {
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                ExchangeUtils.sendJson(exchange, 405, Map.of("message", "Method not allowed."));
                return;
            }

            LinkedHashMap<String, Object> payload = ExchangeUtils.readJsonObject(exchange);
            String path = exchange.getRequestURI().getPath();

            if (SIGNUP_PATH.equals(path)) {
                Credentials credentials = authValidator.validateCredentials(payload, true);
                ExchangeUtils.sendJson(exchange, 201, authService.signup(credentials.username(), credentials.password()));
                return;
            }

            if (LOGIN_PATH.equals(path)) {
                Credentials credentials = authValidator.validateCredentials(payload, false);
                ExchangeUtils.sendJson(exchange, 200, authService.login(credentials.username(), credentials.password()));
                return;
            }

            throw new ApiException(404, "Route not found.");
        } catch (ApiException exception) {
            ExchangeUtils.sendError(exchange, exception);
        } catch (Exception exception) {
            ExchangeUtils.sendInternalError(exchange);
        } finally {
            exchange.close();
        }
    }
}
