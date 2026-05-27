package com.watchlist.backend;

import com.sun.net.httpserver.HttpServer;
import com.watchlist.backend.http.AuthHandler;
import com.watchlist.backend.http.HealthHandler;
import com.watchlist.backend.http.ShowHandler;
import com.watchlist.backend.http.StatsHandler;
import com.watchlist.backend.repository.InMemoryShowRepository;
import com.watchlist.backend.repository.InMemoryUserRepository;
import com.watchlist.backend.service.AuthService;
import com.watchlist.backend.service.ShowService;
import com.watchlist.backend.validation.AuthValidator;
import com.watchlist.backend.validation.ShowValidator;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class ApiServer {
    private final HttpServer server;

    public ApiServer(int port) throws IOException {
        this(port, true);
    }

    public ApiServer(int port, boolean loadSeedData) throws IOException {
        // Build the main backend pieces once, then give them to the HTTP handlers.
        InMemoryShowRepository repository = new InMemoryShowRepository();
        ShowService service = new ShowService(repository);
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        AuthService authService = new AuthService(userRepository, service);
        ShowValidator validator = new ShowValidator();
        AuthValidator authValidator = new AuthValidator();

        if (loadSeedData) {
            userRepository.insert("demo", "demo123");
            service.seedUserShows("demo");
        }

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/auth/login", new AuthHandler(authService, authValidator));
        server.createContext("/api/auth/signup", new AuthHandler(authService, authValidator));
        server.createContext("/api/shows", new ShowHandler(service, validator));
        server.createContext("/api/stats", new StatsHandler(service));
    }

    public void start() {
        server.start();
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }
}
