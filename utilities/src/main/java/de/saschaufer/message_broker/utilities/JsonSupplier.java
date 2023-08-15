package de.saschaufer.message_broker.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.function.Supplier;

@RequiredArgsConstructor(staticName = "json")
public class JsonSupplier implements Supplier<String> {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules() // To register jackson-datatype-jsr310 for parsing of LocalDateTime.
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm a z"))
            .build();
    private final Object object;

    @Override
    public String get() {

        if (object == null) {
            return null;
        }

        if (object instanceof String) {
            return (String) object;
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
