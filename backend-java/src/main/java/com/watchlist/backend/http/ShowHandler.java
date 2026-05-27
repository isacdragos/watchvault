package com.watchlist.backend.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.watchlist.backend.errors.ApiException;
import com.watchlist.backend.service.ShowService;
import com.watchlist.backend.validation.ShowValidator;
import com.watchlist.backend.validation.ShowValidator.PaginationQuery;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ShowHandler implements HttpHandler {
    private static final String SHOWS_PATH = "/api/shows";

    private final ShowService showService;
    private final ShowValidator showValidator;

    public ShowHandler(ShowService showService, ShowValidator showValidator) {
        this.showService = showService;
        this.showValidator = showValidator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (ExchangeUtils.handlePreflight(exchange)) {
                return;
            }

            String username = ExchangeUtils.requireAuthenticatedUsername(exchange);
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (isCollectionPath(path)) {
                handleCollection(exchange, method, username);
                return;
            }

            String showId = extractShowId(path);

            handleItem(exchange, method, username, showId);
        } catch (ApiException exception) {
            ExchangeUtils.sendError(exchange, exception);
        } catch (Exception exception) {
            ExchangeUtils.sendInternalError(exchange);
        } finally {
            exchange.close();
        }
    }

    private boolean isCollectionPath(String path) {
        return path.equals(SHOWS_PATH) || path.equals(SHOWS_PATH + "/");
    }

    private String extractShowId(String path) {
        if (!path.startsWith(SHOWS_PATH + "/")) {
            throw new ApiException(404, "Route not found.");
        }

        String showId = path.substring((SHOWS_PATH + "/").length()).trim();

        if (showId.isEmpty() || showId.contains("/")) {
            throw new ApiException(404, "Route not found.");
        }

        return showId;
    }

    private void handleCollection(HttpExchange exchange, String method, String username) throws IOException {
        switch (method) {
            case "GET" -> {
                PaginationQuery query = showValidator.validatePaginationQuery(exchange.getRequestURI().getRawQuery());
                ExchangeUtils.sendJson(exchange, 200, showService.listShows(username, query));
            }
            case "POST" -> {
                LinkedHashMap<String, Object> payload = ExchangeUtils.readJsonObject(exchange);
                LinkedHashMap<String, Object> validated = showValidator.validateCreatePayload(payload);
                ExchangeUtils.sendJson(exchange, 201, showService.createShow(username, validated));
            }
            default -> ExchangeUtils.sendJson(exchange, 405, Map.of("message", "Method not allowed."));
        }
    }

    private void handleItem(HttpExchange exchange, String method, String username, String showId) throws IOException {
        switch (method) {
            case "GET" -> ExchangeUtils.sendJson(exchange, 200, showService.getShowById(username, showId));
            case "PUT" -> {
                LinkedHashMap<String, Object> payload = ExchangeUtils.readJsonObject(exchange);
                LinkedHashMap<String, Object> validated = showValidator.validateCreatePayload(payload);
                ExchangeUtils.sendJson(exchange, 200, showService.replaceShow(username, showId, validated));
            }
            case "PATCH" -> {
                LinkedHashMap<String, Object> payload = ExchangeUtils.readJsonObject(exchange);
                LinkedHashMap<String, Object> validated = showValidator.validateUpdatePayload(payload);
                ExchangeUtils.sendJson(exchange, 200, showService.updateShow(username, showId, validated));
            }
            case "DELETE" -> {
                showService.deleteShow(username, showId);
                ExchangeUtils.sendNoContent(exchange);
            }
            default -> ExchangeUtils.sendJson(exchange, 405, Map.of("message", "Method not allowed."));
        }
    }
}
