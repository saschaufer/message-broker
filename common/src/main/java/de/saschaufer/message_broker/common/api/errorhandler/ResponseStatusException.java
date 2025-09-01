package de.saschaufer.message_broker.common.api.errorhandler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ResponseStatusException extends RuntimeException {

    private final HttpStatus statusCode;
    private final String reason;
    private final Map<String, String> additionalDetails;

    public ResponseStatusException(final HttpStatus statusCode, final String reason) {
        super(reason);
        this.statusCode = statusCode;
        this.reason = reason;
        this.additionalDetails = new HashMap<>();
    }

    public ResponseStatusException(final HttpStatus statusCode, final String reason, final Throwable cause) {
        super(reason, cause);
        this.statusCode = statusCode;
        this.reason = reason;
        this.additionalDetails = new HashMap<>();
    }

    public ResponseStatusException(final HttpStatus statusCode, final String reason, final Map<String, String> additionalDetails) {
        super(reason);
        this.statusCode = statusCode;
        this.reason = reason;
        this.additionalDetails = additionalDetails;
    }

    public ResponseStatusException(final HttpStatus statusCode, final String reason, final Map<String, String> additionalDetails, final Throwable cause) {
        super(reason, cause);
        this.statusCode = statusCode;
        this.reason = reason;
        this.additionalDetails = additionalDetails;
    }

    public Map<String, Object> getResponseBody() {
        final Map<String, Object> body = new HashMap<>();
        body.put("reason", getReason());
        if (!getAdditionalDetails().isEmpty()) {
            body.put("additional_details", getAdditionalDetails());
        }
        return body;
    }
}
