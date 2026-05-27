package com.watchlist.backend;

public final class Application {
    private Application() {
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portValue = System.getenv("PORT");

        if (portValue != null && !portValue.isBlank()) {
            port = Integer.parseInt(portValue);
        }

        ApiServer server = new ApiServer(port);
        server.start();
        System.out.println("Watchlist Java API listening on http://localhost:" + port);
    }
}
