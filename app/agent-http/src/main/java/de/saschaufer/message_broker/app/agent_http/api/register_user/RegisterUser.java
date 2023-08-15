package de.saschaufer.message_broker.app.agent_http.api.register_user;

import de.saschaufer.message_broker.app.agent_http.producer.RabbitMq;
import de.saschaufer.message_broker.plugin.spi.Constants;
import de.saschaufer.message_broker.plugin.spi.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;
import java.time.LocalDateTime;

@Slf4j
@RestController
public class RegisterUser implements RegisterUserApi {
    private final RabbitMq rabbitMq;

    public RegisterUser(final RabbitMq rabbitMq) {
        this.rabbitMq = rabbitMq;
    }

    public Mono<ResponseEntity<String>> postUser(final String correlationId, final User user) {

        return Mono.just(user)
                .doOnNext(u -> log.atInfo().setMessage("Request arrived at controller.")
                        .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                        .log()
                )
                .map(u -> new Message()
                        .correlationId(correlationId)
                        .procedure("example-procedure")
                        .payload(u.toMap())
                        .receptionTime(LocalDateTime.now())
                )
                .flatMap(message -> {
                    if (RabbitMq.isRunning()) {
                        return rabbitMq.send(message);
                    }
                    return Mono.error(new ServiceUnavailableException("RabbitMq is not reachable."));
                })
                .doOnNext(message -> log.atInfo().setMessage("Request has been successfully processed.")
                        .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                        .log()
                )
                .map(message -> ResponseEntity.ok()
                        .header(Constants.Http.Header.CORRELATION_ID, correlationId)
                        .body("Ok.")
                )
                ;
    }
}
