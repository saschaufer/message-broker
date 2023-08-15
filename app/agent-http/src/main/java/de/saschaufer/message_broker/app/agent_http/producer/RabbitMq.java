package de.saschaufer.message_broker.app.agent_http.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.rabbitmq.client.Connection;
import de.saschaufer.message_broker.app.agent_http.config.ApplicationProperties;
import de.saschaufer.message_broker.plugin.spi.Constants;
import de.saschaufer.message_broker.plugin.spi.Message;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.Sender;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;

import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
@Configuration
public class RabbitMq {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules() // To register jackson-datatype-jsr310 for parsing of LocalDateTime.
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
            .build();
    private final ApplicationProperties applicationProperties;
    private final RabbitMqProcessor rabbitMqProcessor;
    private final WebClient healthClient;
    private Mono<Connection> rabbitMqConnection;
    private Sender rabbitMqSender;
    private static boolean running = false;

    public static boolean isRunning() {
        return running;
    }

    public RabbitMq(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.rabbitMqProcessor = new RabbitMqProcessor(applicationProperties);
        healthClient = rabbitMqProcessor.createHealthWebClient();
        rabbitMqConnection = rabbitMqProcessor.createConnection();
    }

    public void start() {
        rabbitMqSender = rabbitMqProcessor.createRabbitMqSender(rabbitMqConnection);
        running = false;
        runHealthCheck();
    }

    public void stop() {
        running = false;
    }

    @PreDestroy
    private void destroy() {
        log.atInfo().setMessage("Destroy producer.").log();
        running = false;
        try {
            rabbitMqConnection.block().close();
        } catch (final Exception e) {
            log.atInfo().setMessage("Error destroying producer.").setCause(e).log();
        }
        log.atInfo().setMessage("Producer destroyed.").log();
    }

    public Mono<Message> send(final Message message) {

        return rabbitMqSender.bind(new BindingSpecification()
                        .exchange(applicationProperties.rabbitMq().exchange())
                        .queue(applicationProperties.rabbitMq().queue())
                        .routingKey(applicationProperties.rabbitMq().routingKey())
                )
                .thenMany(rabbitMqSender.sendWithPublishConfirms(Flux.just(message)
                        .doOnNext(m -> log.atDebug().setMessage("Send message to RabbitMq.").addKeyValue(Constants.Logging.PAYLOAD, m).log())
                        .map(m -> rabbitMqProcessor.createOutboundMessage(m))
                ))
                .flatMap(result -> {

                    if (!result.isAck()) {
                        log.atInfo().setMessage("Failed to sent message to RabbitMq.").addKeyValue(Constants.Logging.PAYLOAD, json(result.getOutboundMessage())::get).log();
                        return Mono.error(new Exception("Message not acknowledged by RabbitMq."));
                    }

                    log.atInfo().setMessage("Message successfully sent to RabbitMq.").addKeyValue(Constants.Logging.PAYLOAD, json(result.getOutboundMessage())::get).log();

                    final byte[] body = result.getOutboundMessage().getBody();

                    try {
                        return Mono.just(objectMapper.readValue(body, Message.class));
                    } catch (final IOException e) {
                        return Mono.error(new Exception("Failed to deserialize acknowledged message from RabbitMq.", e));
                    }
                }).last();
    }

    private void runHealthCheck() {

        Mono.just("")
                .flatMap(rabbitMqProcessor.runHealthCheck(healthClient))
                .map(up -> {
                    running = up;
                    return Mono.empty();
                })
                .delaySubscription(Duration.ofSeconds(1))
                .repeat()
                .subscribe();
    }
}
