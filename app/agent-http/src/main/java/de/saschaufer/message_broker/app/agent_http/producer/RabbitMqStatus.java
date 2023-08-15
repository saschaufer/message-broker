package de.saschaufer.message_broker.app.agent_http.producer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RabbitMqStatus {
    private String error;
    private String reason;
}
