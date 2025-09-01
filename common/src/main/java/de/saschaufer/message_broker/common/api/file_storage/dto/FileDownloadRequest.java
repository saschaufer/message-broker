package de.saschaufer.message_broker.common.api.file_storage.dto;

import java.util.List;

public record FileDownloadRequest(
        String directoryId,
        List<FileDownload> files
) {
}
