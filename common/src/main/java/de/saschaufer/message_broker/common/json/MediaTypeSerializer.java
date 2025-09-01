package de.saschaufer.message_broker.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.http.MediaType;

import java.io.IOException;

public class MediaTypeSerializer extends StdSerializer<MediaType> {

    public MediaTypeSerializer(final Class<MediaType> t) {
        super(t);
    }

    @Override
    public void serialize(final MediaType value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {

        if (value != null) {
            generator.writeString(value.toString());
        } else {
            generator.writeNull();
        }
    }
}
