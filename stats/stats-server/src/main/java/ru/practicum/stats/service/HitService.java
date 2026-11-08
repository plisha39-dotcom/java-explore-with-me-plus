package ru.practicum.stats.service;

import org.springframework.stereotype.Service;
import ru.practicum.stats.mapper.HitMapper;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.repository.HitRepository;

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
}
