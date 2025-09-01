package de.saschaufer.message_broker.app.file_storage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import de.saschaufer.message_broker.app.file_storage.config.FileStorageProperties;
import de.saschaufer.message_broker.app.file_storage.service.dto.File;
import de.saschaufer.message_broker.app.file_storage.service.dto.Metadata;
import de.saschaufer.message_broker.common.Constants;
import de.saschaufer.message_broker.common.api.errorhandler.ResponseStatusException;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownload;
import de.saschaufer.message_broker.common.json.JsonUtils;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final String METADATA_FILENAME = "metadata.json";

    private final Object lock = new Object();

    private final FileStorageProperties fileStorageProperties;

    public List<File> saveFiles(final String directoryId, final List<Part> parts) {

        final Path path = getPath(directoryId);

        createDirectoryIfNotExists(path);

        final List<File> files = parts.stream().parallel()
                .map(part -> {

                    final String id = part.getHeader(Constants.Http.Header.FILE_ID);
                    if (id == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Missing header '%s'.", Constants.Http.Header.FILE_ID), Map.of(
                                "file", part.getName()
                        ));
                    }

                    final String hash = part.getHeader(Constants.Http.Header.FILE_HASH);
                    final String fileId = UUID.randomUUID().toString();
                    final String name = part.getSubmittedFileName();
                    final MediaType mediaType = MediaType.valueOf(part.getContentType());
                    final String hashHex;

                    try (final InputStream is = part.getInputStream()) {
                        final MessageDigest md = MessageDigest.getInstance("SHA3-512");
                        final DigestInputStream dis = new DigestInputStream(is, md);
                        Files.copy(dis, path.resolve(fileId + ".TMP"));
                        hashHex = HexFormat.of().formatHex(md.digest());
                    } catch (final Exception e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't write temporary file.", Map.of(
                                "id", id,
                                "file", path.resolve(fileId + ".TMP").toAbsolutePath().toString()
                        ), e);
                    }

                    if (hash != null && !hashHex.equals(hash)) {
                        try {
                            Files.delete(path.resolve(fileId + ".TMP"));
                        } catch (final IOException e) {
                            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't delete temporary file.", Map.of(
                                    "id", id,
                                    "file", path.resolve(fileId + ".TMP").toAbsolutePath().toString()
                            ), e);
                        }
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hash doesn't match.", Map.of(
                                "id", id,
                                "file", path.resolve(fileId + ".TMP").toAbsolutePath().toString(),
                                "hash-calculated", hashHex,
                                "hash-provided", hash
                        ));
                    }

                    try {
                        Files.move(path.resolve(fileId + ".TMP"), path.resolve(fileId), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't move temporary file.", Map.of(
                                "id", id,
                                "file-from", path.resolve(fileId + ".TMP").toAbsolutePath().toString(),
                                "file-to", path.resolve(fileId).toAbsolutePath().toString()
                        ), e);
                    }

                    return new File(id, fileId, name, mediaType, hashHex);
                })
                .toList();

        updateMetadataNewFiles(path, files);

        return files;
    }

    public List<HttpEntity<InputStreamResource>> readFiles(final String directoryId, final List<FileDownload> files) {

        final Path path = getPath(directoryId);

        final List<Metadata> allMetadata;
        synchronized (lock) {
            allMetadata = readMetadata(path.resolve(METADATA_FILENAME));
        }

        final List<Metadata> metadata = files.stream()
                .map(file -> allMetadata.stream()
                        .filter(md -> md.fileId().equals(file.fileId()))
                        .findFirst()
                        .orElseThrow(
                                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found.", Map.of(
                                        "fileId", file.fileId()
                                ))
                        )
                )
                .map(m -> new Metadata(m.fileId(), m.name(), m.mediaType(), m.hash(), m.uploaded(), OffsetDateTime.now()))
                .toList();

        final List<HttpEntity<InputStreamResource>> httpEntities = files.stream().parallel()
                .map(file -> {

                    final Metadata m = metadata.stream().filter(md -> md.fileId().equals(file.fileId())).findFirst().orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found.", Map.of(
                                    "fileId", file.fileId()
                            ))
                    );

                    final ContentDisposition contentDisposition = ContentDisposition
                            .builder("form-data")
                            .name(Constants.Http.BodyPart.FILE)
                            .filename(m.name())
                            .build();

                    final MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
                    fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
                    fileMap.add(HttpHeaders.CONTENT_TYPE, m.mediaType());
                    fileMap.add(Constants.Http.Header.FILE_ID, file.id());
                    fileMap.add(Constants.Http.Header.FILE_HASH, m.hash());

                    final InputStream inputStream;
                    try {
                        inputStream = Files.newInputStream(path.resolve(m.fileId()));
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't read file.", Map.of(
                                "file", m.fileId()
                        ), e);
                    }

                    return new HttpEntity<>(new InputStreamResource(inputStream), fileMap);
                })
                .toList();

        updateMetadataDownloadedFiles(directoryId, files.stream().map(FileDownload::fileId).toList(), allMetadata);

        return httpEntities;
    }

    private void createDirectoryIfNotExists(final Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (final IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't create directory.", Map.of(
                        "directory", path.toAbsolutePath().toString()
                ), e);
            }
        }
    }

    private void updateMetadataNewFiles(final Path path, final List<File> files) {
        final Path pathMetadata = path.resolve(METADATA_FILENAME);
        final List<Metadata> metadata = new ArrayList<>(files.stream()
                .map(f -> new Metadata(
                        f.fileId(),
                        f.name(),
                        f.mediaType().toString(),
                        f.hash(),
                        OffsetDateTime.now(),
                        null
                ))
                .toList());

        synchronized (lock) {
            metadata.addAll(readMetadata(pathMetadata));
            saveMetadata(pathMetadata, metadata);
        }
    }

    private void updateMetadataDownloadedFiles(final String directoryId, final List<String> fileIds, final List<Metadata> metadata) {
        final Path path = getPath(directoryId).resolve(METADATA_FILENAME);
        final List<Metadata> metadataList = metadata.stream()
                .map(m -> {
                    if (fileIds.contains(m.fileId())) {
                        return new Metadata(m.fileId(), m.name(), m.mediaType(), m.hash(), m.uploaded(), OffsetDateTime.now());
                    }
                    return m;
                })
                .toList();
        synchronized (lock) {
            saveMetadata(path, metadataList);
        }
    }

    private List<Metadata> readMetadata(final Path path) {
        final List<Metadata> metadata = new ArrayList<>();
        if (Files.exists(path)) {
            try {
                metadata.addAll(JsonUtils.fromJson(Files.readAllBytes(path), new TypeReference<List<Metadata>>() {
                }));
            } catch (final IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't read metadata.", e);
            }
        }
        return metadata;
    }

    private void saveMetadata(final Path path, final List<Metadata> metadata) {
        try {
            Files.writeString(path, JsonUtils.toJson(metadata), StandardCharsets.UTF_8);
        } catch (final JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't serialize metadata.", e);
        } catch (final IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't write file.", Map.of(
                    "file", METADATA_FILENAME
            ), e);
        }
    }

    private Path getPath(final String directoryId) {
        return fileStorageProperties.path().resolve(directoryId);
    }
}
