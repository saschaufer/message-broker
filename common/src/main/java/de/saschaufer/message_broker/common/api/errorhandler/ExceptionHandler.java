package de.saschaufer.message_broker.common.api.errorhandler;

import de.saschaufer.message_broker.common.Constants;
import de.saschaufer.message_broker.common.api.errorhandler.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class ExceptionHandler {

    static final String ERROR_MESSAGE = "Error processing request.";

    public ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.atError().setMessage(ERROR_MESSAGE).setCause(e).log();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (e instanceof org.springframework.web.ErrorResponse er) {

            final Map<String, String> additionalDetails = new HashMap<>();

            if (er.getBody().getDetail() != null) {
                additionalDetails.put("error", er.getBody().getDetail());
            }

            return new ResponseEntity<>(
                    new ErrorResponse(ERROR_MESSAGE, additionalDetails),
                    headers,
                    er.getStatusCode()
            );
        }

        if (e instanceof HttpMessageConversionException er) {

            final Map<String, String> additionalDetails = new HashMap<>();

            if (er.getMessage() != null) {
                additionalDetails.put("error", er.getMessage());
            }

            return new ResponseEntity<>(
                    new ErrorResponse(ERROR_MESSAGE, additionalDetails),
                    headers,
                    HttpStatus.BAD_REQUEST
            );
        }

        return new ResponseEntity<>(
                new ErrorResponse(ERROR_MESSAGE, null),
                headers,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    public ResponseEntity<ErrorResponse> handleResponseStatusException(final ResponseStatusException e) {
        log.atError().setMessage(ERROR_MESSAGE).addKeyValue(Constants.Logging.DETAILS, e.getAdditionalDetails()).setCause(e).log();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(
                new ErrorResponse(e.getReason(), e.getAdditionalDetails()),
                headers,
                e.getStatusCode()
        );
    }
}
