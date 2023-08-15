package de.saschaufer.message_broker.app.agent_http.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties
public record ApplicationProperties(
        @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
        ApplicationProperties.Server server,
        @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
        ApplicationProperties.RabbitMq rabbitMq
) {
    private static final String ERROR_MESSAGE_PROPERTY_MISSING = "Property must be set and must not be empty.";
    private static final String ERROR_MESSAGE_PROPERTY_MIN_VALUE = "Property must be at least {value}.";
    private static final String ERROR_MESSAGE_PROPERTY_MAX_VALUE = "Property must not be larger than {value}.";

    public record Server(
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            @Min(value = 0, message = ERROR_MESSAGE_PROPERTY_MIN_VALUE)
            @Max(value = 65535, message = ERROR_MESSAGE_PROPERTY_MAX_VALUE)
            int port
    ) {
    }

    public record RabbitMq(
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            String host,
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            @Min(value = 0, message = ERROR_MESSAGE_PROPERTY_MIN_VALUE)
            @Max(value = 65535, message = ERROR_MESSAGE_PROPERTY_MAX_VALUE)
            int port,
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            @Min(value = 0, message = ERROR_MESSAGE_PROPERTY_MIN_VALUE)
            @Max(value = 65535, message = ERROR_MESSAGE_PROPERTY_MAX_VALUE)
            int managementPort,
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            String user,
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            String password,
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            String vhost,
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            String exchange,
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            String queue,
            @NotNull(message = ERROR_MESSAGE_PROPERTY_MISSING)
            String routingKey
    ) {
    }
}
