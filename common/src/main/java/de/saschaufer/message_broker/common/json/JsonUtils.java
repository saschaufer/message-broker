package de.saschaufer.message_broker.common.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    private static DateFormat getDateFormat() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat;
    }

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .defaultDateFormat(getDateFormat())
            .addModule(new JavaTimeModule())
            .addModules(new SimpleModule().addSerializer(MediaType.class, new MediaTypeSerializer(MediaType.class)))
            .addModules(new SimpleModule().addDeserializer(MediaType.class, new MediaTypeDeserializer(MediaType.class)))
            .build();

    public static String toJson(final Object object) throws JsonProcessingException {
        return switch (object) {
            case null -> null;
            case String s -> s;
            case byte[] b -> new String(b);
            default -> objectMapper.writeValueAsString(object);
        };
    }

    public static <T> T fromJson(final byte[] json, final Class<T> clazz) throws IOException {

        if (json == null || json.length == 0) {
            return null;
        }

        return objectMapper.readValue(json, clazz);
    }

    public static <T> T fromJson(final String json, final Class<T> clazz) throws IOException {

        if (json == null || json.isBlank()) {
            return null;
        }

        return objectMapper.readValue(json, clazz);
    }

    public static <T> T fromJson(final byte[] json, final TypeReference<T> typeReference) throws IOException {

        if (json == null || json.length == 0) {
            return null;
        }

        return objectMapper.readValue(json, typeReference);
    }

    public static <T> T fromJson(final String json, final TypeReference<T> typeReference) throws IOException {

        if (json == null || json.isBlank()) {
            return null;
        }

        return objectMapper.readValue(json, typeReference);
    }
}
