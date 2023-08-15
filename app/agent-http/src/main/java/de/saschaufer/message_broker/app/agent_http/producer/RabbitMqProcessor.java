package de.saschaufer.message_broker.app.agent_http.producer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.saschaufer.message_broker.app.agent_http.config.ApplicationProperties;
import de.saschaufer.message_broker.plugin.spi.Constants;
import de.saschaufer.message_broker.plugin.spi.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
public class RabbitMqProcessor {
    private final ApplicationProperties applicationProperties;

    public RabbitMqProcessor(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public Mono<Connection> createConnection() {

        final Address address = new Address(applicationProperties.rabbitMq().host(), applicationProperties.rabbitMq().port());

        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(applicationProperties.rabbitMq().user());
        connectionFactory.setPassword(applicationProperties.rabbitMq().password());
        connectionFactory.setVirtualHost(applicationProperties.rabbitMq().vhost());
        connectionFactory.useNio();  // <- with this flag our RabbitMq connection will be non-blocking
        connectionFactory.setAutomaticRecoveryEnabled(true);

        return Mono.fromCallable(() -> connectionFactory.newConnection(List.of(address), "rabbitmq"));
    }

    public Sender createRabbitMqSender(final Mono<Connection> connection) {

        final SenderOptions senderOptions = new SenderOptions()
                .connectionMono(connection)
                .resourceManagementScheduler(Schedulers.boundedElastic());

        final Sender sender = RabbitFlux.createSender(senderOptions);

        sender.declareExchange(new ExchangeSpecification()
                .name(applicationProperties.rabbitMq().exchange())
                .autoDelete(false)
                .durable(true)
                .type("direct")
                .arguments(Map.of(
                        "auto-declare", true,
                        "x-queue-type", "quorum",
                        "x-single-active-consumer", true
                ))
        ).subscribe();

        sender.declareQueue(new QueueSpecification()
                        .name(applicationProperties.rabbitMq().queue())
                        .autoDelete(false)
                        .durable(true)
                        .arguments(Map.of(
                                "auto-declare", true,
                                "x-queue-type", "quorum",
                                "x-single-active-consumer", false//true
                        ))
                //.passive()
                //.exclusive()
        ).subscribe();

        return sender;
    }

    public OutboundMessage createOutboundMessage(final Message message) {

        final AMQP.BasicProperties properties = new AMQP.BasicProperties(
                null, //String contentType
                null, //String contentEncoding
                null, //Map<String, Object> headers
                2, //Integer deliveryMode (1=non-persistent (default), 2=persistent)
                null, //Integer priority
                message.correlationId(), //String correlationId
                null, //String replyTo
                null, //String expiration
                null, //String messageId
                Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)), //Date timestamp
                null, //String type
                null, //String userId
                null, //String appId
                null  //String clusterId
        );

        return new OutboundMessage(
                applicationProperties.rabbitMq().exchange(),
                applicationProperties.rabbitMq().routingKey(),
                properties,
                json(message).get().getBytes()
        );
    }

    public WebClient createHealthWebClient() {
        return WebClient.builder()
                .filter(ExchangeFilterFunction.ofRequestProcessor(request -> {
                    log.atDebug().setMessage("RabbitMq Health Request.")
                            .addKeyValue(Constants.Logging.METHOD, request.method() + " " + request.url())
                            .log();
                    return Mono.just(request);
                }))
                .defaultHeader(HttpHeaders.AUTHORIZATION, String.format("Basic %s", new String(Base64.getEncoder().encode(String.format("%s:%s", applicationProperties.rabbitMq().user(), applicationProperties.rabbitMq().password()).getBytes()))))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl(String.format("http://%s:%s", applicationProperties.rabbitMq().host(), applicationProperties.rabbitMq().managementPort()))
                .build();
    }

    public Function<Object, Mono<Boolean>> runHealthCheck(final WebClient webClient) {
        return up -> webClient.get().uri(String.format("/api/aliveness-test/%s", applicationProperties.rabbitMq().vhost())).retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(RabbitMqStatus.class).map(status ->
                                new RuntimeException("Error on RabbitMq Health Check."))
                )
                .bodyToMono(RabbitMqStatus.class)
                .doOnNext(status -> log.atDebug().setMessage("RabbitMq Health Check.").addKeyValue(Constants.Logging.PAYLOAD, status).log())
                .map(status -> Boolean.TRUE)
                .onErrorResume(throwable -> {
                    log.atError().setMessage("Error on RabbitMq Health Check.").setCause(throwable).log();
                    return Mono.just(Boolean.FALSE);
                });
    }
}
