package de.saschaufer.message_broker.app.file_storage.service;

import de.saschaufer.message_broker.app.file_storage.config.FileStorageProperties;
import de.saschaufer.message_broker.app.file_storage.service.dto.File;
import de.saschaufer.message_broker.common.Constants;
import de.saschaufer.message_broker.common.api.errorhandler.ResponseStatusException;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownload;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileStorageServiceTest {

    @TempDir
    private Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void beforeEach() {
        fileStorageService = new FileStorageService(new FileStorageProperties(tempDir));
    }

    @Test
    void saveAndReadFiles_positive() throws IOException {

        final Part part1 = createPart("1", "test1.txt", "test1", MediaType.TEXT_PLAIN, "d2d8cc4f369b340130bd2b29b8b54e918b7c260c3279176da9ccaa37c96eb71735fc97568e892dc6220bf4ae0d748edb46bd75622751556393be3f482e6f794e");
        final Part part2 = createPart("2", "test2.pdf", "test2", MediaType.APPLICATION_PDF, "e35970edaa1e0d8af7d948491b2da0450a49fd9cc1e83c5db4c6f175f9550cf341f642f6be8cfb0bfa476e4258e5088c5ad549087bf02811132ac2fa22b734c6");
        final Part part3 = createPart("3", "test3.png", "test3", MediaType.IMAGE_PNG, null);

        final List<File> files = fileStorageService.saveFiles("directoryId", List.of(
                part1, part2, part3
        ));

        assertThat(files.size(), is(3));

        assertThat(files.getFirst().id(), is("1"));
        assertThat(files.getFirst().fileId(), notNullValue());
        assertThat(files.getFirst().name(), is("test1.txt"));
        assertThat(files.getFirst().mediaType(), is(MediaType.TEXT_PLAIN));
        assertThat(files.getFirst().hash(), is("d2d8cc4f369b340130bd2b29b8b54e918b7c260c3279176da9ccaa37c96eb71735fc97568e892dc6220bf4ae0d748edb46bd75622751556393be3f482e6f794e"));

        assertThat(files.get(1).id(), is("2"));
        assertThat(files.get(1).fileId(), notNullValue());
        assertThat(files.get(1).name(), is("test2.pdf"));
        assertThat(files.get(1).mediaType(), is(MediaType.APPLICATION_PDF));
        assertThat(files.get(1).hash(), is("e35970edaa1e0d8af7d948491b2da0450a49fd9cc1e83c5db4c6f175f9550cf341f642f6be8cfb0bfa476e4258e5088c5ad549087bf02811132ac2fa22b734c6"));

        assertThat(files.getLast().id(), is("3"));
        assertThat(files.getLast().fileId(), notNullValue());
        assertThat(files.getLast().name(), is("test3.png"));
        assertThat(files.getLast().mediaType(), is(MediaType.IMAGE_PNG));
        assertThat(files.getLast().hash(), is("05697d8f12c7ffdb85064a7f9ddacfc7fc0e5d32642dcd25c3a613917d00607c7bed242deea2e44a256b7e4c189557395c1a9ea1ce5c6b2b0f5285b514fb3cb2"));

        assertThat(Files.readString(tempDir.resolve("directoryId").resolve(files.getFirst().fileId())), is("test1"));
        assertThat(Files.readString(tempDir.resolve("directoryId").resolve(files.get(1).fileId())), is("test2"));
        assertThat(Files.readString(tempDir.resolve("directoryId").resolve(files.getLast().fileId())), is("test3"));

        final String metadata = Files.readString(tempDir.resolve("directoryId").resolve("metadata.json"));
        assertThat(metadata, notNullValue());

        final List<HttpEntity<InputStreamResource>> httpEntities = fileStorageService.readFiles("directoryId", List.of(
                new FileDownload("1", files.getFirst().fileId()),
                new FileDownload("2", files.get(1).fileId()),
                new FileDownload("3", files.getLast().fileId())
        ));

        assertThat(httpEntities.getFirst().getHeaders().getContentDisposition().getName(), is(Constants.Http.BodyPart.FILE));
        assertThat(httpEntities.getFirst().getHeaders().getContentDisposition().getFilename(), is("test1.txt"));
        assertThat(httpEntities.getFirst().getHeaders().getContentType(), is(MediaType.TEXT_PLAIN));
        assertThat(httpEntities.getFirst().getHeaders().getFirst(Constants.Http.Header.FILE_ID), is("1"));
        assertThat(httpEntities.getFirst().getHeaders().getFirst(Constants.Http.Header.FILE_HASH), is("d2d8cc4f369b340130bd2b29b8b54e918b7c260c3279176da9ccaa37c96eb71735fc97568e892dc6220bf4ae0d748edb46bd75622751556393be3f482e6f794e"));
        assertThat(httpEntities.getFirst().getBody().getInputStream().readAllBytes(), is("test1".getBytes(StandardCharsets.UTF_8)));

        assertThat(httpEntities.get(1).getHeaders().getContentDisposition().getName(), is(Constants.Http.BodyPart.FILE));
        assertThat(httpEntities.get(1).getHeaders().getContentDisposition().getFilename(), is("test2.pdf"));
        assertThat(httpEntities.get(1).getHeaders().getContentType(), is(MediaType.APPLICATION_PDF));
        assertThat(httpEntities.get(1).getHeaders().getFirst(Constants.Http.Header.FILE_ID), is("2"));
        assertThat(httpEntities.get(1).getHeaders().getFirst(Constants.Http.Header.FILE_HASH), is("e35970edaa1e0d8af7d948491b2da0450a49fd9cc1e83c5db4c6f175f9550cf341f642f6be8cfb0bfa476e4258e5088c5ad549087bf02811132ac2fa22b734c6"));
        assertThat(httpEntities.get(1).getBody().getInputStream().readAllBytes(), is("test2".getBytes(StandardCharsets.UTF_8)));

        assertThat(httpEntities.getLast().getHeaders().getContentDisposition().getName(), is(Constants.Http.BodyPart.FILE));
        assertThat(httpEntities.getLast().getHeaders().getContentDisposition().getFilename(), is("test3.png"));
        assertThat(httpEntities.getLast().getHeaders().getContentType(), is(MediaType.IMAGE_PNG));
        assertThat(httpEntities.getLast().getHeaders().getFirst(Constants.Http.Header.FILE_ID), is("3"));
        assertThat(httpEntities.getLast().getHeaders().getFirst(Constants.Http.Header.FILE_HASH), is("05697d8f12c7ffdb85064a7f9ddacfc7fc0e5d32642dcd25c3a613917d00607c7bed242deea2e44a256b7e4c189557395c1a9ea1ce5c6b2b0f5285b514fb3cb2"));
        assertThat(httpEntities.getLast().getBody().getInputStream().readAllBytes(), is("test3".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void saveFiles_negative_HashDoesNotMatch() {

        final Part part1 = createPart("1", "test.txt", "test", MediaType.TEXT_PLAIN, "Doesn't match");

        final ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> fileStorageService.saveFiles("directoryId", List.of(
                part1
        )));

        assertThat(tempDir.toFile().list(), arrayWithSize(1));
        assertThat(tempDir.resolve("directoryId").toFile().list(), arrayWithSize(0));

        assertThat(e.getMessage(), is("Hash doesn't match."));
        assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(e.getAdditionalDetails(), aMapWithSize(4));
        assertThat(e.getAdditionalDetails().get("id"), is("1"));
        assertThat(e.getAdditionalDetails().get("file"), notNullValue());
        assertThat(e.getAdditionalDetails().get("hash-calculated"), is("9ece086e9bac491fac5c1d1046ca11d737b92a2b2ebd93f005d7b710110c0a678288166e7fbe796883a4f2e9b3ca9f484f521d0ce464345cc1aec96779149c14"));
        assertThat(e.getAdditionalDetails().get("hash-provided"), is("Doesn't match"));
    }

    private Part createPart(final String id, final String filename, final String content, final MediaType contentType, final String hash) {
        final MockPart part = new MockPart(Constants.Http.BodyPart.FILE, filename, content.getBytes(StandardCharsets.UTF_8), contentType);
        part.getHeaders().add(Constants.Http.Header.FILE_ID, id);
        part.getHeaders().add(Constants.Http.Header.FILE_HASH, hash);
        return part;
    }
}
