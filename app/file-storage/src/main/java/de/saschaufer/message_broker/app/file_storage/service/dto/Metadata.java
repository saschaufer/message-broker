package de.saschaufer.message_broker.app.file_storage.service.dto;

import java.time.OffsetDateTime;

public record Metadata(
        String fileId,
        String name,
        String mediaType,
        String hash,
        OffsetDateTime uploaded,
        OffsetDateTime lastDownloaded
) {
}
