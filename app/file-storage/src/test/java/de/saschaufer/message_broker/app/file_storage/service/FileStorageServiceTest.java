package de.saschaufer.message_broker.app.file_storage.service;

import de.saschaufer.message_broker.app.file_storage.service.dto.File;
import de.saschaufer.message_broker.common.Constants;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownload;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class FileStorageServiceTest {

    @TempDir
    private Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void beforeEach() {
        fileStorageService = new FileStorageService(tempDir);
    }

    @Test
    void saveAndReadFiles() throws IOException {

        final Part part1 = createPart("1", "test1.txt", "test1", MediaType.TEXT_PLAIN);
        final Part part2 = createPart("2", "test2.pdf", "test2", MediaType.APPLICATION_PDF);

        final List<File> files = fileStorageService.saveFiles("directoryId", List.of(
                part1, part2
        ));

        assertThat(files.size(), is(2));

        assertThat(files.getFirst().id(), is("1"));
        assertThat(files.getFirst().fileId(), notNullValue());
        assertThat(files.getFirst().name(), is("test1.txt"));
        assertThat(files.getFirst().mediaType(), is(MediaType.TEXT_PLAIN));

        assertThat(files.getLast().id(), is("2"));
        assertThat(files.getLast().fileId(), notNullValue());
        assertThat(files.getLast().name(), is("test2.pdf"));
        assertThat(files.getLast().mediaType(), is(MediaType.APPLICATION_PDF));

        assertThat(Files.readString(tempDir.resolve("directoryId").resolve(files.getFirst().fileId())), is("test1"));
        assertThat(Files.readString(tempDir.resolve("directoryId").resolve(files.getLast().fileId())), is("test2"));

        final String metadata = Files.readString(tempDir.resolve("directoryId").resolve("metadata.json"));
        assertThat(metadata, notNullValue());

        final List<HttpEntity<InputStreamResource>> httpEntities = fileStorageService.readFiles("directoryId", List.of(
                new FileDownload("1", files.getFirst().fileId()),
                new FileDownload("2", files.getLast().fileId())
        ));

        assertThat(httpEntities.getFirst().getHeaders().getContentDisposition().getName(), is(Constants.Http.BodyPart.FILE));
        assertThat(httpEntities.getFirst().getHeaders().getContentDisposition().getFilename(), is("test1.txt"));
        assertThat(httpEntities.getFirst().getHeaders().getContentType(), is(MediaType.TEXT_PLAIN));
        assertThat(httpEntities.getFirst().getHeaders().getFirst(Constants.Http.Header.FILE_ID), is("1"));
        assertThat(httpEntities.getFirst().getBody().getInputStream().readAllBytes(), is("test1".getBytes(StandardCharsets.UTF_8)));

        assertThat(httpEntities.getLast().getHeaders().getContentDisposition().getName(), is(Constants.Http.BodyPart.FILE));
        assertThat(httpEntities.getLast().getHeaders().getContentDisposition().getFilename(), is("test2.pdf"));
        assertThat(httpEntities.getLast().getHeaders().getContentType(), is(MediaType.APPLICATION_PDF));
        assertThat(httpEntities.getLast().getHeaders().getFirst(Constants.Http.Header.FILE_ID), is("2"));
        assertThat(httpEntities.getLast().getBody().getInputStream().readAllBytes(), is("test2".getBytes(StandardCharsets.UTF_8)));
    }

    private Part createPart(final String id, final String filename, final String content, final MediaType contentType) {
        final MockPart part = new MockPart(Constants.Http.BodyPart.FILE, filename, content.getBytes(StandardCharsets.UTF_8), contentType);
        part.getHeaders().add(Constants.Http.Header.FILE_ID, id);
        return part;
    }
}
