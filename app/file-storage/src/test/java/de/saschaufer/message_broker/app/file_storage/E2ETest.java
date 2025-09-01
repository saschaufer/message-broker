package de.saschaufer.message_broker.app.file_storage;

import de.saschaufer.message_broker.app.file_storage.api.FileStorageController;
import de.saschaufer.message_broker.app.file_storage.config.FileStorageProperties;
import de.saschaufer.message_broker.app.file_storage.service.FileStorageService;
import de.saschaufer.message_broker.common.Constants;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownload;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownloadRequest;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileUploadResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:application.yml")
class E2ETest {

    @TempDir
    private static Path tempDir;

    @Autowired
    private RestTemplate restTemplate;

    @Lazy
    @TestConfiguration
    static class TestConfig {

        @Bean
        FileStorageService fileStorageService() {
            return new FileStorageService(new FileStorageProperties(tempDir));
        }

        @Bean
        RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder, @Value("${local.server.port}") int port) {
            return restTemplateBuilder
                    .connectTimeout(Duration.ofMillis(1000))
                    .readTimeout(Duration.ofMillis(1000))
                    .rootUri("http://localhost:" + port)
                    .build();
        }
    }

    @Test
    void postFiles_getFiles_positive_Multithreaded() throws ExecutionException, InterruptedException, TimeoutException, IOException {

        final List<Future<String>> directoryIdFutures = new ArrayList<>();

        final int numThreads = 200;
        try (final ExecutorService executorService = Executors.newFixedThreadPool(Math.min(numThreads, 10))) {
            for (int i = 0; i < numThreads; i++) {
                // Wait a bit to mix uploads and downloads
                Thread.sleep(i % 2 == 0 ? 10 : 0);
                directoryIdFutures.add(executorService.submit(new Call(i)));
            }
        }

        final Set<String> directoryIds = new HashSet<>();

        for (final Future<String> future : directoryIdFutures) {
            final String directoryId = future.get(10, TimeUnit.SECONDS);
            if (directoryId != null) {
                directoryIds.add(directoryId);
            }
        }

        assertThat(directoryIds, hasSize(numThreads));
    }

    private class Call implements Callable<String> {

        final int counter;
        final String body;

        public Call(final int counter) {
            this.counter = counter;
            body = """
                    --my-boundary\r
                    Content-Disposition: form-data; name="::file"; filename="file-1-::counter.txt"\r
                    Content-Type: text/plain\r
                    ::file-id: 1\r
                    ::file-hash-1\r
                    \r
                    ::body-1\r
                    --my-boundary\r
                    Content-Disposition: form-data; name="::file"; filename="file-2-::counter.pdf"\r
                    Content-Type: application/pdf\r
                    ::file-id: 2\r
                    ::file-hash-2\r
                    \r
                    ::body-2\r
                    --my-boundary--\r
                    """
                    .replace("::file-id", Constants.Http.Header.FILE_ID)
                    .replace("::file-hash-1", Constants.Http.Header.FILE_HASH + ": " + calcHash("file-1-" + counter))
                    .replace("::file-hash-2", Constants.Http.Header.FILE_HASH + ": " + calcHash("file-2-" + counter))
                    .replace("::body-1", "file-1-" + counter)
                    .replace("::body-2", "file-2-" + counter)
                    .replace("::file", Constants.Http.BodyPart.FILE);
        }

        @Override
        public String call() {

            final ResponseEntity<FileUploadResponse> responseUpload = uploadFiles();

            if (responseUpload.getBody() == null) {
                throw new RuntimeException("No body in response");
            }

            final ResponseEntity<String> responseDownload = downloadFiles(responseUpload.getBody());

            if (responseDownload.getHeaders().getContentType() == null) {
                throw new RuntimeException("No content type in response");
            }

            if (responseDownload.getBody() == null) {
                throw new RuntimeException("No body in response");
            }

            final String boundary = responseDownload.getHeaders().getContentType().getParameters().get("boundary");

            final boolean b1 = responseUpload.getStatusCode().is2xxSuccessful();
            final boolean b2 = responseDownload.getStatusCode().is2xxSuccessful();
            final boolean b3 = responseDownload.getBody().equals(body.replace("my-boundary", boundary));

            if (b1 && b2 && b3) {
                return responseUpload.getBody().directoryId();
            }

            return null;
        }

        private ResponseEntity<FileUploadResponse> uploadFiles() {

            final HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE + "; boundary=my-boundary");

            final HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(FileStorageController.FILE_STORAGE_PATH_V1, HttpMethod.POST, requestEntity, FileUploadResponse.class);
        }

        private ResponseEntity<String> downloadFiles(final FileUploadResponse fileUploadResponse) {

            final HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            final FileDownloadRequest request = new FileDownloadRequest(fileUploadResponse.directoryId(), List.of(
                    new FileDownload("1", fileUploadResponse.files().getFirst().fileId()),
                    new FileDownload("2", fileUploadResponse.files().getLast().fileId())
            ));

            final HttpEntity<FileDownloadRequest> requestEntity = new HttpEntity<>(request, headers);

            return restTemplate.exchange(FileStorageController.FILE_STORAGE_PATH_V1, HttpMethod.GET, requestEntity, String.class);
        }
    }

    private String calcHash(final String s) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA3-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA3-512 not supported", e);
        }
        final byte[] hash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
