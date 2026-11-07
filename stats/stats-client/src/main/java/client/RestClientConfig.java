package client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(StatsServerProperties.class)
public class RestClientConfig {
    @Bean
    public RestClient StatRestClient(StatsServerProperties statsServerProperties) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(statsServerProperties.connectTimeout())
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(statsServerProperties.readTimeout());

        return RestClient.builder()
                .baseUrl(statsServerProperties.baseUrl().toString())
                .build();
    }
}
