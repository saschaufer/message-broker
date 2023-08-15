package de.saschaufer.message_broker.app.broker.routing;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.saschaufer.message_broker.app.broker.config.ApplicationProperties;
import de.saschaufer.message_broker.plugin.spi.Constants;
import de.saschaufer.message_broker.plugin.spi.Message;
import de.saschaufer.message_broker.utilities.Ksuid;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
@Component
public class RabbitMq {
    private final ApplicationProperties applicationProperties;
    private Mono<Connection> rabbitMqConnection;
    private Receiver rabbitMqReceiver;

    public RabbitMq(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        rabbitMqConnection = createConnection();
        rabbitMqReceiver = createRabbitMqReceiver(rabbitMqConnection);
    }

    @PreDestroy
    private void destroy() {
        log.atInfo().setMessage("Destroy consumer.").log();
        try {
            rabbitMqConnection.block().close();
        } catch (final Exception e) {
            log.atInfo().setMessage("Error destroying consumer.").setCause(e).log();
        }
        log.atInfo().setMessage("Consumer destroyed.").log();
    }

    public Flux<Message> receive() {

        return rabbitMqReceiver.consumeNoAck(applicationProperties.rabbitMq().queue())
                .doOnNext(m -> log.atInfo().setMessage("Receiver message.").addKeyValue(Constants.Logging.PAYLOAD, json(m)::get).log())
                .map(m -> {

                    final String ksuid = Ksuid.generate();
                    final String procedure = "example-procedure";
                    final Map<String, Object> payload = Map.of("body", new String(m.getBody()));
                    final LocalDateTime receptionTime = LocalDateTime.now();

                    return new Message(ksuid, procedure, payload, receptionTime);
                })
                .doOnNext(m -> log.atInfo().setMessage("Message created.")
                        .addKeyValue(Constants.Logging.CORRELATION_ID, m.correlationId())
                        .addKeyValue(Constants.Logging.PAYLOAD, json(m)::get)
                        .log()
                )
                ;
    }

    private Mono<Connection> createConnection() {

        final Address address = new Address(applicationProperties.rabbitMq().host(), applicationProperties.rabbitMq().port());

        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(applicationProperties.rabbitMq().user());
        connectionFactory.setPassword(applicationProperties.rabbitMq().password());
        connectionFactory.setVirtualHost(applicationProperties.rabbitMq().vhost());
        connectionFactory.useNio();  // <- with this flag our RabbitMq connection will be non-blocking
        connectionFactory.setAutomaticRecoveryEnabled(true);
        return Mono.fromCallable(() -> connectionFactory.newConnection(List.of(address), "rabbitmq"));
    }

    private Receiver createRabbitMqReceiver(final Mono<Connection> connection) {
        final ReceiverOptions receiverOptions = new ReceiverOptions()
                .connectionMono(connection)
                .connectionSubscriptionScheduler(Schedulers.boundedElastic());

        return RabbitFlux.createReceiver(receiverOptions);
    }
}
