package ru.practicum.stats;

import client.RestClientConfig;
import client.StatClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClientResponseException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import({RestClientConfig.class, StatClient.class})
@Testcontainers
class StatsClientServerIntegrationTest {

    private static final String APP_NAME = "ewm-main-service";

    private static final String FIRST_URI = "/events/1";
    private static final String SECOND_URI = "/events/2";
    private static final String THIRD_URI = "/events/3";

    private static final LocalDateTime START =
            LocalDateTime.of(2026, 11, 16, 10, 0);

    private static final LocalDateTime END =
            LocalDateTime.of(2026, 11, 16, 12, 0);

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int SERVER_PORT = findAvailablePort();

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.1")
            .withDatabaseName("stats").withUsername("user").withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("server.port", () -> SERVER_PORT);
        registry.add("stats.server.base-url", () -> "http://localhost:" + SERVER_PORT);
        registry.add("stats.server.connect-timeout", () -> "2s");
        registry.add("stats.server.read-timeout", () -> "5s");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StatClient statClient;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM hits");
    }

    @Test
    void shouldRegisterHitAndReturnStatistics() {
        EndpointHit hit = createHit(FIRST_URI, "192.168.1.100",
                LocalDateTime.of(2026, 11, 16, 10, 30)
        );

        var response = statClient.hit(hit);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hits WHERE app = ? AND uri = ? AND ip = ?",
                Integer.class, APP_NAME, FIRST_URI, "192.168.1.100");

        assertEquals(1, count);

        List<ViewStats> statistics = getStatistics(List.of(), false);

        assertEquals(1, statistics.size());

        ViewStats stats = findByUri(statistics, FIRST_URI);

        assertEquals(APP_NAME, stats.getApp());
        assertEquals(FIRST_URI, stats.getUri());
        assertEquals(1L, stats.getHits());
    }

    @Test
    void shouldCountAllHitsWhenUniqueIsFalse() {
        insertHit(FIRST_URI, "192.168.1.100",
                LocalDateTime.of(2026, 11, 16, 10, 10));

        insertHit(FIRST_URI, "192.168.1.100",
                LocalDateTime.of(2026, 11, 16, 10, 20));

        insertHit(FIRST_URI, "192.168.1.101",
                LocalDateTime.of(2026, 11, 16, 10, 30));

        List<ViewStats> statistics = getStatistics(List.of(), false);

        assertEquals(1, statistics.size());
        assertEquals(3L, findByUri(statistics, FIRST_URI).getHits());
    }

    @Test
    void shouldCountUniqueIpsWhenUniqueIsTrue() {
        insertHit(FIRST_URI, "192.168.1.100",
                LocalDateTime.of(2026, 11, 16, 10, 10));

        insertHit(FIRST_URI, "192.168.1.100",
                LocalDateTime.of(2026, 11, 16, 10, 20));

        insertHit(FIRST_URI, "192.168.1.101",
                LocalDateTime.of(2026, 11, 16, 10, 30));

        List<ViewStats> statistics = getStatistics(List.of(), true);

        assertEquals(1, statistics.size());
        assertEquals(2L, findByUri(statistics, FIRST_URI).getHits());
    }

    @Test
    void shouldFilterStatisticsBySeveralUris() {
        insertHit(FIRST_URI, "192.168.1.100",
                LocalDateTime.of(2026, 11, 16, 10, 10));

        insertHit(SECOND_URI, "192.168.1.101",
                LocalDateTime.of(2026, 11, 16, 10, 20));

        insertHit(THIRD_URI, "192.168.1.102",
                LocalDateTime.of(2026, 11, 16, 10, 30));

        List<ViewStats> statistics = getStatistics(
                List.of(FIRST_URI, SECOND_URI),
                false
        );

        assertEquals(2, statistics.size());

        assertNotNull(findByUri(statistics, FIRST_URI));
        assertNotNull(findByUri(statistics, SECOND_URI));
        assertNull(findByUri(statistics, THIRD_URI));
    }

    @Test
    void shouldFilterStatisticsBySingleUri() {
        insertHit(FIRST_URI, "192.168.1.100",
                LocalDateTime.of(2026, 11, 16, 10, 10));

        insertHit(SECOND_URI, "192.168.1.101",
                LocalDateTime.of(2026, 11, 16, 10, 20));

        List<ViewStats> statistics = getStatistics(List.of(FIRST_URI), false);

        assertEquals(1, statistics.size());
        assertEquals(FIRST_URI, statistics.get(0).getUri());
    }

    @Test
    void shouldReturnEmptyStatisticsWhenThereAreNoHits() {
        List<ViewStats> statistics = getStatistics(List.of(), false);

        assertNotNull(statistics);
        assertTrue(statistics.isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenTimeRangeIsInvalid() {
        String later = "2026-11-16 12:00:00";
        String earlier = "2026-11-16 10:00:00";

        RestClientResponseException exception = assertThrows(RestClientResponseException.class,
                () -> statClient.get(later, earlier, List.of(), false));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        assertTrue(exception.getResponseBodyAsString().contains("Некорректный интервал времени"));
    }

    private List<ViewStats> getStatistics(List<String> uris, boolean unique) {
        return statClient.get(START.format(FORMATTER), END.format(FORMATTER), uris, unique);
    }

    private ViewStats findByUri(List<ViewStats> statistics, String uri) {
        return statistics.stream().filter(stats -> uri.equals(stats.getUri())).findFirst().orElse(null);
    }

    private EndpointHit createHit(String uri, String ip, LocalDateTime timestamp) {
        return new EndpointHit(APP_NAME, uri, ip, timestamp);
    }

    private void insertHit(String uri, String ip, LocalDateTime timestamp) {
        jdbcTemplate.update("INSERT INTO hits (app, uri, ip, timestamp) VALUES (?, ?, ?, ?)",
                APP_NAME, uri, ip, Timestamp.valueOf(timestamp)
        );
    }

    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
