package ru.practicum.stats.service;

import org.springframework.stereotype.Service;
import ru.practicum.stats.exception.InvalidTimeRangeException;
import ru.practicum.stats.mapper.HitMapper;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HitService {
    private final HitRepository hitRepository;

    public HitService(HitRepository hitRepository) {
        this.hitRepository = hitRepository;
    }

    public Hit save(EndpointHit endpointHit) {
        Hit hit = HitMapper.toHit(endpointHit);
        return hitRepository.save(hit);
    }

    public List<ViewStats> getStatistics(LocalDateTime start, LocalDateTime end,
                                         List<String> uris, boolean unique) {
        List<ViewStats> stats;
        if (start.isAfter(end)) {
            throw new InvalidTimeRangeException("Неккоректный интервал времени");
        }
        if (uris == null || uris.isEmpty()) {
            stats = hitRepository.noFilterByUris(start, end, unique);
        } else {
            stats = hitRepository.filterByUris(start, end, uris, unique);
        }
        return stats;
    }
}
