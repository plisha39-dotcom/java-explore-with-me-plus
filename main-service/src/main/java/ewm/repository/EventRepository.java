package ewm.repository;

import ewm.model.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>,
        JpaSpecificationExecutor<Event> {
    List<Event> findAllByInitiator_Id(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiator_Id(Long id, Long initiatorId);

    boolean existsByCategory_Id(Long categoryId);

    boolean existsByInitiator_Id(Long initiatorId);

    @Query("select distinct e from Event e "
            + "join fetch e.category "
            + "join fetch e.initiator "
            + "where e.id in :ids")
    List<Event> findAllWithCategoryAndInitiatorByIdIn(@Param("ids") Collection<Long> ids);
}
