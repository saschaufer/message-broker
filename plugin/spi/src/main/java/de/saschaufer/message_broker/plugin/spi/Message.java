package de.saschaufer.message_broker.plugin.spi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter(onMethod = @__(@JsonProperty))
@Setter
@Accessors(fluent = true)
public class Message {
    String correlationId;
    String procedure;
    Map<String, Object> payload;
    LocalDateTime receptionTime;
}
