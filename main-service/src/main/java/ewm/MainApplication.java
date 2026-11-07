package ewm;

import client.StatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MainApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MainApplication.class, args);

        StatClient statClient = context.getBean(StatClient.class);
        statClient.hit(new HitDto());
        StatDto statDto = statClient.get(new ParamDto());
        System.out.println(statDto);
    }
}
