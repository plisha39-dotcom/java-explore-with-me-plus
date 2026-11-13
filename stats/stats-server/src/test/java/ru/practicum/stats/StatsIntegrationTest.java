package ru.practicum.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class StatsIntegrationTest {
    private static final String APP_NAME = "ewm-main-service";
    private static final String FIRST_URI = "/events/1";
    private static final String SECOND_URI = "/events/2";
    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 12, 14, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 11, 12, 16, 0);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.1")
            .withDatabaseName("stats")
            .withUsername("user")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM hits");
    }

    @Test
    void shouldSaveHitsThroughHttpAndReturnAggregatedStatistics() {
        registerHit(FIRST_URI, "192.168.1.100", LocalDateTime.of(2026, 11, 12, 14, 10));
        registerHit(FIRST_URI, "192.168.1.100", LocalDateTime.of(2026, 11, 12, 14, 20));
        registerHit(FIRST_URI, "192.168.1.101", LocalDateTime.of(2026, 11, 12, 14, 30));
        registerHit(SECOND_URI, "192.168.1.100", LocalDateTime.of(2026, 11, 12, 14, 40));
        registerHit(FIRST_URI, "192.168.1.102", LocalDateTime.of(2026, 11, 12, 16, 1));

        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(
                statisticsUri(false, List.of()), ViewStats[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ViewStats[] statistics = response.getBody();
        assertNotNull(statistics);
        assertEquals(2, statistics.length);
        assertEquals(APP_NAME, statistics[0].getApp());
        assertEquals(FIRST_URI, statistics[0].getUri());
        assertEquals(3L, statistics[0].getHits());
        assertEquals(SECOND_URI, statistics[1].getUri());
        assertEquals(1L, statistics[1].getHits());
    }

    @Test
    void shouldApplyUriFilterAndCountUniqueIpsThroughHttp() {
        insertHit(FIRST_URI, "192.168.1.100", LocalDateTime.of(2026, 11, 12, 14, 10));
        insertHit(FIRST_URI, "192.168.1.100", LocalDateTime.of(2026, 11, 12, 14, 20));
        insertHit(FIRST_URI, "192.168.1.101", LocalDateTime.of(2026, 11, 12, 14, 30));
        insertHit(SECOND_URI, "192.168.1.102", LocalDateTime.of(2026, 11, 12, 14, 40));

        ResponseEntity<ViewStats[]> response = restTemplate.getForEntity(
                statisticsUri(true, List.of(FIRST_URI)), ViewStats[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ViewStats[] statistics = response.getBody();
        assertNotNull(statistics);
        assertEquals(1, statistics.length);
        assertEquals(FIRST_URI, statistics[0].getUri());
        assertEquals(2L, statistics[0].getHits());
    }

    @Test
    void shouldReturnBadRequestWhenStartIsAfterEnd() {
        URI requestUri = URI.create("http://localhost:" + port + UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", "2026-11-12 16:00:00")
                .queryParam("end", "2026-11-12 14:00:00")
                .queryParam("unique", false)
                .build()
                .encode()
                .toUriString());

        ResponseEntity<String> response = restTemplate.getForEntity(requestUri, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Некорректный интервал времени"));
    }

    private void registerHit(String uri, String ip, LocalDateTime timestamp) {
        EndpointHit hit = new EndpointHit(APP_NAME, uri, ip, timestamp);
        ResponseEntity<Void> response = restTemplate.postForEntity("/hit", hit, Void.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    private void insertHit(String uri, String ip, LocalDateTime timestamp) {
        jdbcTemplate.update(
                "INSERT INTO hits (app, uri, ip, timestamp) VALUES (?, ?, ?, ?)",
                APP_NAME,
                uri,
                ip,
                Timestamp.valueOf(timestamp)
        );
    }

    private URI statisticsUri(boolean unique, List<String> uris) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", START.format(DATE_TIME_FORMATTER))
                .queryParam("end", END.format(DATE_TIME_FORMATTER))
                .queryParam("unique", unique);

        if (!uris.isEmpty()) {
            builder.queryParam("uris", uris.toArray());
        }

        return URI.create("http://localhost:" + port + builder.build().encode().toUriString());
    }
}
