package de.saschaufer.message_broker.app.agent_http.api.register_user;

import de.saschaufer.message_broker.plugin.spi.Constants;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;

@RequestMapping(RegisterUserApi.ROOT + RegisterUserApi.PATH)
@Tag(name = RegisterUserApi.TAG, description = RegisterUserApi.DESCRIPTION)
public interface RegisterUserApi {

    String TITLE = "Register User";
    String VERSION = "1.0.0";
    String TAG = "register-user";
    String DESCRIPTION = "Api to register a user.";
    String ROOT = "/agent-http";
    String PATH = "/register-user";

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ApiResponse(responseCode = "200", description = "The user was received and successfully forwarded for further processing.",
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
    Mono<ResponseEntity<String>> postUser(
            @Parameter(
                    description = "ID to trace the message across multiple services. If no ID is given, one will be generated.",
                    example = "5CYlwaEAxskZNag8p3IxeYzvRGr"
            )
            @RequestHeader(
                    name = Constants.Http.Header.CORRELATION_ID,
                    required = false
            ) final String correlationId,

            @RequestBody @Validated final User user
    ) throws ServiceUnavailableException;
}
