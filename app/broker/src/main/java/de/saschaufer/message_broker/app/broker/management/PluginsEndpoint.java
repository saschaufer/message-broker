package de.saschaufer.message_broker.app.broker.management;

import de.saschaufer.message_broker.app.broker.plugins.PluginManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Endpoint(id = "plugins")
@RequiredArgsConstructor
public class PluginsEndpoint {

    private final PluginManager pluginManager;

    @ReadOperation
    public Map<String, List<Map<String, Object>>> getPluginInfos() {
        final Map<String, List<Map<String, Object>>> pluginInfos = new HashMap<>();

        pluginInfos.put("tasks", pluginManager.getTasks().values().stream().map(
                task -> {

                    final Map<String, Object> map = new HashMap<>();
                    map.put("id", task.getPluginInfo().id());
                    map.put("version", task.getPluginInfo().version());
                    map.put("spi-version", task.getPluginInfo().spiVersion());

                    return map;
                }).collect(Collectors.toList())
        );

        pluginInfos.put("procedures", pluginManager.getProcedures().values().stream().map(
                procedure -> {

                    final Map<String, Object> map = new HashMap<>();
                    map.put("id", procedure.getPluginInfo().id());
                    map.put("version", procedure.getPluginInfo().version());
                    map.put("spi-version", procedure.getPluginInfo().spiVersion());
                    map.put("required-tasks", procedure.getTaskPluginInfos().stream().map(
                            taskPluginInfo -> {

                                final Map<String, Object> m = new HashMap<>();
                                m.put("id", taskPluginInfo.id());
                                m.put("version", taskPluginInfo.version());
                                m.put("spi-version", taskPluginInfo.spiVersion());

                                return m;
                            }).collect(Collectors.toList())
                    );

                    return map;
                }).collect(Collectors.toList())
        );

        return pluginInfos;
    }
}
