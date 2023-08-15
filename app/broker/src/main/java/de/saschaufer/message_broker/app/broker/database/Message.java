package de.saschaufer.message_broker.app.broker.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter(onMethod = @__(@JsonProperty))
@Setter
@Accessors(fluent = true)
@Table(name = "messages")
public class Message {

    public enum Status {waiting_for_processing, in_process, finished, finished_with_error}

    @Id
    private Long id;
    private String correlationId;
    private Status status;
    private String procedure;
    private String previousStep;
    private String nextStep;
    private Map<String, Object> payload;
    private String error;
    private LocalDateTime receptionTime;
    private LocalDateTime lastChangedTime;
}
