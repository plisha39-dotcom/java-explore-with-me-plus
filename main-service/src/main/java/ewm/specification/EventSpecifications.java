package ewm.specification;

import ewm.model.Event;
import ewm.model.EventState;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public final class EventSpecifications {
    private EventSpecifications() {
    }

    public static Specification<Event> byUsers(List<Long> users) {
        return (root, query, cb) -> users == null || users.isEmpty()
                ? cb.conjunction()
                : root.get("initiator").get("id").in(users);
    }


    public static Specification<Event> byState(List<EventState> states) {
        return (root, query, cb) -> states == null || states.isEmpty()
                ? cb.conjunction()
                : root.get("state").in(states);
    }

    public static Specification<Event> byCategories(List<Long> categories) {
        return (root, query, cb) -> categories == null || categories.isEmpty()
                ? cb.conjunction()
                : root.get("category").get("id").in(categories);
    }

    public static Specification<Event> byDateRange(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            Predicate result = cb.conjunction();
            if (start != null) {
                result = cb.and(result, cb.greaterThanOrEqualTo(root.get("eventDate"), start));
            }
            if (end != null) {
                result = cb.and(result, cb.lessThanOrEqualTo(root.get("eventDate"), end));
            }
            return result;
        };
    }

    public static Specification<Event> build(List<Long> users, List<EventState> states,
                                             List<Long> categories, LocalDateTime start,
                                             LocalDateTime end) {
        return byUsers(users)
                .and(byState(states))
                .and(byCategories(categories))
                .and(byDateRange(start, end));
    }
}
