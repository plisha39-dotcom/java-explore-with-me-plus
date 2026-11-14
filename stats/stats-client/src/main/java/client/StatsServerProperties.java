package client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties(prefix = "stats.server")
public record StatsServerProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}