package de.saschaufer.message_broker.plugin.spi.task;

import de.saschaufer.message_broker.plugin.spi.Plugin;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.Duration;
import java.util.Map;

public interface Task extends Plugin {
    Marker TASK = MarkerFactory.getMarker("task");

    Health health(final Duration timeout);

    Map<String, Object> run(final String correlationId, final Map<String, Object> payload) throws Exception;
}
