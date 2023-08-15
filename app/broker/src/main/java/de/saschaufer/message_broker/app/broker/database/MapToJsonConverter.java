package de.saschaufer.message_broker.app.broker.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.Map;

import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
@WritingConverter
@AllArgsConstructor
public class MapToJsonConverter implements Converter<Map<String, Object>, Json> {

    private final ObjectMapper objectMapper;

    @Override
    public Json convert(Map<String, Object> source) {
        try {
            return Json.of(objectMapper.writeValueAsString(source));
        } catch (JsonProcessingException e) {
            log.atError().setMessage("Error occurred while serializing Map to JSON.").addKeyValue("map", json(source)::get).setCause(e).log();
        }
        return Json.of("");
    }

}
