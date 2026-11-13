package ru.practicum.stats.mapper;

import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.model.Hit;

public class HitMapper {
    public static Hit toHit(EndpointHit endpointHit) {
        Hit hit = new Hit();
        hit.setApp(endpointHit.getApp());
        hit.setIp(endpointHit.getIp());
        hit.setUri(endpointHit.getUri());
        hit.setTimestamp(endpointHit.getTimestamp());
        return hit;
    }
}
