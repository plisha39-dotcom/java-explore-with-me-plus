package ewm.repository;

import ewm.model.Compilation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Long id);

    Page<Compilation> findAllByPinned(boolean pinned, Pageable pageable);

    @Query("select distinct c from Compilation c "
            + "left join fetch c.events e "
            + "left join fetch e.category "
            + "left join fetch e.initiator "
            + "where c.id in :ids")
    List<Compilation> findAllWithEventsByIdIn(@Param("ids") Collection<Long> ids);

    @Query("select distinct c from Compilation c "
            + "left join fetch c.events e "
            + "left join fetch e.category "
            + "left join fetch e.initiator "
            + "where c.id = :id")
    Optional<Compilation> findWithEventsById(@Param("id") Long id);
}
