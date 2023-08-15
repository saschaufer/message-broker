package de.saschaufer.message_broker.plugin.spi;

import java.nio.file.Path;

public interface Plugin {
    PluginInfo getPluginInfo();

    void init(final Path configFile) throws Exception;

    void destroy();
}
