package de.saschaufer.message_broker.app.broker.plugins;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@Slf4j
public class PluginLoader {

    public static <T> T load(final Class<T> type, final Path plugin, final List<String> sharedPackages) throws Exception {

        if (!Files.exists(plugin)) {
            throw new Exception(String.format("Plugin '%s' does not exist.", plugin));
        }

        final URL url = plugin.toUri().toURL();

        log.atInfo().setMessage("Load plugin.").addKeyValue("plugin", url.getPath()).log();

        final List<T> plugins = new ArrayList<>();

        final URLClassLoader pluginClassLoader = new PluginClassLoader(new URL[]{url}, PluginLoader.class.getClassLoader(), sharedPackages);
        final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(pluginClassLoader);
            for (final T t : ServiceLoader.load(type, pluginClassLoader)) {
                plugins.add(t);
            }
        } catch (final Exception e) {
            throw new Exception(String.format("Plugin '%s' could not be loaded.", url.getPath()), e);
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }

        if (plugins.isEmpty()) {
            throw new Exception(String.format("No plugin was loaded in '%s'.", url.getPath()));
        }

        if (plugins.size() > 1) {
            throw new Exception(String.format("Expected only one plugin '%s' but was %d.", url.getPath(), plugins.size()));
        }

        final T p = plugins.get(0);

        log.atInfo().setMessage("Plugin loaded.").addKeyValue("plugin", url.getPath()).log();

        return p;
    }
}
