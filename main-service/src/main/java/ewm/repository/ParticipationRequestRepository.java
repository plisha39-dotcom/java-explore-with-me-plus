package ewm.repository;

import ewm.model.ParticipationRequest;
import ewm.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    boolean existsByRequester_Id(Long requesterId);

    @Query("select r.event.id as eventId, count(r.id) as confirmedCount "
            + "from ParticipationRequest r "
            + "where r.event.id in :eventIds and r.status = :status "
            + "group by r.event.id")
    List<EventConfirmedRequestCount> countByEventIdsAndStatus(
            @Param("eventIds") Collection<Long> eventIds,
            @Param("status") RequestStatus status);
}
