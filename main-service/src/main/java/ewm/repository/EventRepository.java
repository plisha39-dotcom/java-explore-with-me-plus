package ewm.repository;

import ewm.model.Event;
import ewm.model.EventState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("""
            SELECT e
            FROM Event e
            WHERE (
                LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))
                OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
            )
            AND e.paid = :paid
            AND e.publishedOn = :publishedOn
            AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
            AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
            AND e.state = :state
            """)
    List<Event> searchEventsWithoutCategories(
            @Param("text") String text,
            @Param("paid") boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("state") EventState state
    );

    @Query("""
            SELECT e
            FROM Event e
            WHERE (
                LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))
                OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
            )
            AND e.paid = :paid
            AND e.publishedOn = :publishedOn
            AND e.category.id IN :categories
            AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
            AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
            AND e.state = :state
            """)
    List<Event> searchEvents(
            @Param("text") String text,
            @Param("categories") Collection<Integer> categories,
            @Param("paid") boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("state") EventState state
    );
}
