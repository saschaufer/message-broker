package de.saschaufer.message_broker.plugin.spi.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class Health {

    public enum Status {UNKNOWN, UP, DOWN, OUT_OF_SERVICE}

    private final Status status;
    private final Map<String, Object> details;

    private final List<Health> components;
}
