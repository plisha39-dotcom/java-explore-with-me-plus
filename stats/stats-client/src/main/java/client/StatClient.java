package client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
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

    public EndpointHit hit(EndpointHit endpointHit) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(statsServerProperties.baseUrl().toString() + "/hits");
        try {

            return restClient.post()
                    .uri(uriComponentsBuilder.toUriString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(endpointHit)
                    .retrieve()
                    .body(EndpointHit.class);

        } catch (RestClientResponseException e) {

            log.error("HTTP status: {}", e.getStatusCode());
            log.error("Response: {}", e.getResponseBodyAsString());

            throw e;
        }
    }

    public List<ViewStats> get() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(statsServerProperties.baseUrl().toString() + "/stats");
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
