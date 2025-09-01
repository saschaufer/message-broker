package de.saschaufer.message_broker.app.file_storage.api;

import de.saschaufer.message_broker.app.file_storage.service.FileStorageService;
import de.saschaufer.message_broker.app.file_storage.service.dto.File;
import de.saschaufer.message_broker.common.Constants;
import de.saschaufer.message_broker.common.api.errorhandler.ResponseStatusException;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownloadRequest;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileResponse;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileUploadResponse;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(FileStorageController.FILE_STORAGE_PATH_V1)
@RequiredArgsConstructor
public class FileStorageController implements FileStorageApi {

    public static final String FILE_STORAGE_PATH_V1 = "/file-storage";

    private final FileStorageService fileStorageService;

    public ResponseEntity<FileUploadResponse> postFiles(final String userAgent, final String directoryId, final List<Part> files) {

        log.atInfo().setMessage("Received request for upload files.").addKeyValue("user-agent", userAgent).log();

        if (userAgent.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Header '%s' must not be empty.", Constants.Http.Header.USER_AGENT));
        }

        final String dirId = directoryId != null ? directoryId : UUID.randomUUID().toString();
        if (dirId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Parameter '%s' must not be empty.", Constants.Http.Query.DIRECTORY_ID));
        }

        final List<File> filesOut = fileStorageService.saveFiles(dirId, files);

        final FileUploadResponse response = new FileUploadResponse(dirId, new ArrayList<>());
        for (final File file : filesOut) {
            response.files().add(new FileResponse(file.id(), file.fileId(), file.hash()));
        }

        log.atInfo().setMessage("Processed request for upload files successfully.").log();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<MultiValueMap<String, Object>> getFiles(final String userAgent, final FileDownloadRequest fileDownloadRequest) {

        log.atInfo().setMessage("Received request for download files.").addKeyValue("user-agent", userAgent).log();

        if (userAgent.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Header '%s' must not be empty.", Constants.Http.Header.USER_AGENT));
        }

        final List<HttpEntity<InputStreamResource>> httpEntities = fileStorageService.readFiles(fileDownloadRequest.directoryId(), fileDownloadRequest.files());

        final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.addAll(Constants.Http.BodyPart.FILE, httpEntities);

        log.atInfo().setMessage("Processed request for download files successfully.").log();

        return ResponseEntity.ok().contentType(MediaType.MULTIPART_FORM_DATA).body(body);
    }
}
