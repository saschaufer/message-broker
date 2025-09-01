package de.saschaufer.message_broker.app.file_storage.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.saschaufer.message_broker.app.file_storage.config.FilterConfig;
import de.saschaufer.message_broker.app.file_storage.service.FileStorageService;
import de.saschaufer.message_broker.app.file_storage.service.dto.File;
import de.saschaufer.message_broker.common.Constants;
import de.saschaufer.message_broker.common.api.errorhandler.ResponseStatusException;
import de.saschaufer.message_broker.common.api.errorhandler.dto.ErrorResponse;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownload;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownloadRequest;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileUploadResponse;
import de.saschaufer.message_broker.common.json.JsonUtils;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static de.saschaufer.message_broker.app.file_storage.api.FileStorageController.FILE_STORAGE_PATH_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@WebMvcTest(controllers = FileStorageController.class)
@Import({FilterConfig.class})
class FileStorageControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    void postFiles_positive() throws Exception {

        doReturn(List.of(
                new File("1", "file-id-1", "file-1.txt", MediaType.TEXT_PLAIN, "file-hash-1"),
                new File("2", "file-id-2", "file-2.pdf", MediaType.APPLICATION_PDF, "file-hash-2")
        )).when(fileStorageService).saveFiles(any(String.class), any(List.class));

        final MockPart mockPartFile1 = new MockPart(Constants.Http.BodyPart.FILE, "file-1.txt", "body-1".getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN);
        mockPartFile1.getHeaders().add(Constants.Http.Header.FILE_ID, "1");
        final MockPart mockPartFile2 = new MockPart(Constants.Http.BodyPart.FILE, "file-2.pdf", "body-2".getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_PDF);
        mockPartFile2.getHeaders().add(Constants.Http.Header.FILE_ID, "2");

        final MvcTestResult mvcTestResult = mockMvc.perform(multipart(FILE_STORAGE_PATH_V1)
                .part(mockPartFile1, mockPartFile2)
                .header(Constants.Http.Header.USER_AGENT, "test-user-agent")
        );

        @SuppressWarnings("unchecked") final ArgumentCaptor<List<Part>> captorListParts = ArgumentCaptor.forClass(List.class);

        verify(fileStorageService, times(1)).saveFiles(any(String.class), captorListParts.capture());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        final List<Part> parts = captorListParts.getValue();
        assertThat(parts, hasSize(2));

        assertThat(parts.getFirst().getName(), is(Constants.Http.BodyPart.FILE));
        assertThat(parts.getFirst().getSubmittedFileName(), is("file-1.txt"));
        assertThat(parts.getFirst().getHeader(Constants.Http.Header.FILE_ID), is("1"));
        assertThat(parts.getFirst().getContentType(), is(MediaType.TEXT_PLAIN_VALUE));
        assertThat(parts.getFirst().getInputStream().readAllBytes(), is("body-1".getBytes(StandardCharsets.UTF_8)));

        assertThat(parts.getLast().getName(), is(Constants.Http.BodyPart.FILE));
        assertThat(parts.getLast().getSubmittedFileName(), is("file-2.pdf"));
        assertThat(parts.getLast().getHeader(Constants.Http.Header.FILE_ID), is("2"));
        assertThat(parts.getLast().getContentType(), is(MediaType.APPLICATION_PDF_VALUE));
        assertThat(parts.getLast().getInputStream().readAllBytes(), is("body-2".getBytes(StandardCharsets.UTF_8)));

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), nullValue());
        final MockHttpServletResponse response = mvcTestResult.getResponse();

        assertThat(response.getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());

        final FileUploadResponse fileUploadResponse = JsonUtils.fromJson(response.getContentAsByteArray(), FileUploadResponse.class);
        assertThat(fileUploadResponse.directoryId(), notNullValue());
        assertThat(fileUploadResponse.files(), hasSize(2));

        assertThat(fileUploadResponse.files().getFirst().id(), is("1"));
        assertThat(fileUploadResponse.files().getFirst().fileId(), is("file-id-1"));
        assertThat(fileUploadResponse.files().getFirst().fileHash(), is("file-hash-1"));

        assertThat(fileUploadResponse.files().getLast().id(), is("2"));
        assertThat(fileUploadResponse.files().getLast().fileId(), is("file-id-2"));
        assertThat(fileUploadResponse.files().getLast().fileHash(), is("file-hash-2"));
    }

    @Test
    void postFiles_positive_WithDirectoryIdAndCorrelationId() throws Exception {

        doReturn(List.of(
                new File("1", "file-id-1", "file-1.txt", MediaType.TEXT_PLAIN, "file-hash-1"),
                new File("2", "file-id-2", "file-2.pdf", MediaType.APPLICATION_PDF, "file-hash-2")
        )).when(fileStorageService).saveFiles(any(String.class), any(List.class));

        final MockPart mockPartFile1 = new MockPart(Constants.Http.BodyPart.FILE, "file-1.txt", "body-1".getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN);
        mockPartFile1.getHeaders().add(Constants.Http.Header.FILE_ID, "1");
        final MockPart mockPartFile2 = new MockPart(Constants.Http.BodyPart.FILE, "file-2.pdf", "body-2".getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_PDF);
        mockPartFile2.getHeaders().add(Constants.Http.Header.FILE_ID, "2");

        final MvcTestResult mvcTestResult = mockMvc.perform(multipart(FILE_STORAGE_PATH_V1)
                .part(mockPartFile1, mockPartFile2)
                .param(Constants.Http.Query.DIRECTORY_ID, "dirId")
                .header(Constants.Http.Header.CORRELATION_ID, "corId")
                .header(Constants.Http.Header.USER_AGENT, "test-user-agent")
        );

        @SuppressWarnings("unchecked") final ArgumentCaptor<List<Part>> captorListParts = ArgumentCaptor.forClass(List.class);

        verify(fileStorageService, times(1)).saveFiles(eq("dirId"), captorListParts.capture());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        final List<Part> parts = captorListParts.getValue();
        assertThat(parts, hasSize(2));

        assertThat(parts.getFirst().getName(), is(Constants.Http.BodyPart.FILE));
        assertThat(parts.getFirst().getSubmittedFileName(), is("file-1.txt"));
        assertThat(parts.getFirst().getHeader(Constants.Http.Header.FILE_ID), is("1"));
        assertThat(parts.getFirst().getContentType(), is(MediaType.TEXT_PLAIN_VALUE));
        assertThat(parts.getFirst().getInputStream().readAllBytes(), is("body-1".getBytes(StandardCharsets.UTF_8)));

        assertThat(parts.getLast().getName(), is(Constants.Http.BodyPart.FILE));
        assertThat(parts.getLast().getSubmittedFileName(), is("file-2.pdf"));
        assertThat(parts.getLast().getHeader(Constants.Http.Header.FILE_ID), is("2"));
        assertThat(parts.getLast().getContentType(), is(MediaType.APPLICATION_PDF_VALUE));
        assertThat(parts.getLast().getInputStream().readAllBytes(), is("body-2".getBytes(StandardCharsets.UTF_8)));

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), nullValue());
        final MockHttpServletResponse response = mvcTestResult.getResponse();

        assertThat(response.getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), is("corId"));

        final FileUploadResponse fileUploadResponse = JsonUtils.fromJson(response.getContentAsByteArray(), FileUploadResponse.class);
        assertThat(fileUploadResponse.directoryId(), notNullValue());
        assertThat(fileUploadResponse.files(), hasSize(2));

        assertThat(fileUploadResponse.files().getFirst().id(), is("1"));
        assertThat(fileUploadResponse.files().getFirst().fileId(), is("file-id-1"));
        assertThat(fileUploadResponse.files().getFirst().fileHash(), is("file-hash-1"));

        assertThat(fileUploadResponse.files().getLast().id(), is("2"));
        assertThat(fileUploadResponse.files().getLast().fileId(), is("file-id-2"));
        assertThat(fileUploadResponse.files().getLast().fileHash(), is("file-hash-2"));
    }

    @Test
    void postFiles_negative_BodyIsNotAMultipart() throws IOException {

        //@formatter:off
        final MvcTestResult mvcTestResult = mockMvc.post()
                .uri(FILE_STORAGE_PATH_V1)
                .header(Constants.Http.Header.USER_AGENT, "test-user-agent")
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE + "; boundary=\"abc\"")
                .content("Not a multipart")
                .exchange();
        //@formatter:on

        verify(fileStorageService, times(0)).saveFiles(any(), any());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), notNullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), instanceOf(org.springframework.web.ErrorResponse.class));

        final org.springframework.web.ErrorResponse e = (org.springframework.web.ErrorResponse) mvcTestResult.getMvcResult().getResolvedException();

        assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(e.getBody().getDetail(), is(String.format("Required part '%s' is not present.", Constants.Http.BodyPart.FILE)));

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_VALUE));
        assertThat(response.getErrorMessage(), nullValue());

        final ErrorResponse errorResponse = new ObjectMapper().readValue(response.getContentAsByteArray(), ErrorResponse.class);

        assertThat(errorResponse.error(), is("Error processing request."));
        assertThat(errorResponse.additionalDetails(), aMapWithSize(1));
        assertThat(errorResponse.additionalDetails().get("error"), is(String.format("Required part '%s' is not present.", Constants.Http.BodyPart.FILE)));
    }

    @Test
    void postFiles_negative_MissingHeaderUserAgent() throws IOException {

        final MockPart mockPartFile1 = new MockPart(Constants.Http.BodyPart.FILE, "file-1.txt", "body-1".getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN);
        mockPartFile1.getHeaders().add(Constants.Http.Header.FILE_ID, "1");

        final MvcTestResult mvcTestResult = mockMvc.perform(multipart(FILE_STORAGE_PATH_V1)
                .part(mockPartFile1)
        );

        verify(fileStorageService, times(0)).saveFiles(any(), any());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), notNullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), instanceOf(org.springframework.web.ErrorResponse.class));

        final org.springframework.web.ErrorResponse e = (org.springframework.web.ErrorResponse) mvcTestResult.getMvcResult().getResolvedException();

        assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(e.getBody().getDetail(), is(String.format("Required header '%s' is not present.", Constants.Http.Header.USER_AGENT)));

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_VALUE));
        assertThat(response.getErrorMessage(), nullValue());

        final ErrorResponse errorResponse = new ObjectMapper().readValue(response.getContentAsByteArray(), ErrorResponse.class);

        assertThat(errorResponse.error(), is("Error processing request."));
        assertThat(errorResponse.additionalDetails(), aMapWithSize(1));
        assertThat(errorResponse.additionalDetails().get("error"), is(String.format("Required header '%s' is not present.", Constants.Http.Header.USER_AGENT)));
    }

    @Test
    void postFiles_negative_EmptyHeaderUserAgent() throws IOException {

        final MockPart mockPartFile1 = new MockPart(Constants.Http.BodyPart.FILE, "file-1.txt", "body-1".getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN);
        mockPartFile1.getHeaders().add(Constants.Http.Header.FILE_ID, "1");

        final MvcTestResult mvcTestResult = mockMvc.perform(multipart(FILE_STORAGE_PATH_V1)
                .part(mockPartFile1)
                .header(Constants.Http.Header.USER_AGENT, "")
        );

        verify(fileStorageService, times(0)).saveFiles(any(), any());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), notNullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), instanceOf(ResponseStatusException.class));

        final ResponseStatusException e = (ResponseStatusException) mvcTestResult.getMvcResult().getResolvedException();

        assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(e.getMessage(), is(String.format("Header '%s' must not be empty.", Constants.Http.Header.USER_AGENT)));
        assertThat(e.getAdditionalDetails(), anEmptyMap());

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_VALUE));
        assertThat(response.getErrorMessage(), nullValue());

        final ErrorResponse errorResponse = new ObjectMapper().readValue(response.getContentAsByteArray(), ErrorResponse.class);

        assertThat(errorResponse.error(), is(String.format("Header '%s' must not be empty.", Constants.Http.Header.USER_AGENT)));
        assertThat(errorResponse.additionalDetails(), anEmptyMap());
    }

    @Test
    void postFiles_negative_EmptyParameterDirectoryId() throws IOException {

        final MockPart mockPartFile1 = new MockPart(Constants.Http.BodyPart.FILE, "file-1.txt", "body-1".getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN);
        mockPartFile1.getHeaders().add(Constants.Http.Header.FILE_ID, "1");

        final MvcTestResult mvcTestResult = mockMvc.perform(multipart(FILE_STORAGE_PATH_V1)
                .part(mockPartFile1)
                .header(Constants.Http.Header.USER_AGENT, "test-user-agent")
                .param(Constants.Http.Query.DIRECTORY_ID, "")
        );

        verify(fileStorageService, times(0)).saveFiles(any(), any());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), notNullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), instanceOf(ResponseStatusException.class));

        final ResponseStatusException e = (ResponseStatusException) mvcTestResult.getMvcResult().getResolvedException();

        assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(e.getMessage(), is(String.format("Parameter '%s' must not be empty.", Constants.Http.Query.DIRECTORY_ID)));
        assertThat(e.getAdditionalDetails(), aMapWithSize(0));

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_VALUE));
        assertThat(response.getErrorMessage(), nullValue());

        final ErrorResponse errorResponse = new ObjectMapper().readValue(response.getContentAsByteArray(), ErrorResponse.class);

        assertThat(errorResponse.error(), is(String.format("Parameter '%s' must not be empty.", Constants.Http.Query.DIRECTORY_ID)));
        assertThat(errorResponse.additionalDetails(), anEmptyMap());
    }

    @Test
    void postFiles_negative_SaveFilesThrowsError() throws IOException {

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error", Map.of("a", "b"))).when(fileStorageService).saveFiles(any(), any());

        final MockPart mockPartFile1 = new MockPart(Constants.Http.BodyPart.FILE, "file-1.txt", "body-1".getBytes(StandardCharsets.UTF_8), MediaType.TEXT_PLAIN);
        mockPartFile1.getHeaders().add(Constants.Http.Header.FILE_ID, "1");

        final MvcTestResult mvcTestResult = mockMvc.perform(multipart(FILE_STORAGE_PATH_V1)
                .part(mockPartFile1)
                .header(Constants.Http.Header.USER_AGENT, "test-user-agent")
                .param(Constants.Http.Query.DIRECTORY_ID, "dirId")
        );

        verify(fileStorageService, times(1)).saveFiles(any(), any());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), notNullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), instanceOf(ResponseStatusException.class));

        final ResponseStatusException e = (ResponseStatusException) mvcTestResult.getMvcResult().getResolvedException();

        assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(e.getMessage(), is("Error"));
        assertThat(e.getAdditionalDetails(), aMapWithSize(1));
        assertThat(e.getAdditionalDetails().get("a"), is("b"));

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_VALUE));
        assertThat(response.getErrorMessage(), nullValue());

        final ErrorResponse errorResponse = new ObjectMapper().readValue(response.getContentAsByteArray(), ErrorResponse.class);

        assertThat(errorResponse.error(), is("Error"));
        assertThat(errorResponse.additionalDetails(), aMapWithSize(1));
        assertThat(errorResponse.additionalDetails().get("a"), is("b"));
    }

    @Test
    void getFiles_positive() throws Exception {

        doReturn(List.of(
                createHttpEntity("1", "file-1.txt", MediaType.TEXT_PLAIN, "file-1-hash", "body-1".getBytes(StandardCharsets.UTF_8)),
                createHttpEntity("2", "file-2.pdf", MediaType.APPLICATION_PDF, "file-2-hash", "body-2".getBytes(StandardCharsets.UTF_8))
        )).when(fileStorageService).readFiles(any(String.class), any(List.class));

        final MvcTestResult mvcTestResult = mockMvc.perform(get(FILE_STORAGE_PATH_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .header(Constants.Http.Header.USER_AGENT, "test-user-agent")
                .content(JsonUtils.toJson(new FileDownloadRequest("dirId", List.of(
                        new FileDownload("1", "file-id-1"),
                        new FileDownload("2", "file-id-2")
                ))))
        );

        verify(fileStorageService, times(0)).saveFiles(any(), any());
        verify(fileStorageService, times(1)).readFiles("dirId", List.of(
                new FileDownload("1", "file-id-1"),
                new FileDownload("2", "file-id-2")
        ));

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), nullValue());

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.OK.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());

        // A normal String can't be used because the boundary is random.
        final StringBuilder sb = new StringBuilder();
        sb.append("--[\\w-]+\\r\\n");
        sb.append("Content-Disposition: form-data; name=\\\"file\\\"; filename=\\\"file-1\\.txt\\\"\\r\\n");
        sb.append("Content-Type: text/plain\\r\\n");
        sb.append("X-File-ID: 1\\r\\n");
        sb.append("X-File-Hash: file-1-hash\\r\\n");
        sb.append("\\r\\n");
        sb.append("body-1\\r\\n");
        sb.append("--[\\w-]+\\r\\n");
        sb.append("Content-Disposition: form-data; name=\\\"file\\\"; filename=\\\"file-2\\.pdf\\\"\\r\\n");
        sb.append("Content-Type: application/pdf\\r\\n");
        sb.append("X-File-ID: 2\\r\\n");
        sb.append("X-File-Hash: file-2-hash\\r\\n");
        sb.append("\\r\\n");
        sb.append("body-2\\r\\n");
        sb.append("--[\\w-]+--\\r\\n");

        final Pattern pattern = Pattern.compile(sb.toString());

        assertThat(pattern.matcher(response.getContentAsString()).matches(), is(true));
    }

    @Test
    void getFiles_negative_BodyIsNotAFileDownloadRequest() throws IOException {

        final MvcTestResult mvcTestResult = mockMvc.perform(get(FILE_STORAGE_PATH_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .header(Constants.Http.Header.USER_AGENT, "test-user-agent")
                .content("Wrong".getBytes(StandardCharsets.UTF_8))
        );

        verify(fileStorageService, times(0)).saveFiles(any(), any());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), notNullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), instanceOf(HttpMessageConversionException.class));

        final HttpMessageConversionException e = (HttpMessageConversionException) mvcTestResult.getMvcResult().getResolvedException();

        assertThat(e.getMessage(), startsWith("JSON parse error"));

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_VALUE));
        assertThat(response.getErrorMessage(), nullValue());

        final ErrorResponse errorResponse = new ObjectMapper().readValue(response.getContentAsByteArray(), ErrorResponse.class);

        assertThat(errorResponse.error(), is("Error processing request."));
        assertThat(errorResponse.additionalDetails(), aMapWithSize(1));
        assertThat(errorResponse.additionalDetails().get("error"), startsWith("JSON parse error"));
    }

    @Test
    void getFiles_negative_MissingHeaderUserAgent() throws IOException {

        final MvcTestResult mvcTestResult = mockMvc.perform(get(FILE_STORAGE_PATH_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJson(new FileDownloadRequest("dirId", List.of(
                        new FileDownload("1", "file-id-1"),
                        new FileDownload("2", "file-id-2")
                ))))
        );

        verify(fileStorageService, times(0)).saveFiles(any(), any());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), notNullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), instanceOf(org.springframework.web.ErrorResponse.class));

        final org.springframework.web.ErrorResponse e = (org.springframework.web.ErrorResponse) mvcTestResult.getMvcResult().getResolvedException();

        assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(e.getBody().getDetail(), is(String.format("Required header '%s' is not present.", Constants.Http.Header.USER_AGENT)));

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_VALUE));
        assertThat(response.getErrorMessage(), nullValue());

        final ErrorResponse errorResponse = new ObjectMapper().readValue(response.getContentAsByteArray(), ErrorResponse.class);

        assertThat(errorResponse.error(), is("Error processing request."));
        assertThat(errorResponse.additionalDetails(), aMapWithSize(1));
        assertThat(errorResponse.additionalDetails().get("error"), is(String.format("Required header '%s' is not present.", Constants.Http.Header.USER_AGENT)));
    }

    @Test
    void getFiles_negative_EmptyHeaderUserAgent() throws IOException {

        final MvcTestResult mvcTestResult = mockMvc.perform(get(FILE_STORAGE_PATH_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .header(Constants.Http.Header.USER_AGENT, "")
                .content(JsonUtils.toJson(new FileDownloadRequest("dirId", List.of(
                        new FileDownload("1", "file-id-1"),
                        new FileDownload("2", "file-id-2")
                ))))
        );

        verify(fileStorageService, times(0)).saveFiles(any(), any());
        verify(fileStorageService, times(0)).readFiles(any(), any());

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), notNullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), instanceOf(ResponseStatusException.class));

        final ResponseStatusException e = (ResponseStatusException) mvcTestResult.getMvcResult().getResolvedException();

        assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(e.getMessage(), is(String.format("Header '%s' must not be empty.", Constants.Http.Header.USER_AGENT)));
        assertThat(e.getAdditionalDetails(), anEmptyMap());

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_VALUE));
        assertThat(response.getErrorMessage(), nullValue());

        final ErrorResponse errorResponse = new ObjectMapper().readValue(response.getContentAsByteArray(), ErrorResponse.class);

        assertThat(errorResponse.error(), is(String.format("Header '%s' must not be empty.", Constants.Http.Header.USER_AGENT)));
        assertThat(errorResponse.additionalDetails(), anEmptyMap());
    }

    @Test
    void getFiles_negative_ReadFilesThrowsError() throws Exception {

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error", Map.of("a", "b"))).when(fileStorageService).readFiles(any(), any());

        final MvcTestResult mvcTestResult = mockMvc.perform(get(FILE_STORAGE_PATH_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .header(Constants.Http.Header.USER_AGENT, "test-user-agent")
                .content(JsonUtils.toJson(new FileDownloadRequest("dirId", List.of(
                        new FileDownload("1", "file-id-1"),
                        new FileDownload("2", "file-id-2")
                ))))
        );

        verify(fileStorageService, times(0)).saveFiles(any(), any());
        verify(fileStorageService, times(1)).readFiles(any(), any());

        assertThat(mvcTestResult.getUnresolvedException(), nullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), notNullValue());
        assertThat(mvcTestResult.getMvcResult().getResolvedException(), instanceOf(ResponseStatusException.class));

        final ResponseStatusException e = (ResponseStatusException) mvcTestResult.getMvcResult().getResolvedException();

        assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(e.getMessage(), is("Error"));
        assertThat(e.getAdditionalDetails(), aMapWithSize(1));
        assertThat(e.getAdditionalDetails().get("a"), is("b"));

        final MockHttpServletResponse response = mvcTestResult.getResponse();
        assertThat(response.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getHeader(Constants.Http.Header.CORRELATION_ID), notNullValue());
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_VALUE));
        assertThat(response.getErrorMessage(), nullValue());

        final ErrorResponse errorResponse = new ObjectMapper().readValue(response.getContentAsByteArray(), ErrorResponse.class);

        assertThat(errorResponse.error(), is("Error"));
        assertThat(errorResponse.additionalDetails(), aMapWithSize(1));
        assertThat(errorResponse.additionalDetails().get("a"), is("b"));
    }

    private HttpEntity<InputStreamResource> createHttpEntity(final String id, final String filename, final MediaType mediaType, final String fileHash, final byte[] content) {

        final ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name(Constants.Http.BodyPart.FILE)
                .filename(filename)
                .build();

        final MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        fileMap.add(HttpHeaders.CONTENT_TYPE, mediaType.toString());
        fileMap.add(Constants.Http.Header.FILE_ID, id);
        fileMap.add(Constants.Http.Header.FILE_HASH, fileHash);

        return new HttpEntity<>(new InputStreamResource(new ByteArrayInputStream(content)), fileMap);
    }
}
