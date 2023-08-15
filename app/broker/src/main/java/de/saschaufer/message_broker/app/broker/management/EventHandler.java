package de.saschaufer.message_broker.app.broker.management;

import de.saschaufer.message_broker.app.broker.plugins.PluginManager;
import de.saschaufer.message_broker.app.broker.routing.Router;
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
    private final PluginManager pluginManager;
    private final Router router;

    public EventHandler(final UserAgent userAgent, final PluginManager pluginManager, final Router router) {
        this.userAgent = userAgent;
        this.pluginManager = pluginManager;
        this.router = router;
    }

    @EventListener(classes = ApplicationStartedEvent.class)
    void handleApplicationStartedEvent(final ApplicationStartedEvent event) {
        log.atInfo().setMessage("Started").log();
    }

    @EventListener(classes = ApplicationReadyEvent.class)
    void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        log.atInfo().setMessage("Ready. I am {}.").addArgument(userAgent.getUserAgent()).log();

        try {
            pluginManager.loadPlugins();
        } catch (final Exception e) {
            throw new RuntimeException("Error loading plugins.", e);
        }

        router.start();
    }

    @EventListener(classes = ContextClosedEvent.class)
    void handleContextClosedEvent(final ContextClosedEvent event) throws Exception {
        router.destroy();
        pluginManager.destroyPlugins();
        log.atInfo().setMessage("Closed").log();
    }

    @EventListener(classes = ApplicationFailedEvent.class)
    void handleApplicationFailedEvent(final ApplicationFailedEvent event) {
        log.atError().setMessage("Failed").log();
    }
}
