package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.stats.model.Hit;

public interface HitRepository extends JpaRepository<Hit, Long> {
}
