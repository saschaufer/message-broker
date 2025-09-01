package de.saschaufer.message_broker.app.file_storage.api;

import de.saschaufer.message_broker.app.file_storage.service.FileStorageService;
import de.saschaufer.message_broker.app.file_storage.service.dto.File;
import de.saschaufer.message_broker.common.Constants;
import de.saschaufer.message_broker.common.api.errorhandler.ResponseStatusException;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownloadRequest;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileResponse;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileUploadResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Handler {

    private final FileStorageService fileStorageService;

    public ServerResponse postFiles(final ServerRequest request) {

        log.atInfo().setMessage("Received request for upload files.").log();

        final MultiValueMap<String, Part> body;
        try {
            body = request.multipartData();
        } catch (final IOException | ServletException e) {
            log.atError().setMessage("Couldn't parse body as multipart request.").setCause(e).log();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Couldn't parse body as multipart request.", e);
        }

        if (!body.containsKey(Constants.Http.BodyPart.FILE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No body part '%s'.", Constants.Http.BodyPart.FILE));
        }

        final String directoryId = request.param(Constants.Http.Query.DIRECTORY_ID).orElse(UUID.randomUUID().toString());
        if (directoryId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Parameter '%s' must not be empty.", Constants.Http.Query.DIRECTORY_ID));
        }

        final List<File> files = fileStorageService.saveFiles(directoryId, body.get(Constants.Http.BodyPart.FILE));

        final FileUploadResponse response = new FileUploadResponse(
                directoryId,
                files.stream().map(f ->
                        new FileResponse(f.id(), f.fileId())
                ).toList());

        log.atInfo().setMessage("Processed request for upload files successfully.").log();

        return ServerResponse.ok().body(response);
    }

    public ServerResponse getFiles(final ServerRequest request) {

        log.atInfo().setMessage("Received request for download files.").log();

        final FileDownloadRequest fileDownloadRequest;
        try {
            fileDownloadRequest = request.body(FileDownloadRequest.class);
        } catch (final ServletException | IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Couldn't parse body.", e);
        }

        final List<HttpEntity<InputStreamResource>> httpEntities = fileStorageService.readFiles(fileDownloadRequest.directoryId(), fileDownloadRequest.files());

        final MultiValueMap<String, Object> body = httpEntities.stream()
                .map(e -> (Object) e)
                .collect(Collectors.groupingBy(e -> Constants.Http.BodyPart.FILE, Collectors.toList()))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, n) -> o, LinkedMultiValueMap::new));

        log.atInfo().setMessage("Processed request for download files successfully.").log();

        return ServerResponse.ok().contentType(MediaType.MULTIPART_FORM_DATA).body(body);
    }
}
