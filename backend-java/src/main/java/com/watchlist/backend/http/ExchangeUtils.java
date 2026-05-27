package com.watchlist.backend.http;

import com.sun.net.httpserver.HttpExchange;
import com.watchlist.backend.errors.ApiException;
import com.watchlist.backend.util.JsonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ExchangeUtils {
    private static final String ALLOWED_METHODS = "GET,POST,PUT,PATCH,DELETE,OPTIONS";
    private static final String AUTH_HEADER = "X-Auth-User";

    private ExchangeUtils() {
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object payload) throws IOException {
        byte[] responseBytes = JsonUtils.stringify(payload).getBytes(StandardCharsets.UTF_8);
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    public static void sendNoContent(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    public static boolean handlePreflight(HttpExchange exchange) throws IOException {
        if (!"OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            return false;
        }

        addCorsHeaders(exchange);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return true;
    }

    public static LinkedHashMap<String, Object> readJsonObject(HttpExchange exchange) throws IOException {
        String body = readBody(exchange.getRequestBody());

        if (body.isBlank()) {
            return new LinkedHashMap<>();
        }

        Object parsedValue;

        try {
            parsedValue = JsonUtils.parse(body);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(400, "Request body must be valid JSON.");
        }

        if (!(parsedValue instanceof Map<?, ?> parsedMap)) {
            throw new ApiException(400, "Request body must be a JSON object.");
        }

        LinkedHashMap<String, Object> normalized = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : parsedMap.entrySet()) {
            normalized.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        return normalized;
    }

    public static void sendError(HttpExchange exchange, ApiException exception) throws IOException {
        sendJson(exchange, exception.getStatusCode(), Map.of("message", exception.getMessage()));
    }

    public static void sendInternalError(HttpExchange exchange) throws IOException {
        sendJson(exchange, 500, Map.of("message", "Internal server error."));
    }

    public static String requireAuthenticatedUsername(HttpExchange exchange) {
        String username = exchange.getRequestHeaders().getFirst(AUTH_HEADER);

        if (username == null || username.trim().isEmpty()) {
            throw new ApiException(401, "Authentication required.");
        }

        return username.trim();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", ALLOWED_METHODS);
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-Auth-User");
    }

    private static String readBody(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
