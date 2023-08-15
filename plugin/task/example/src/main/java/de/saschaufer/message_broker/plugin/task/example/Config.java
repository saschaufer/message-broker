package de.saschaufer.message_broker.plugin.task.example;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Getter
public class Config {
    private static Config INSTANCE;
    private String example;

    private Config() {
    }

    public static void createInstance(final Path path) {

        log.atInfo().setMessage("Load configs.").addKeyValue("path", path).addMarker(Task.TASK).addMarker(Task.ID).log();

        if (INSTANCE != null) {
            final RuntimeException e = new RuntimeException("Cannot create instance twice.");
            log.atError().setMessage("Error loading configs.").setCause(e).addMarker(Task.TASK).addMarker(Task.ID).log();
            throw e;
        }

        final Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        yaml.setBeanAccess(BeanAccess.FIELD); // Access class members without setters.

        try (final InputStream is = Files.newInputStream(path.toAbsolutePath())) {
            INSTANCE = yaml.load(is);
        } catch (final IOException e) {
            log.atError().setMessage("Error loading configs.").setCause(e).addMarker(Task.TASK).addMarker(Task.ID).log();
            throw new RuntimeException(e);
        }

        log.atInfo().setMessage("Configs loaded.").addMarker(Task.TASK).addMarker(Task.ID).log();
    }

    public static Config instance() {

        if (INSTANCE == null) {
            final RuntimeException e = new RuntimeException("Instance must be created first.");
            log.atError().setMessage("Error getting config instance.").setCause(e).addMarker(Task.TASK).addMarker(Task.ID).log();
            throw e;
        }

        return INSTANCE;
    }
}
