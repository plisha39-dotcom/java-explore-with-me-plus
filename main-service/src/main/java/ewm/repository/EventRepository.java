package ewm.repository;

import ewm.dto.EventFullDto;
import ewm.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiator_Id(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiator_Id(Long id, Long initiatorId);
}
