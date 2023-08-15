package de.saschaufer.message_broker.app.broker.plugins;

import de.saschaufer.message_broker.app.broker.config.ApplicationProperties;
import de.saschaufer.message_broker.plugin.spi.Plugin;
import de.saschaufer.message_broker.plugin.spi.PluginInfo;
import de.saschaufer.message_broker.plugin.spi.procedure.Procedure;
import de.saschaufer.message_broker.plugin.spi.task.Task;
import de.saschaufer.message_broker.utilities.JsonSupplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
@Getter
@Component
public class PluginManager {
    private final Map<String, Task> tasks = new HashMap<>();
    private final Map<String, Procedure> procedures = new HashMap<>();
    @Getter(AccessLevel.NONE)
    private static final List<String> sharedPackages = List.of(
            Plugin.class.getPackageName(),
            Marker.class.getPackageName(),
            Flux.class.getPackageName(),
            JsonSupplier.class.getPackageName()
    );
    @Getter(AccessLevel.NONE)
    private final ApplicationProperties applicationProperties;

    public PluginManager(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void loadPlugins() {
        tasks.putAll(loadTasks());
        procedures.putAll(loadProcedures(tasks));
    }

    public void destroyPlugins() {

        for (final Plugin task : tasks.values()) {
            task.destroy();
        }

        for (final Plugin procedure : procedures.values()) {
            procedure.destroy();
        }
    }

    private Map<String, Task> loadTasks() {

        log.atInfo().setMessage("Load task plugins.").addKeyValue("plugin-directory", applicationProperties.plugins().tasks()).log();

        final Map<String, Task> loadedTasks = applicationProperties.plugins().tasks().stream()
                .map(config -> {
                    final Task task;
                    try {
                        task = PluginLoader.load(Task.class, config.jar(), sharedPackages);
                        task.init(config.config());
                    } catch (final Exception e) {
                        throw new RuntimeException("Could not load task plugin.", e);
                    }
                    return task;
                }).collect(Collectors.toMap(task -> task.getPluginInfo().id(), Function.identity()));

        log.atInfo().setMessage("Task plugins loaded.")
                .addKeyValue("loaded-tasks", json(loadedTasks.values().stream().map(task -> task.getPluginInfo()).collect(Collectors.toList()))::get)
                .log();

        return loadedTasks;
    }

    private Map<String, Procedure> loadProcedures(final Map<String, Task> loadedTasks) {

        log.atInfo().setMessage("Load procedure plugins.").addKeyValue("plugin-directory", applicationProperties.plugins().procedures()).log();

        final Map<String, Procedure> loadedProcedures = applicationProperties.plugins().procedures().stream()
                .map(config -> {
                    final Procedure procedure;
                    try {
                        procedure = PluginLoader.load(Procedure.class, config.jar(), sharedPackages);
                        procedure.init(config.config());
                    } catch (final Exception e) {
                        throw new RuntimeException("Could not load procedure plugin.", e);
                    }
                    return procedure;
                })
                .map(procedure -> {

                    for (final PluginInfo procedureTaskInfo : procedure.getTaskPluginInfos()) {

                        final Task actualTask = loadedTasks.get(procedureTaskInfo.id());

                        if (actualTask == null) {
                            log.atError().setMessage("Task plugin not present.")
                                    .addKeyValue("procedure", procedure.getPluginInfo().id())
                                    .addKeyValue("task", procedureTaskInfo.id())
                                    .log();
                            throw new RuntimeException("Task plugin not present.");
                        }

                        final PluginInfo actualTaskInfo = actualTask.getPluginInfo();

                        if (!procedureTaskInfo.version().equals(actualTaskInfo.version())) {
                            log.atWarn().setMessage("The version of the task the procedure expected and the actual available task is different. This might lead to unexpected behaviour.")
                                    .addKeyValue("procedure", procedure.getPluginInfo().id())
                                    .addKeyValue("task", actualTaskInfo.id())
                                    .addKeyValue("expected-version", procedureTaskInfo.version())
                                    .addKeyValue("actual-version", actualTaskInfo.version())
                                    .log();
                        }

                        if (!procedureTaskInfo.spiVersion().equals(actualTaskInfo.spiVersion())) {
                            log.atError().setMessage("The SPI version of the task the procedure expected and the actual SPI version of the task are different.")
                                    .addKeyValue("procedure", procedure.getPluginInfo().id())
                                    .addKeyValue("task", actualTaskInfo.id())
                                    .addKeyValue("expected-spi-version", procedureTaskInfo.spiVersion())
                                    .addKeyValue("actual-spi-version", actualTaskInfo.spiVersion())
                                    .log();
                            throw new RuntimeException("The SPI version of the task the procedure expected and the actual SPI version of the task are different.");
                        }
                    }

                    return procedure;
                }).collect(Collectors.toMap(procedure -> procedure.getPluginInfo().id(), Function.identity()));

        log.atInfo().setMessage("Procedure plugins loaded.")
                .addKeyValue("loaded-procedures", json(loadedProcedures.values().stream().map(procedures -> procedures.getPluginInfo()).collect(Collectors.toList()))::get)
                .log();

        return loadedProcedures;
    }
}
