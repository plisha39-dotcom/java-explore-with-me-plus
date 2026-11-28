package ewm.repository;

import ewm.model.ParticipationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Collection<ParticipationRequest> findByEventId(Long eventId);
}
