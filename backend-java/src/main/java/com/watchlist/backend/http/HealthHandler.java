package com.watchlist.backend.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public final class HealthHandler implements HttpHandler {
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

            ExchangeUtils.sendJson(exchange, 200, Map.of("status", "ok"));
        } finally {
            exchange.close();
        }
    }
}
