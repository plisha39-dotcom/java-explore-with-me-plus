package client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.util.List;

@Slf4j
@Component
public class StatClient {
    private final RestClient restClient;
    private final StatsServerProperties statsServerProperties;

    public StatClient(RestClient restClient, StatsServerProperties statsServerProperties) {
        this.restClient = restClient;
        this.statsServerProperties = statsServerProperties;
    }

    public ResponseEntity<Void> hit(EndpointHit endpointHit) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(statsServerProperties.baseUrl().toString() + "/hit");
        try {

            return restClient.post()
                    .uri(uriComponentsBuilder.toUriString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(endpointHit)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException e) {

            log.error("HTTP status: {}", e.getStatusCode());
            log.error("Response: {}", e.getResponseBodyAsString());

            throw e;
        }
    }

    public List<ViewStats> get(String start, String end, List<String> uris, boolean unique) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(statsServerProperties.baseUrl().toString() + "/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique);
        try {
            return restClient.get()
                    .uri(uriComponentsBuilder.toUriString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ViewStats>>() {
                    });
        } catch (RestClientResponseException e) {

            log.error("HTTP status: {}", e.getStatusCode());
            log.error("Response: {}", e.getResponseBodyAsString());

            throw e;
        }
    }
}
