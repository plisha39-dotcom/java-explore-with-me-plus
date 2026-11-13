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
        statClient.hit(new EndpointHit("App", "https://test.com","127.0.0.1", LocalDateTime.now()));
        List<ViewStats> viewStats = statClient.get();
        System.out.println(viewStats);

    }
}
