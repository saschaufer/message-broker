package de.saschaufer.message_broker.common.api.file_storage.dto;

public record FileResponse(
        String id,
        String fileId,
        String fileHash
) {
}
