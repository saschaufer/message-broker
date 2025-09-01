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
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import static de.saschaufer.message_broker.app.file_storage.api.Router.FILE_STORAGE_PATH_V1;

// Swagger in Custom Annotation
// https://medium.com/dandelion-tutorials/documenting-functional-rest-endpoints-with-springdoc-openapi-21657c0ebc8a
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@RouterOperations({
        @RouterOperation(
                method = RequestMethod.POST,
                path = FILE_STORAGE_PATH_V1,
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.TEXT_PLAIN_VALUE,
                operation = @Operation(
                        operationId = "message",
                        parameters = {
                                @Parameter(
                                        name = Constants.Http.Header.USER_AGENT,
                                        description = "A name for the sending system.",
                                        example = "my-pc",
                                        in = ParameterIn.HEADER,
                                        required = true
                                ),
                                @Parameter(
                                        name = Constants.Http.Header.CORRELATION_ID,
                                        description = "A unique identifier for the request. If none is provided, one is generated.",
                                        example = "sadrwrw3r",
                                        in = ParameterIn.HEADER,
                                        required = false
                                ),
                                @Parameter(
                                        name = Constants.Http.Query.DIRECTORY_ID,
                                        description = "Provide if files should be stored under the same directory as previously uploaded files.",
                                        example = "sdfaefafe",
                                        in = ParameterIn.QUERY,
                                        required = false
                                )
                        },
                        requestBody = @RequestBody(
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
                        ),
                        responses = {
                                @ApiResponse(responseCode = "201", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FileUploadResponse.class))),
                                @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
                                @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
                        }
                )
        ),
        @RouterOperation(
                method = RequestMethod.GET,
                path = FILE_STORAGE_PATH_V1,
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.MULTIPART_FORM_DATA_VALUE,
                operation = @Operation(
                        operationId = "message",
                        parameters = {
                                @Parameter(
                                        name = Constants.Http.Header.USER_AGENT,
                                        description = "A name for the sending system.",
                                        example = "my-pc",
                                        in = ParameterIn.HEADER,
                                        required = true
                                ),
                                @Parameter(
                                        name = Constants.Http.Header.CORRELATION_ID,
                                        description = "A unique identifier for the request. If none is provided, one is generated.",
                                        example = "sadrwrw3r",
                                        in = ParameterIn.HEADER,
                                        required = false
                                )
                        },
                        requestBody = @RequestBody(
                                content = @Content(
                                        schema = @Schema(
                                                implementation = FileDownloadRequest.class
                                        )
                                )
                        ),
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
        )
})
public @interface RouterDoc {
}
