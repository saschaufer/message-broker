package de.saschaufer.message_broker.plugin.procedure.example;

import de.saschaufer.message_broker.plugin.spi.PluginInfo;
import de.saschaufer.message_broker.plugin.task.example_mapper.TaskInput;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@Slf4j
public class Procedure implements de.saschaufer.message_broker.plugin.spi.procedure.Procedure {
    public static final Marker ID = MarkerFactory.getMarker("example-procedure");
    public static final String ID_1 = "step-1";
    public static final String ID_2 = "step-2";
    public static final String ID_3 = "step-3";
    public static final String ID_4 = "step-4";
    public static final String ID_5 = "step-5";

    private final PluginInfo pluginInfo;

    public Procedure() {
        final String pluginVersion = getClass().getPackage().getImplementationVersion();

        final Manifest manifest = new Manifest();
        try {
            manifest.read(getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF"));
        } catch (IOException e) {
            throw new RuntimeException("Error reading manifest.", e);
        }

        final Attributes attributes = manifest.getMainAttributes();

        final String spiVersion = attributes.getValue("Plugin-Spi-Version");

        pluginInfo = new PluginInfo(ID.getName(), pluginVersion, spiVersion);
    }

    @Override
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    @Override
    public List<PluginInfo> getTaskPluginInfos() {

        // List the task of every step.
        return List.of(
                        getTaskPluginInfo(ID_1),
                        getTaskPluginInfo(ID_2),
                        getTaskPluginInfo(ID_3),
                        getTaskPluginInfo(ID_4),
                        getTaskPluginInfo(ID_5)
                )

                // Get every task only once.
                .stream()
                .distinct() // by .equals()
                .collect(Collectors.toList());
    }

    @Override
    public PluginInfo getTaskPluginInfo(final String stepId) {
        return switch (stepId) {
            case ID_1, ID_2, ID_3, ID_4, ID_5 -> new PluginInfo("example-task", "1.0.0-SNAPSHOT", "1.0.0");
            default -> throw new RuntimeException(String.format("Unknown step '%s'.", stepId));
        };
    }

    @Override
    public void init(final Path configFile) {
        log.atInfo().setMessage("Init procedure.").addKeyValue("config-file", configFile).addMarker(Procedure.PROCEDURE).addMarker(Procedure.ID).log();
        log.atInfo().setMessage("Procedure initialized.").addMarker(Procedure.PROCEDURE).addMarker(Procedure.ID).log();
    }

    @Override
    public void destroy() {
        log.atInfo().setMessage("Destroy procedure.").addMarker(Procedure.PROCEDURE).addMarker(Procedure.ID).log();
        log.atInfo().setMessage("Procedure destroyed.").addMarker(Procedure.PROCEDURE).addMarker(Procedure.ID).log();
    }

    @Override
    public String getNextStep(final String correlationId, final String previousStep, final String nextStep, final Map<String, Object> payload) throws Exception {

        if (nextStep != null) {
            return nextStep;
        }

        if (previousStep == null) {
            return ID_1;
        }

        return switch (previousStep) {
            case ID_1 -> ID_2;
            case ID_2 -> ID_3;
            case ID_3 -> ID_4;
            case ID_4 -> ID_5;
            case ID_5 -> null;
            default -> throw new Exception("Unknown step.");
        };
    }

    @Override
    public Map<String, Object> mapInput(final String correlationId, final String stepId, final String previousStep, final Map<String, Object> payload) throws Exception {

        final TaskInput input = switch (stepId) {
            case ID_1 -> new TaskInput(1, (String) payload.get("string"), Boolean.TRUE);
            case ID_2 -> new TaskInput(2, (String) payload.get("example-string"), Boolean.FALSE);
            case ID_3 -> new TaskInput(3, (String) payload.get("example-string"), Boolean.TRUE);
            case ID_4 -> new TaskInput(4, (String) payload.get("example-string"), Boolean.FALSE);
            case ID_5 -> new TaskInput(5, (String) payload.get("example-string"), Boolean.TRUE);
            default -> throw new Exception("Unknown step.");
        };

        return TaskInput.toMap(input);
    }

    @Override
    public Map<String, Object> mapOutput(final String correlationId, final String stepId, final String previousStep, final Map<String, Object> payload) throws Exception {
        return payload;
    }
}
