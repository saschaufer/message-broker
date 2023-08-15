package de.saschaufer.message_broker.plugin.task.example_mapper;

import java.util.HashMap;
import java.util.Map;

public record TaskOutput(String exampleString) {

    public static Map<String, Object> toMap(final TaskOutput taskOutput) {

        final Map<String, Object> map = new HashMap<>();

        map.put("example-string", taskOutput.exampleString());

        return map;
    }

    public static TaskOutput fromMap(final Map<String, Object> payload) {

        final String exampleString = (String) payload.get("example-string");

        return new TaskOutput(exampleString);
    }
}
