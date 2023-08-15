package de.saschaufer.message_broker.plugin.spi.procedure;

import de.saschaufer.message_broker.plugin.spi.Plugin;
import de.saschaufer.message_broker.plugin.spi.PluginInfo;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.List;
import java.util.Map;

public interface Procedure extends Plugin {
    Marker PROCEDURE = MarkerFactory.getMarker("procedure");

    List<PluginInfo> getTaskPluginInfos();

    PluginInfo getTaskPluginInfo(final String stepId);

    String getNextStep(final String correlationId, final String previousStep, final String nextStep, final Map<String, Object> payload) throws Exception;

    Map<String, Object> mapInput(final String correlationId, final String stepId, final String previousStep, final Map<String, Object> payload) throws Exception;

    Map<String, Object> mapOutput(final String correlationId, final String stepId, final String previousStep, final Map<String, Object> payload) throws Exception;
}
