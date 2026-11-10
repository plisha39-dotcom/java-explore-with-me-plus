package ru.practicum.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class EndpointHit {
    private Long id;
    private String app;
    private String uri;
    private String ip;
    private String timestamp;

    public EndpointHit(String timestamp, String ip, String uri, String app) {
        this.timestamp = timestamp;
        this.ip = ip;
        this.uri = uri;
        this.app = app;
    }
}
