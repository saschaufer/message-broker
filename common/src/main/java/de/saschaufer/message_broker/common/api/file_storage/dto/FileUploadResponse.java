package de.saschaufer.message_broker.common.api.file_storage.dto;

import java.util.List;

public record FileUploadResponse(
        String directoryId,
        List<FileResponse> files
) {
}
