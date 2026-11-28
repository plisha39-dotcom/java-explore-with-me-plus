package ewm.repository;

import ewm.model.Category;
import ewm.model.Event;
import ewm.model.EventState;
import ewm.model.Location;
import ewm.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EventRepositoryTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    EntityManager entityManager;

    private Long user1Id;
    private Long user2Id;
    private Long eventId1;
    private Long eventId2;
    private Long categoryId;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.1")
                    .withDatabaseName("ewn")
                    .withUsername("user")
                    .withPassword("test");

    private User createUser(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@test.local");
        return userRepository.save(user);
    }

    private Event createEvent(User initiator, Category category) {
        Event event = new Event();
        event.setAnnotation("Тестовое событие для проверки репозитория");
        event.setDescription("Описание тестового события, длина которого больше двадцати символов");
        event.setTitle("Тестовое событие");
        event.setCreatedOn(LocalDateTime.now());
        event.setEventDate(LocalDateTime.now().plusDays(3));
        Location location = new Location();
        location.setLon(37.62);
        location.setLat(55.75);
        event.setLocation(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        return eventRepository.save(event);
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    @BeforeEach
    void setUp() {
        User user1 = createUser("Тест Один");
        user1Id = user1.getId();
        User user2 = createUser("Тест Два");
        user2Id = user2.getId();
        Category category = createCategory("Спорт");
        categoryId = category.getId();
        eventId1 = createEvent(user1, category).getId();
        eventId2 = createEvent(user2, category).getId();

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldSomeoneEventNotFind() {
        Optional<Event> found = eventRepository.findByIdAndInitiator_Id(eventId1, user2Id);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindEventByItsId() {
        Optional<Event> found = eventRepository.findByIdAndInitiator_Id(eventId1, user1Id);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(eventId1);
    }

    @Test
    void shouldReturnOnlyOwn() {
        List<Event> events = eventRepository.findAllByInitiator_Id(user1Id, PageRequest.of(0, 10));
        assertThat(events).hasSize(1);
        assertThat(events).extracting(Event::getId).containsExactly(eventId1);

        List<Event> second = eventRepository.findAllByInitiator_Id(user2Id, PageRequest.of(0, 10));
        assertThat(second).extracting(Event::getId).containsExactly(eventId2);
    }

    @Test
    void shouldSavingAndReadingBack() {
        Optional<Event> testEvent = eventRepository.findById(eventId1);
        Event event = testEvent.orElseThrow();
        assertThat(event.getTitle()).isEqualTo("Тестовое событие");
        assertThat(event.getAnnotation()).isEqualTo("Тестовое событие для проверки репозитория");
        assertThat(event.getEventDate()).isNotNull();
        assertThat(event.getLocation().getLat()).isEqualTo(55.75);
        assertThat(event.getLocation().getLon()).isEqualTo(37.62);
        assertThat(event.getCategory().getId()).isEqualTo(categoryId);
        assertThat(event.getInitiator().getId()).isEqualTo(user1Id);
        assertThat(event.getState()).isEqualTo(EventState.PENDING);
        assertThat(event.getPublishedOn()).isNull();
    }

    @Test
    void shouldPagination() {
        User user1 = userRepository.findById(user1Id).orElseThrow();
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        createEvent(user1, category);
        createEvent(user1, category);
        entityManager.flush();
        entityManager.clear();

        List<Event> events = eventRepository.findAllByInitiator_Id(user1Id, PageRequest.of(0, 2));

        assertThat(events).hasSize(2);

        List<Event> events1 = eventRepository.findAllByInitiator_Id(user1Id, PageRequest.of(1, 2));
        List<Long> firstPageIds = events.stream().map(Event::getId).toList();

        assertThat(events1).hasSize(1);
        assertThat(events1).extracting(Event::getId)
                              .doesNotContainAnyElementsOf(firstPageIds);

    }
}
