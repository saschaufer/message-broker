package de.saschaufer.message_broker.app.file_storage.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Validated
@ConfigurationProperties("file-storage")
public record FileStorageProperties(

        @NotNull
        Path path
) {
}
