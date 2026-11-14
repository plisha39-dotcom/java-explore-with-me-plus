package ewm;

import client.StatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.stats.dto.EndpointHit;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication(scanBasePackages = {
        "ewm",
        "client"
})
public class MainApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MainApplication.class, args);

        //Проверка работы клиента и сервера
        StatClient statClient = context.getBean(StatClient.class);
        statClient.hit(new EndpointHit("App", "https://test.com", "127.0.0.1", LocalDateTime.now()));
        List<ViewStats> viewStats = statClient.get(
                "2026-11-01 08:00:00",
                "2026-11-10 18:00:00",
                List.of("https://test1", "https://test2"),
                true);
        System.out.println(viewStats);
    }
}
