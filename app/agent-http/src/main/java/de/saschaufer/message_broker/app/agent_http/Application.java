package de.saschaufer.message_broker.app.agent_http;

import de.saschaufer.message_broker.app.agent_http.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
