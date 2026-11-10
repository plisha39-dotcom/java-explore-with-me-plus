package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {
    @Query("""
            SELECT new ru.practicum.dto.ViewStats(h.app, h.uri,
                        CASE
                            WHEN :unique = true THEN COUNT(DISTINCT h.ip)
                            ELSE COUNT(h)
                        END)
                FROM Hit h
                WHERE h.timestamp BETWEEN :start AND :end
                GROUP BY h.app, h.uri
                ORDER BY
                        CASE
                            WHEN :unique = true THEN COUNT(DISTINCT h.ip)
                            ELSE COUNT(h)
                        END DESC
            """)
    List<ViewStats> noFilterByUris(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   @Param("unique") boolean unique);

    @Query("""
            SELECT new ru.practicum.dto.ViewStats(h.app, h.uri,
                        CASE
                            WHEN :unique = true THEN COUNT(DISTINCT h.ip)
                            ELSE COUNT(h)
                        END)
                FROM Hit h
                WHERE h.timestamp BETWEEN :start AND :end
                AND h.uri IN :uris
                GROUP BY h.app, h.uri
                ORDER BY
                        CASE
                            WHEN :unique = true THEN COUNT(DISTINCT h.ip)
                            ELSE COUNT(h)
                        END DESC
            """)
    List<ViewStats> filterByUris(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("uris") List<String> uris,
                                 @Param("unique") boolean unique);
}
