package de.saschaufer.message_broker.plugin.task.example;

import de.saschaufer.message_broker.plugin.spi.Constants;
import de.saschaufer.message_broker.plugin.spi.PluginInfo;
import de.saschaufer.message_broker.plugin.spi.task.Health;
import de.saschaufer.message_broker.plugin.task.example_mapper.TaskInput;
import de.saschaufer.message_broker.plugin.task.example_mapper.TaskOutput;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
public class Task implements de.saschaufer.message_broker.plugin.spi.task.Task {
    public static final Marker ID = MarkerFactory.getMarker("example-task");
    private final PluginInfo pluginInfo;

    public Task() {
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
    public void init(final Path configFile) {
        log.atInfo().setMessage("Init task.").addKeyValue("config-file", configFile).addMarker(Task.TASK).addMarker(Task.ID).log();
        Config.createInstance(configFile);
        log.atInfo().setMessage("Task initialized.").addMarker(Task.TASK).addMarker(Task.ID).log();
    }

    @Override
    public void destroy() {
        log.atInfo().setMessage("Destroy task.").addMarker(Task.TASK).addMarker(Task.ID).log();
        log.atInfo().setMessage("Task destroyed.").addMarker(Task.TASK).addMarker(Task.ID).log();
    }

    @Override
    public Health health(final Duration timeout) {
        return new Health(Health.Status.UP, Map.of(), List.of());
    }

    @Override
    public Map<String, Object> run(String correlationId, final Map<String, Object> payload) throws Exception {

        // Input
        log.atInfo().setMessage("Run task.")
                .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                .addKeyValue(Constants.Logging.PAYLOAD, json(payload)::get)
                .addMarker(TASK)
                .addMarker(ID)
                .log();

        final TaskInput taskInput = TaskInput.fromMap(payload);

        // Work
        log.atInfo().setMessage("Example property: {}")
                .addArgument(Config.instance().getExample())
                .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                .addMarker(TASK)
                .addMarker(ID)
                .log();

        final StringBuilder sb = new StringBuilder()
                .append("integer=").append(taskInput.integer()).append("; ")
                .append("string=").append(taskInput.string()).append("; ")
                .append("bool=").append(taskInput.bool()).append("; ")
                .append("example=").append(Config.instance().getExample());

        final TaskOutput taskOutput = new TaskOutput(sb.toString());

        // Output
        final Map<String, Object> output = TaskOutput.toMap(taskOutput);

        log.atInfo().setMessage("Task ran.")
                .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                .addKeyValue(Constants.Logging.PAYLOAD, json(output)::get)
                .addMarker(TASK)
                .addMarker(ID)
                .log();

        return output;
    }
}
