package com.watchvault.watchvaultbackendspringboot;

import com.watchvault.watchvaultbackendspringboot.dto.AuthRequest;
import com.watchvault.watchvaultbackendspringboot.dto.ShowRequest;
import com.watchvault.watchvaultbackendspringboot.entity.AuthSessionEntity;
import com.watchvault.watchvaultbackendspringboot.repository.AuthSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:watchvault_auth_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.defer-datasource-initialization=true",
        "spring.sql.init.mode=always",
        "server.ssl.enabled=false",
        "app.security.session-timeout-minutes=1"
})
class AuthFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthSessionRepository authSessionRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void signupCreatesAdminSessionAndTokenForFirstUser() throws Exception {
        String response = signup("admin", "Password123!", "Password123!");

        assertThat(extractString(response, "username")).isEqualTo("admin");
        assertThat(extractString(response, "token")).isNotBlank();
        assertThat(response).contains("\"roles\":[\"ADMIN\"]");
        assertThat(response).contains("\"SHOW_READ\"", "\"SHOW_WRITE\"", "\"USER_MANAGE\"");
    }

    @Test
    void loginRejectsWrongPasswordAndAcceptsCorrectPassword() throws Exception {
        signup("admin-seed", "Password123!", "Password123!");
        signup("normal", "Password123!", "Password123!");

        HttpResponse<String> rejected = postJson("/api/auth/login", authRequest("normal", "bad-password", null));

        assertThat(rejected.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

        String accepted = login("normal", "Password123!");
        assertThat(extractString(accepted, "token")).isNotBlank();
        assertThat(accepted).contains("\"USER\"");
    }

    @Test
    void bearerTokenIsRequiredForProtectedEndpoints() throws Exception {
        HttpResponse<String> rejected = get("/api/shows", null);
        assertThat(rejected.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

        String session = signup("viewer", "Password123!", "Password123!");
        HttpResponse<String> accepted = get("/api/shows", extractString(session, "token"));

        assertThat(accepted.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void expiredSessionIsRejectedAndDeleted() throws Exception {
        String session = signup("timedout", "Password123!", "Password123!");
        AuthSessionEntity storedSession = authSessionRepository.findAll().stream()
                .filter(authSession -> authSession.getUser().getUsername().equals("timedout"))
                .findFirst()
                .orElseThrow();
        storedSession.setLastActivityAt(OffsetDateTime.now().minusMinutes(5));
        authSessionRepository.save(storedSession);

        HttpResponse<String> rejected = get("/api/shows", extractString(session, "token"));

        assertThat(rejected.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void postToShowIdUpdatesExistingShow() throws Exception {
        String session = signup("editor", "Password123!", "Password123!");
        String token = extractString(session, "token");

        HttpResponse<String> created = postJson("/api/shows", showRequest("Arcane", "watching", 3), token);
        assertThat(created.statusCode()).isEqualTo(HttpStatus.OK.value());

        String id = extractNumber(created.body(), "id");
        HttpResponse<String> updated = postJson(
                "/api/shows/" + id,
                showRequest("Arcane: League of Legends", "completed", 9),
                token
        );

        assertThat(updated.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(extractString(updated.body(), "title")).isEqualTo("Arcane: League of Legends");
        assertThat(extractString(updated.body(), "status")).isEqualTo("completed");
        assertThat(extractNumber(updated.body(), "episodesWatched")).isEqualTo("9");
    }

    @Test
    void statsIncludeWatchTimeAndGroupedCounts() throws Exception {
        String session = signup("stats-user", "Password123!", "Password123!");
        String token = extractString(session, "token");

        assertThat(postJson("/api/shows", showRequest("Arcane", "completed", 9), token).statusCode())
                .isEqualTo(HttpStatus.OK.value());
        assertThat(postJson("/api/shows", showRequest("Frieren", "watching", 12), token).statusCode())
                .isEqualTo(HttpStatus.OK.value());

        HttpResponse<String> stats = get("/api/stats", token);

        assertThat(stats.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(stats.body()).contains(
                "\"totalShows\":2",
                "\"episodesWatched\":21",
                "\"daysWatched\":0.4",
                "\"completed\":1",
                "\"watching\":1",
                "\"series\":2"
        );
    }

    @Test
    void statsCountTotalEpisodesForAnyStatusWhenWatchedCountIsZero() throws Exception {
        String session = signup("fallback-stats-user", "Password123!", "Password123!");
        String token = extractString(session, "token");

        assertThat(postJson("/api/shows", showRequest("Watching Show", "watching", 0), token).statusCode())
                .isEqualTo(HttpStatus.OK.value());
        assertThat(postJson("/api/shows", showRequest("Planned Show", "plan-to-watch", 0), token).statusCode())
                .isEqualTo(HttpStatus.OK.value());

        HttpResponse<String> stats = get("/api/stats", token);

        assertThat(stats.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(stats.body()).contains("\"episodesWatched\":18", "\"daysWatched\":0.3");
    }

    private String signup(String username, String password, String confirmPassword) throws Exception {
        HttpResponse<String> response = postJson("/api/auth/signup", authRequest(username, password, confirmPassword));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        return response.body();
    }

    private String login(String username, String password) throws Exception {
        HttpResponse<String> response = postJson("/api/auth/login", authRequest(username, password, null));

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        return response.body();
    }

    private AuthRequest authRequest(String username, String password, String confirmPassword) {
        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        return request;
    }

    private ShowRequest showRequest(String title, String status, int episodesWatched) {
        ShowRequest request = new ShowRequest();
        request.setTitle(title);
        request.setType("series");
        request.setStatus(status);
        request.setDescription("Animated fantasy drama with strong characters.");
        request.setReleaseDate(LocalDate.of(2021, 11, 6));
        request.setImage("data:image/png;base64,abc");
        request.setEpisodesWatched(episodesWatched);
        request.setTotalEpisodes(9);
        request.setRating(9.5);
        return request;
    }

    private HttpResponse<String> postJson(String path, AuthRequest body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, ShowRequest body, String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String token) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path))).GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String toJson(AuthRequest request) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"username\":\"").append(escapeJson(request.getUsername())).append("\",");
        json.append("\"password\":\"").append(escapeJson(request.getPassword())).append("\"");
        if (request.getConfirmPassword() != null) {
            json.append(",\"confirmPassword\":\"").append(escapeJson(request.getConfirmPassword())).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    private String toJson(ShowRequest request) {
        return """
                {
                  "title":"%s",
                  "type":"%s",
                  "status":"%s",
                  "description":"%s",
                  "releaseDate":"%s",
                  "image":"%s",
                  "episodesWatched":%d,
                  "totalEpisodes":%d,
                  "rating":%.1f
                }
                """.formatted(
                escapeJson(request.getTitle()),
                escapeJson(request.getType()),
                escapeJson(request.getStatus()),
                escapeJson(request.getDescription()),
                request.getReleaseDate(),
                escapeJson(request.getImage()),
                request.getEpisodesWatched(),
                request.getTotalEpisodes(),
                request.getRating()
        );
    }

    private String extractString(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private String extractNumber(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
