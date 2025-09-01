package de.saschaufer.message_broker.app.file_storage.service.dto;

import org.springframework.http.MediaType;

public record File(
        String id,
        String fileId,
        String name,
        MediaType mediaType,
        String hash
) {
}
