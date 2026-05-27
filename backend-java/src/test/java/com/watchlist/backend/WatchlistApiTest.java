package com.watchlist.backend;

import com.watchlist.backend.util.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public final class WatchlistApiTest {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private WatchlistApiTest() {
    }

    public static void main(String[] args) throws Exception {
        ApiServer server = new ApiServer(0, false);
        server.start();

        try {
            String baseUrl = "http://localhost:" + server.getPort();
            runTest("validator unit tests", ShowValidatorUnitTest::runAll);
            runTest("service unit tests", ShowServiceUnitTest::runAll);
            runTest("signup and login work", () -> testSignupAndLogin(baseUrl));
            runTest("users have separate lists", () -> testUserListIsolation(baseUrl));
            runTest("health endpoint reports server status", () -> testHealth(baseUrl));
            runTest("create and fetch a show", () -> testCreateAndFetch(baseUrl));
            runTest("reject invalid payloads with server-side validation", () -> testInvalidPayload(baseUrl));
            runTest("list shows with pagination metadata", () -> testPagination(baseUrl));
            runTest("replace and partially update a show", () -> testReplaceAndPatch(baseUrl));
            runTest("delete removes a show from the in-memory store", () -> testDelete(baseUrl));
            runTest("statistics endpoint aggregates current shows", () -> testStats(baseUrl));
            runTest("reject invalid pagination query parameters", () -> testInvalidPagination(baseUrl));
        } finally {
            server.stop(0);
        }
    }

    private static void testHealth(String baseUrl) throws Exception {
        HttpResponse<String> response = request(baseUrl, "/api/health", "GET", null);
        Map<String, Object> body = objectBody(response);

        assertEquals(200, response.statusCode());
        assertEquals("ok", body.get("status"));
    }

    private static void testSignupAndLogin(String baseUrl) throws Exception {
        HttpResponse<String> signupResponse = request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"alex","password":"secret1","confirmPassword":"secret1"}
                """);
        Map<String, Object> signupBody = objectBody(signupResponse);
        assertEquals(201, signupResponse.statusCode());
        assertEquals("alex", signupBody.get("username"));

        HttpResponse<String> loginResponse = request(baseUrl, "/api/auth/login", "POST", """
                {"username":"alex","password":"secret1"}
                """);
        Map<String, Object> loginBody = objectBody(loginResponse);
        assertEquals(200, loginResponse.statusCode());
        assertEquals("alex", loginBody.get("username"));
    }

    private static void testUserListIsolation(String baseUrl) throws Exception {
        request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"maria","password":"secret1","confirmPassword":"secret1"}
                """);
        request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"john","password":"secret1","confirmPassword":"secret1"}
                """);

        request(baseUrl, "/api/shows", "POST", sampleShowJson("Maria Show"), "maria");
        request(baseUrl, "/api/shows", "POST", sampleShowJson("John Show"), "john");

        Map<String, Object> mariaShows = objectBody(request(baseUrl, "/api/shows", "GET", null, "maria"));
        Map<String, Object> johnShows = objectBody(request(baseUrl, "/api/shows", "GET", null, "john"));

        List<?> mariaItems = (List<?>) mariaShows.get("items");
        List<?> johnItems = (List<?>) johnShows.get("items");
        assertEquals(1, mariaItems.size());
        assertEquals(1, johnItems.size());
        assertEquals("Maria Show", ((Map<?, ?>) mariaItems.get(0)).get("title"));
        assertEquals("John Show", ((Map<?, ?>) johnItems.get(0)).get("title"));
    }

    private static void testCreateAndFetch(String baseUrl) throws Exception {
        request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"crud","password":"secret1","confirmPassword":"secret1"}
                """);
        HttpResponse<String> createResponse = request(baseUrl, "/api/shows", "POST", sampleShowJson("Arcane"), "crud");
        Map<String, Object> created = objectBody(createResponse);

        assertEquals(201, createResponse.statusCode());
        assertEquals("1", created.get("id"));
        assertEquals("Arcane", created.get("title"));

        HttpResponse<String> getResponse = request(baseUrl, "/api/shows/1", "GET", null, "crud");
        Map<String, Object> fetched = objectBody(getResponse);

        assertEquals(200, getResponse.statusCode());
        assertEquals("Animated fantasy drama.", fetched.get("description"));
    }

    private static void testInvalidPayload(String baseUrl) throws Exception {
        request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"validation","password":"secret1","confirmPassword":"secret1"}
                """);
        HttpResponse<String> response = request(baseUrl, "/api/shows", "POST", """
                {"title":"   ","type":"series","status":"watching"}
                """, "validation");
        Map<String, Object> body = objectBody(response);

        assertEquals(400, response.statusCode());
        assertEquals("title is required.", body.get("message"));
    }

    private static void testPagination(String baseUrl) throws Exception {
        request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"paging","password":"secret1","confirmPassword":"secret1"}
                """);
        request(baseUrl, "/api/shows", "POST", sampleShowJson("Show 1"), "paging");
        request(baseUrl, "/api/shows", "POST", sampleShowJson("Show 2"), "paging");
        request(baseUrl, "/api/shows", "POST", sampleShowJson("Show 3"), "paging");

        HttpResponse<String> response = request(baseUrl, "/api/shows?page=2&limit=2", "GET", null, "paging");
        Map<String, Object> body = objectBody(response);
        List<?> items = (List<?>) body.get("items");
        Map<String, Object> pagination = castMap(body.get("pagination"));

        assertEquals(200, response.statusCode());
        assertEquals(2L, pagination.get("page"));
        assertEquals(2L, pagination.get("limit"));
        assertEquals(3L, pagination.get("totalItems"));
        assertEquals(2L, pagination.get("totalPages"));
        assertEquals(Boolean.FALSE, pagination.get("hasNextPage"));
        assertEquals(Boolean.TRUE, pagination.get("hasPreviousPage"));
        assertEquals(1, items.size());
    }

    private static void testReplaceAndPatch(String baseUrl) throws Exception {
        request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"editor","password":"secret1","confirmPassword":"secret1"}
                """);
        HttpResponse<String> createResponse = request(baseUrl, "/api/shows", "POST", sampleShowJson("Initial Show"), "editor");
        Map<String, Object> created = objectBody(createResponse);
        String id = String.valueOf(created.get("id"));

        HttpResponse<String> putResponse = request(baseUrl, "/api/shows/" + id, "PUT", """
                {
                  "title":"Blue Eye Samurai",
                  "type":"series",
                  "status":"completed",
                  "description":"Stylish revenge story.",
                  "episodesWatched":8,
                  "totalEpisodes":8,
                  "rating":9.1,
                  "genres":["Action","Drama"]
                }
                """, "editor");
        Map<String, Object> replaced = objectBody(putResponse);
        assertEquals(200, putResponse.statusCode());
        assertEquals("Blue Eye Samurai", replaced.get("title"));
        assertEquals("completed", replaced.get("status"));

        HttpResponse<String> patchResponse = request(baseUrl, "/api/shows/" + id, "PATCH", """
                {"rating":10,"status":"watching"}
                """, "editor");
        Map<String, Object> patched = objectBody(patchResponse);
        assertEquals(200, patchResponse.statusCode());
        assertEquals(10.0, patched.get("rating"));
        assertEquals("watching", patched.get("status"));
    }

    private static void testDelete(String baseUrl) throws Exception {
        request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"deleter","password":"secret1","confirmPassword":"secret1"}
                """);
        HttpResponse<String> createResponse = request(baseUrl, "/api/shows", "POST", sampleShowJson("Delete Me"), "deleter");
        String id = String.valueOf(objectBody(createResponse).get("id"));

        HttpResponse<String> deleteResponse = request(baseUrl, "/api/shows/" + id, "DELETE", null, "deleter");
        assertEquals(204, deleteResponse.statusCode());

        HttpResponse<String> getResponse = request(baseUrl, "/api/shows/" + id, "GET", null, "deleter");
        assertEquals(404, getResponse.statusCode());
    }

    private static void testStats(String baseUrl) throws Exception {
        request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"stats","password":"secret1","confirmPassword":"secret1"}
                """);
        request(baseUrl, "/api/shows", "POST", """
                {
                  "title":"The Batman",
                  "type":"movie",
                  "status":"completed",
                  "description":"Dark detective story.",
                  "episodesWatched":0,
                  "totalEpisodes":0,
                  "rating":8,
                  "genres":["Action","Mystery"]
                }
                """, "stats");

        HttpResponse<String> response = request(baseUrl, "/api/stats", "GET", null, "stats");
        Map<String, Object> body = objectBody(response);
        Map<String, Object> byStatus = castMap(body.get("byStatus"));
        Map<String, Object> byType = castMap(body.get("byType"));

        assertEquals(200, response.statusCode());
        assertEquals(1L, body.get("totalShows"));
        assertEquals(8.0, body.get("averageRating"));
        assertTrue(byStatus.containsKey("completed"));
        assertTrue(byType.containsKey("movie"));
    }

    private static void testInvalidPagination(String baseUrl) throws Exception {
        request(baseUrl, "/api/auth/signup", "POST", """
                {"username":"query","password":"secret1","confirmPassword":"secret1"}
                """);
        HttpResponse<String> response = request(baseUrl, "/api/shows?page=0&limit=500", "GET", null, "query");
        Map<String, Object> body = objectBody(response);

        assertEquals(400, response.statusCode());
        assertEquals("page must be a positive integer.", body.get("message"));
    }

    private static HttpResponse<String> request(String baseUrl, String path, String method, String body) throws IOException, InterruptedException {
        return request(baseUrl, path, method, body, null);
    }

    private static HttpResponse<String> request(String baseUrl, String path, String method, String body, String username) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path));

        if (body != null) {
            builder.header("Content-Type", "application/json");
        }

        if (username != null) {
            builder.header("X-Auth-User", username);
        }

        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        }

        return CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objectBody(HttpResponse<String> response) {
        return (Map<String, Object>) JsonUtils.parse(response.body());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private static String sampleShowJson(String title) {
        return """
                {
                  "title":"%s",
                  "type":"series",
                  "status":"watching",
                  "description":"Animated fantasy drama.",
                  "episodesWatched":3,
                  "totalEpisodes":9,
                  "rating":9.5,
                  "genres":["Animation","Fantasy"]
                }
                """.formatted(title);
    }

    private static void runTest(String name, ThrowingRunnable runnable) throws Exception {
        try {
            runnable.run();
            System.out.println("PASS " + name);
        } catch (Exception exception) {
            System.out.println("FAIL " + name);
            throw exception;
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected instanceof Number expectedNumber && actual instanceof Number actualNumber) {
            double difference = Math.abs(expectedNumber.doubleValue() - actualNumber.doubleValue());

            if (difference > 0.000001d) {
                throw new AssertionError("Expected " + expected + " but got " + actual);
            }

            return;
        }

        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean value) {
        if (!value) {
            throw new AssertionError("Expected condition to be true.");
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
