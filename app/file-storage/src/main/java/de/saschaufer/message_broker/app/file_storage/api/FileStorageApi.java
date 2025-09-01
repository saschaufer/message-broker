package de.saschaufer.message_broker.app.file_storage.api;

import de.saschaufer.message_broker.common.Constants;
import de.saschaufer.message_broker.common.api.errorhandler.dto.ErrorResponse;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileDownloadRequest;
import de.saschaufer.message_broker.common.api.file_storage.dto.FileUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.Part;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FileStorageApi {

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            parameters = {
                    @Parameter(
                            name = Constants.Http.Header.CORRELATION_ID,
                            description = "A unique identifier for the request. If none is provided, one is generated.",
                            example = "sadrwrw3r",
                            in = ParameterIn.HEADER
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "201", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileUploadResponse.class))),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    ResponseEntity<FileUploadResponse> postFiles(
            //@formatter:off
            @Parameter(
                    name = Constants.Http.Header.USER_AGENT,
                    description = "A name for the sending system.",
                    example = "my-pc",
                    in = ParameterIn.HEADER,
                    required = true
            )
            @RequestHeader(value = Constants.Http.Header.USER_AGENT)
            final String userAgent,

            @Parameter(
                    name = Constants.Http.Query.DIRECTORY_ID,
                    description = "Provide if files should be stored under the same directory as previously uploaded files.",
                    example = "sdfaefafe",
                    in = ParameterIn.QUERY
            )
            @RequestParam(value = Constants.Http.Query.DIRECTORY_ID, required = false)
            final String directoryId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            encoding = @Encoding(
                                    name = Constants.Http.BodyPart.FILE,
                                    style = "",
                                    headers = {
                                            @Header(
                                                    name = Constants.Http.Header.FILE_ID,
                                                    required = true,
                                                    description = "Unique ID for the file. Used in response to identify a file.",
                                                    schema = @Schema(implementation = Map.class)
                                            ),
                                            @Header(
                                                    name = Constants.Http.Header.FILE_HASH,
                                                    description = "SHA3-512 of the file.",
                                                    schema = @Schema(implementation = Map.class)
                                            )
                                    }
                            ),
                            schemaProperties = {
                                    @SchemaProperty(
                                            name = Constants.Http.BodyPart.FILE,
                                            array = @ArraySchema(
                                                    minItems = 1,
                                                    schema = @Schema(
                                                            name = Constants.Http.BodyPart.FILE,
                                                            contentMediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                                            implementation = MultipartFile.class
                                                    )
                                            )
                                    )
                            }
                    )
            )
            @RequestPart(Constants.Http.BodyPart.FILE)
            final List<Part> files
            //@formatter:on
    );

    @GetMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            parameters = {
                    @Parameter(
                            name = Constants.Http.Header.CORRELATION_ID,
                            description = "A unique identifier for the request. If none is provided, one is generated.",
                            example = "sadrwrw3r",
                            in = ParameterIn.HEADER
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(
                            encoding = @Encoding(
                                    name = Constants.Http.BodyPart.FILE,
                                    style = "",
                                    headers = {
                                            @Header(
                                                    name = Constants.Http.Header.FILE_ID,
                                                    required = true,
                                                    description = "Unique ID for the file. Same ID for file as in request.",
                                                    schema = @Schema(implementation = Map.class)
                                            ),
                                            @Header(
                                                    name = Constants.Http.Header.FILE_HASH,
                                                    description = "SHA3-512 of the file.",
                                                    schema = @Schema(implementation = Map.class)
                                            )
                                    }
                            ),
                            schemaProperties = {
                                    @SchemaProperty(
                                            name = Constants.Http.BodyPart.FILE,
                                            array = @ArraySchema(
                                                    minItems = 1,
                                                    schema = @Schema(
                                                            name = Constants.Http.BodyPart.FILE,
                                                            contentMediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                                            implementation = MultipartFile.class
                                                    )
                                            )
                                    )
                            }
                    )),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    ResponseEntity<MultiValueMap<String, Object>> getFiles(
            //@formatter:off
            @Parameter(
                    name = Constants.Http.Header.USER_AGENT,
                    description = "A name for the sending system.",
                    example = "my-pc",
                    in = ParameterIn.HEADER,
                    required = true
            )
            @RequestHeader(value = Constants.Http.Header.USER_AGENT)
            final String userAgent,

            @RequestBody
            final FileDownloadRequest fileDownloadRequest
            //@formatter:on
    );
}
