package ru.practicum.stats;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HitRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.1")
                    .withDatabaseName("stats")
                    .withUsername("user")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private HitRepository hitRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveHit() {
        Hit hit = new Hit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.100");
        hit.setTimestamp(LocalDateTime.of(2026, 11, 10, 14, 30, 0));

        Hit savedHit = hitRepository.save(hit);

        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedHit.getId());

        Optional<Hit> found = hitRepository.findById(savedHit.getId());
        Hit actualHit = found.orElseThrow();

        assertEquals("ewm-main-service", actualHit.getApp());
        assertEquals("/events/1", actualHit.getUri());
        assertEquals("192.168.1.100", actualHit.getIp());
        assertEquals(LocalDateTime.of(2026, 11, 10, 14, 30, 0),
                actualHit.getTimestamp());
    }

    @Test
    void shouldFilterHitsByTimeRange() {
        Hit hit = new Hit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.100");
        hit.setTimestamp(LocalDateTime.of(2026, 11, 10, 14, 30, 0));

        hitRepository.save(hit);

        Hit hit1 = new Hit();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/1");
        hit1.setIp("192.168.1.100");
        hit1.setTimestamp(LocalDateTime.of(2026, 11, 10, 15, 30, 0));

        hitRepository.save(hit1);

        Hit hit2 = new Hit();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/1");
        hit2.setIp("192.168.1.100");
        hit2.setTimestamp(LocalDateTime.of(2026, 11, 10, 16, 30, 0));

        hitRepository.save(hit2);

        entityManager.flush();
        entityManager.clear();

        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 15, 0);

        List<ViewStats> list = hitRepository.noFilterByUris(start, end, false);

        assertEquals(1, list.size());
        assertEquals(1L, list.getFirst().getHits());
    }

    @Test
    void shouldFilterHitsByUris() {
        Hit hit = new Hit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.100");
        hit.setTimestamp(LocalDateTime.of(2026, 11, 10, 14, 30, 0));

        hitRepository.save(hit);

        Hit hit1 = new Hit();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/2");
        hit1.setIp("192.168.1.100");
        hit1.setTimestamp(LocalDateTime.of(2026, 11, 10, 15, 30, 0));

        hitRepository.save(hit1);

        Hit hit2 = new Hit();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/3");
        hit2.setIp("192.168.1.100");
        hit2.setTimestamp(LocalDateTime.of(2026, 11, 10, 16, 30, 0));

        hitRepository.save(hit2);

        entityManager.flush();
        entityManager.clear();

        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 20, 0);
        List<String> uris = List.of("/events/1", "/events/3");
        List<ViewStats> actual = hitRepository.filterByUris(start, end, uris, false);

        assertEquals(2, actual.size());
        assertTrue(actual.stream()
                         .anyMatch(stat -> stat.getUri().equals("/events/1")));
        assertTrue(actual.stream()
                         .anyMatch(stat -> stat.getUri().equals("/events/3")));
        assertFalse(actual.stream()
                          .anyMatch(stat -> stat.getUri().equals("/events/2")));
    }

    @Test
    void shouldCountAllAndUniqueHits() {
        Hit hit = new Hit();
        hit.setApp("ewm-main-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.100");
        hit.setTimestamp(LocalDateTime.of(2026, 11, 10, 14, 30, 0));

        hitRepository.save(hit);

        Hit hit1 = new Hit();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/1");
        hit1.setIp("192.168.1.100");
        hit1.setTimestamp(LocalDateTime.of(2026, 11, 10, 15, 30, 0));

        hitRepository.save(hit1);

        Hit hit2 = new Hit();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/1");
        hit2.setIp("192.168.1.101");
        hit2.setTimestamp(LocalDateTime.of(2026, 11, 10, 16, 30, 0));

        hitRepository.save(hit2);

        Hit hit3 = new Hit();
        hit3.setApp("ewm-main-service");
        hit3.setUri("/events/1");
        hit3.setIp("192.168.1.102");
        hit3.setTimestamp(LocalDateTime.of(2026, 11, 10, 16, 30, 0));

        hitRepository.save(hit3);

        entityManager.flush();
        entityManager.clear();

        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 20, 0);
        List<ViewStats> listFalse = hitRepository.noFilterByUris(start, end, false);
        List<ViewStats> listTrue = hitRepository.noFilterByUris(start, end, true);

        assertEquals(1, listFalse.size());
        assertEquals(1, listTrue.size());
        assertEquals(4L, listFalse.getFirst().getHits());
        assertEquals(3L, listTrue.getFirst().getHits());
    }

    @Test
    void shouldGroupAndSortStatisticsByHits() {
        Hit hit1 = new Hit();
        hit1.setApp("ewm-main-service");
        hit1.setUri("/events/1");
        hit1.setIp("192.168.1.100");
        hit1.setTimestamp(LocalDateTime.of(2026, 11, 10, 15, 30, 0));

        hitRepository.save(hit1);

        Hit hit2 = new Hit();
        hit2.setApp("ewm-main-service");
        hit2.setUri("/events/1");
        hit2.setIp("192.168.1.101");
        hit2.setTimestamp(LocalDateTime.of(2026, 11, 10, 16, 30, 0));

        hitRepository.save(hit2);

        Hit hit3 = new Hit();
        hit3.setApp("ewm-main-service");
        hit3.setUri("/events/1");
        hit3.setIp("192.168.1.102");
        hit3.setTimestamp(LocalDateTime.of(2026, 11, 10, 16, 30, 0));

        hitRepository.save(hit3);

        Hit hit4 = new Hit();
        hit4.setApp("ewm-main-service");
        hit4.setUri("/events/2");
        hit4.setIp("192.168.1.100");
        hit4.setTimestamp(LocalDateTime.of(2026, 11, 10, 15, 30, 0));

        hitRepository.save(hit4);

        Hit hit5 = new Hit();
        hit5.setApp("ewm-main-service");
        hit5.setUri("/events/2");
        hit5.setIp("192.168.1.101");
        hit5.setTimestamp(LocalDateTime.of(2026, 11, 10, 16, 30, 0));

        hitRepository.save(hit5);

        Hit hit = new Hit();
        hit.setApp("another-service");
        hit.setUri("/events/1");
        hit.setIp("192.168.1.100");
        hit.setTimestamp(LocalDateTime.of(2026, 11, 10, 14, 30, 0));

        hitRepository.save(hit);

        entityManager.flush();
        entityManager.clear();

        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 10, 20, 0);
        List<ViewStats> list = hitRepository.noFilterByUris(start, end, false);

        assertEquals(3, list.size());
        assertEquals("/events/1", list.getFirst().getUri());
        assertEquals("ewm-main-service", list.getFirst().getApp());
        assertEquals("/events/2", list.get(1).getUri());
        assertEquals("ewm-main-service", list.get(1).getApp());
        assertEquals("/events/1", list.get(2).getUri());
        assertEquals("another-service", list.get(2).getApp());
        assertEquals(3L, list.getFirst().getHits());
        assertEquals(2L, list.get(1).getHits());
        assertEquals(1L, list.get(2).getHits());
    }
}
