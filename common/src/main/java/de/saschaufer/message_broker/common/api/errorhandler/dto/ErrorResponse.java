package de.saschaufer.message_broker.common.api.errorhandler.dto;

import java.util.Map;

public record ErrorResponse(
        String error,
        Map<String, String> additionalDetails
) {
}
