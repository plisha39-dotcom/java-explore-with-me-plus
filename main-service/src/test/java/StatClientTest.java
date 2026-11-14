import client.RestClientConfig;
import client.StatClient;
import client.StatsServerProperties;
import ewm.MainApplication;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = MainApplication.class)
public class StatClientTest {

    @Autowired
    private StatClient statClient;
    @Autowired
    private RestClientConfig restClientConfig;
    @Autowired
    private StatsServerProperties statsServerProperties;

    @Test
    public void hitShouldSaveHit() {
        String uri = "https://test.com";
        statClient.hit(new EndpointHit("App", uri, "127.0.0.1", LocalDateTime.now()));

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<ViewStats> viewStats = statClient.get(
                now.minusDays(1).format(formatter),
                now.plusDays(1).format(formatter),
                List.of(uri),
                true);

        MatcherAssert.assertThat(viewStats, Matchers.empty());

        ViewStats result = viewStats.getFirst();

        MatcherAssert.assertThat(result.getApp(), Matchers.equalTo("App"));
        MatcherAssert.assertThat(result.getUri(), Matchers.equalTo("https://test.com"));
    }
}
