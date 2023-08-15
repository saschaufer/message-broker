package de.saschaufer.message_broker.app.agent_http.management;

import de.saschaufer.message_broker.app.agent_http.producer.RabbitMq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventHandler {
    private final UserAgent userAgent;
    private final RabbitMq rabbitMq;

    public EventHandler(final UserAgent userAgent, final RabbitMq rabbitMq) {
        this.userAgent = userAgent;
        this.rabbitMq = rabbitMq;
    }

    @EventListener(classes = ApplicationStartedEvent.class)
    void handleApplicationStartedEvent(final ApplicationStartedEvent event) {
        log.atInfo().setMessage("Started").log();
    }

    @EventListener(classes = ApplicationReadyEvent.class)
    void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        log.atInfo().setMessage("Ready. I am {}.").addArgument(userAgent.getUserAgent()).log();
        rabbitMq.start();
    }

    @EventListener(classes = ContextClosedEvent.class)
    void handleContextClosedEvent(final ContextClosedEvent event) {

        rabbitMq.stop();
        log.atInfo().setMessage("Closed").log();
    }

    @EventListener(classes = ApplicationFailedEvent.class)
    void handleApplicationFailedEvent(final ApplicationFailedEvent event) {
        log.atError().setMessage("Failed").log();
    }
}
