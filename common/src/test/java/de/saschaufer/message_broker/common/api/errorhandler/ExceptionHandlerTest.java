package de.saschaufer.message_broker.common.api.errorhandler;

import de.saschaufer.message_broker.common.api.errorhandler.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.ErrorResponseException;

import java.util.Map;

import static de.saschaufer.message_broker.common.api.errorhandler.ExceptionHandler.ERROR_MESSAGE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ExceptionHandlerTest {

    private final ExceptionHandler exceptionHandler = new ExceptionHandler() {

        @Override
        public ResponseEntity<ErrorResponse> handleException(final Exception e) {
            return super.handleException(e);
        }

        @Override
        public ResponseEntity<ErrorResponse> handleResponseStatusException(final ResponseStatusException e) {
            return super.handleResponseStatusException(e);
        }
    };

    @Test
    void handleException_positive() {

        final ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(new Exception("Error"));

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(response.getHeaders().getContentType(), is(MediaType.APPLICATION_JSON));
        assertThat(response.getBody(), is(new ErrorResponse(ERROR_MESSAGE, null)));
    }

    @Test
    void handleException_positive_ErrorResponse() {

        final ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(new ErrorResponseException(HttpStatus.BAD_REQUEST, ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Problem details"), new Exception("Error")));

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(response.getHeaders().getContentType(), is(MediaType.APPLICATION_JSON));
        assertThat(response.getBody(), is(new ErrorResponse(ERROR_MESSAGE, Map.of("error", "Problem details"))));
    }

    @Test
    void handleException_positive_HttpMessageConversionException() {

        final ResponseEntity<ErrorResponse> response = exceptionHandler.handleException(new HttpMessageConversionException("Error"));

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(response.getHeaders().getContentType(), is(MediaType.APPLICATION_JSON));
        assertThat(response.getBody(), is(new ErrorResponse(ERROR_MESSAGE, Map.of("error", "Error"))));
    }

    @Test
    void handleResponseStatusException_positive() {

        final ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Error"
        ));

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(response.getHeaders().getContentType(), is(MediaType.APPLICATION_JSON));
        assertThat(response.getBody(), is(new ErrorResponse("Error", Map.of())));
    }

    @Test
    void handleResponseStatusException_positive_WithAdditionalDetails() {

        final ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Error",
                Map.of("a", "1", "b", "2")
        ));

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(response.getHeaders().getContentType(), is(MediaType.APPLICATION_JSON));
        assertThat(response.getBody(), is(new ErrorResponse("Error", Map.of("a", "1", "b", "2"))));
    }
}
