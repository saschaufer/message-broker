package de.saschaufer.message_broker.app.file_storage.config;

import de.saschaufer.message_broker.app.file_storage.service.FileStorageService;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;

public class FileStorageConfig {

    @Bean
    FileStorageService fileStorageService() {
        return new FileStorageService(Path.of("/var/lib/file-storage"));
    }
}
