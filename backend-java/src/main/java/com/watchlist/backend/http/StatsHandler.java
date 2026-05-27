package com.watchlist.backend.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.watchlist.backend.errors.ApiException;
import com.watchlist.backend.service.ShowService;

import java.io.IOException;
import java.util.Map;

public final class StatsHandler implements HttpHandler {
    private final ShowService showService;

    public StatsHandler(ShowService showService) {
        this.showService = showService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (ExchangeUtils.handlePreflight(exchange)) {
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                ExchangeUtils.sendJson(exchange, 405, Map.of("message", "Method not allowed."));
                return;
            }

            String username = ExchangeUtils.requireAuthenticatedUsername(exchange);
            ExchangeUtils.sendJson(exchange, 200, showService.buildStatsResponse(username));
        } catch (ApiException exception) {
            ExchangeUtils.sendError(exchange, exception);
        } catch (Exception exception) {
            ExchangeUtils.sendInternalError(exchange);
        } finally {
            exchange.close();
        }
    }
}
