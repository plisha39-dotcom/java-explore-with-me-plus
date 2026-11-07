package client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class StatClient {
    private final RestClient restClient;
    private String statUrl;

    public StatClient(RestClient restClient, @Value("http://localhost:9090") String statUrl) {
        this.restClient = restClient;
        this.statUrl = statUrl;
    }

    public void hit(HitDto hitDto) {

    }

    public List<StatDto> get() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(statUrl+"/stats");
        return null;
    }
}