package de.saschaufer.message_broker.app.file_storage.api.errorhandler;

import de.saschaufer.message_broker.common.api.errorhandler.ResponseStatusException;
import de.saschaufer.message_broker.common.api.errorhandler.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends de.saschaufer.message_broker.common.api.errorhandler.ExceptionHandler {

    @Override
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception e) {
        return super.handleException(e);
    }

    @Override
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(final ResponseStatusException e) {
        return super.handleResponseStatusException(e);
    }
}
