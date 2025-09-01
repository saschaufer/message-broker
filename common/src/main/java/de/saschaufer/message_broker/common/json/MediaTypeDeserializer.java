package de.saschaufer.message_broker.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.http.MediaType;

import java.io.IOException;

public class MediaTypeDeserializer extends StdDeserializer<MediaType> {

    public MediaTypeDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public MediaType deserialize(final JsonParser jsonParser, final DeserializationContext context) throws IOException {
        final JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        return MediaType.valueOf(jsonNode.asText());
    }
}
