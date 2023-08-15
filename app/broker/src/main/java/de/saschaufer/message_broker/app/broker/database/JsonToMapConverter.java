package de.saschaufer.message_broker.app.broker.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ReadingConverter
@AllArgsConstructor
public class JsonToMapConverter implements Converter<Json, Map<String, Object>> {

    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> convert(final Json json) {
        try {
            return objectMapper.readValue(json.asString(), new TypeReference<>() {
            });
        } catch (IOException e) {
            log.atError().setMessage("Problem while parsing JSON.").addKeyValue("json", json.asString()).setCause(e).log();
        }
        return new HashMap<>();
    }

}
