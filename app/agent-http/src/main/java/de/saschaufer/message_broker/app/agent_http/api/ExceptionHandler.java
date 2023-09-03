package de.saschaufer.message_broker.app.agent_http.api;

import de.saschaufer.message_broker.plugin.spi.Constants;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {
    private final static String logMessage = "Request has been processed with an error: %s";

    @ApiResponse(responseCode = "500", description = "Something went wrong. Probably an internal error in the service.",
            headers = {
                    @Header(name = Constants.Http.Header.CORRELATION_ID, description = "ID to trace the message across multiple services.",
                            schema = @Schema(example = "5CYlwaEAxskZNag8p3IxeYzvRGr")
                    ),
                    @Header(name = Constants.Http.Header.SERVER_TIMING, description = "Performance metrics about the request-response cycle.",
                            schema = @Schema(example = "total;dur=6;desc=\"Total duration of request\"")
                    )
            },
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)
            )
    )
    @org.springframework.web.bind.annotation.ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<String>> handleException(final Throwable t, final ServerHttpRequest r) {

        final String correlationId = correlationId(r);

        log.atError().setMessage(String.format(logMessage, "Unexpected error."))
                .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                .addKeyValue(Constants.Logging.ENDPOINT, r.getPath())
                .setCause(t)
                .log();

        return status(HttpStatus.INTERNAL_SERVER_ERROR, correlationId, t.getMessage());
    }

    @ApiResponse(responseCode = "415", description = "The Media Type of the request is not supported.",
            headers = {
                    @Header(name = Constants.Http.Header.CORRELATION_ID, description = "ID to trace the message across multiple services.",
                            schema = @Schema(example = "5CYlwaEAxskZNag8p3IxeYzvRGr")
                    ),
                    @Header(name = Constants.Http.Header.SERVER_TIMING, description = "Performance metrics about the request-response cycle.",
                            schema = @Schema(example = "total;dur=6;desc=\"Total duration of request\"")
                    )
            },
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)
            )
    )
    @org.springframework.web.bind.annotation.ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public Mono<ResponseEntity<String>> handleUnsupportedMediaTypeStatusException(final UnsupportedMediaTypeStatusException e, final ServerHttpRequest r) {

        final String correlationId = correlationId(r);

        log.atError().setMessage(String.format(logMessage, "Not supported Media Type."))
                .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                .addKeyValue(Constants.Logging.ENDPOINT, r.getPath())
                .setCause(e)
                .log();

        return status(HttpStatus.UNSUPPORTED_MEDIA_TYPE, correlationId, "Not supported Media Type.");
    }

    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "The request has not fulfilled the API contract.",
                    headers = {
                            @Header(name = Constants.Http.Header.CORRELATION_ID, description = "ID to trace the message across multiple services.",
                                    schema = @Schema(example = "5CYlwaEAxskZNag8p3IxeYzvRGr")
                            ),
                            @Header(name = Constants.Http.Header.SERVER_TIMING, description = "Performance metrics about the request-response cycle.",
                                    schema = @Schema(example = "total;dur=6;desc=\"Total duration of request\"")
                            )
                    },
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = String.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Something went wrong. Probably an internal error in the service.",
                    headers = {
                            @Header(name = Constants.Http.Header.CORRELATION_ID, description = "ID to trace the message across multiple services.",
                                    schema = @Schema(example = "5CYlwaEAxskZNag8p3IxeYzvRGr")
                            ),
                            @Header(name = Constants.Http.Header.SERVER_TIMING, description = "Performance metrics about the request-response cycle.",
                                    schema = @Schema(example = "total;dur=6;desc=\"Total duration of request\"")
                            )
                    },
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = String.class)
                    )
            )
    })
    @org.springframework.web.bind.annotation.ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<String>> handleServerWebInputException(final ServerWebInputException e, final ServerHttpRequest r) {

        final String correlationId = correlationId(r);

        if (e.getCause() instanceof DecodingException) {

            log.atError().setMessage(String.format(logMessage, "Error deserializing request."))
                    .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                    .addKeyValue(Constants.Logging.ENDPOINT, r.getPath())
                    .setCause(e)
                    .log();

            return status(HttpStatus.BAD_REQUEST, correlationId, "Error deserializing request.");

        } else {

            log.atError().setMessage(String.format(logMessage, "Error reading request."))
                    .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                    .addKeyValue(Constants.Logging.ENDPOINT, r.getPath())
                    .setCause(e)
                    .log();

            return status(HttpStatus.INTERNAL_SERVER_ERROR, correlationId, "Error reading request.");
        }
    }

    @ApiResponse(responseCode = "400", description = "The request has not fulfilled the API contract.",
            headers = {
                    @Header(name = Constants.Http.Header.CORRELATION_ID, description = "ID to trace the message across multiple services.",
                            schema = @Schema(example = "5CYlwaEAxskZNag8p3IxeYzvRGr")
                    ),
                    @Header(name = Constants.Http.Header.SERVER_TIMING, description = "Performance metrics about the request-response cycle.",
                            schema = @Schema(example = "total;dur=6;desc=\"Total duration of request\"")
                    )
            },
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)
            )
    )
    @org.springframework.web.bind.annotation.ExceptionHandler(MissingRequestValueException.class)
    public Mono<ResponseEntity<String>> handleMissingRequestValueException(final MissingRequestValueException e, final ServerHttpRequest r) {

        final String correlationId = correlationId(r);

        log.atError().setMessage(String.format(logMessage, "Required parts of the request are missing."))
                .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                .addKeyValue(Constants.Logging.ENDPOINT, r.getPath())
                .setCause(e)
                .log();

        return status(HttpStatus.BAD_REQUEST, correlationId, "Required parts of the request are missing.");
    }

    @ApiResponse(responseCode = "400", description = "The request has not fulfilled the API contract.",
            headers = {
                    @Header(name = Constants.Http.Header.CORRELATION_ID, description = "ID to trace the message across multiple services.",
                            schema = @Schema(example = "5CYlwaEAxskZNag8p3IxeYzvRGr")
                    ),
                    @Header(name = Constants.Http.Header.SERVER_TIMING, description = "Performance metrics about the request-response cycle.",
                            schema = @Schema(example = "total;dur=6;desc=\"Total duration of request\"")
                    )
            },
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)
            )
    )
    @org.springframework.web.bind.annotation.ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<String>> handleWebExchangeBindException(final WebExchangeBindException e, final ServerHttpRequest r) {

        final String correlationId = correlationId(r);

        final String errors = e.getFieldErrors().stream()
                .map(fieldError -> new StringBuilder().append("(")
                        .append("object: ").append(fieldError.getObjectName())
                        .append(", field: ").append(fieldError.getField())
                        .append(", error: ").append(fieldError.getDefaultMessage())
                        .append(")").toString()
                ).collect(Collectors.joining(", "));

        log.atError().setMessage(String.format(logMessage, "The request has not fulfilled the API contract."))
                .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                .addKeyValue(Constants.Logging.ENDPOINT, r.getPath())
                .addKeyValue(Constants.Logging.DETAILS, errors)
                .setCause(e)
                .log();

        return status(HttpStatus.BAD_REQUEST, correlationId, String.format("The request has not fulfilled the API contract.\nCaused by: %s", errors));
    }

    @ApiResponse(responseCode = "503", description = "The service is temporarily not available. Try again later.",
            headers = {
                    @Header(name = Constants.Http.Header.CORRELATION_ID, description = "ID to trace the message across multiple services.",
                            schema = @Schema(example = "5CYlwaEAxskZNag8p3IxeYzvRGr")
                    ),
                    @Header(name = Constants.Http.Header.SERVER_TIMING, description = "Performance metrics about the request-response cycle.",
                            schema = @Schema(example = "total;dur=6;desc=\"Total duration of request\"")
                    )
            },
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)
            )
    )
    @org.springframework.web.bind.annotation.ExceptionHandler(ServiceUnavailableException.class)
    public Mono<ResponseEntity<String>> handleServiceUnavailableException(final ServiceUnavailableException e, final ServerHttpRequest r) {

        final String correlationId = correlationId(r);

        log.atError().setMessage(String.format(logMessage, "The service is temporarily not available. Try again later."))
                .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
                .addKeyValue(Constants.Logging.ENDPOINT, r.getPath())
                .setCause(e)
                .log();

        return status(HttpStatus.SERVICE_UNAVAILABLE, correlationId, "The service is temporarily not available. Try again later.");
    }

    private Mono<ResponseEntity<String>> status(final HttpStatus httpStatus, final String correlationId, final String error) {
        return Mono.just(ResponseEntity.status(httpStatus)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .header(Constants.Http.Header.CORRELATION_ID, correlationId)
                .body(error)
        );
    }

    private String correlationId(final ServerHttpRequest request) {
        return request.getHeaders().getFirst(Constants.Http.Header.CORRELATION_ID);
    }
}
