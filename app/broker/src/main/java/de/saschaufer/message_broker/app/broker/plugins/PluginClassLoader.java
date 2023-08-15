package de.saschaufer.message_broker.app.broker.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class PluginClassLoader extends URLClassLoader {
    private final List<String> sharedPackages;
    private final ClassLoader parentClassLoader;

    public PluginClassLoader(final URL[] urls, final ClassLoader parentClassLoader, final List<String> sharedPackages) {
        super(urls, null);
        this.parentClassLoader = parentClassLoader;
        this.sharedPackages = sharedPackages;
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve)
            throws ClassNotFoundException {

        Class<?> loadedClass = findLoadedClass(name);

        if (loadedClass == null) {

            // Is the class in one of those packages or in a subpackage then it is shard with the app and other plugins.
            final boolean isSharedClass = sharedPackages.stream().anyMatch(name::startsWith);

            if (isSharedClass) {
                loadedClass = parentClassLoader.loadClass(name);
            } else {
                loadedClass = super.loadClass(name, resolve);
            }
        }

        if (resolve) {
            resolveClass(loadedClass);
        }

        return loadedClass;
    }
}
