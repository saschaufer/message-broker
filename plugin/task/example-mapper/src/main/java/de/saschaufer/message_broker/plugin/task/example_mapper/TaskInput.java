package de.saschaufer.message_broker.plugin.task.example_mapper;

import java.util.HashMap;
import java.util.Map;

public record TaskInput(Integer integer, String string, Boolean bool) {

    public static Map<String, Object> toMap(final TaskInput taskInput) {

        final Map<String, Object> map = new HashMap<>();

        map.put("integer", taskInput.integer());
        map.put("string", taskInput.string());
        map.put("bool", taskInput.bool());

        return map;
    }

    public static TaskInput fromMap(final Map<String, Object> payload) {

        final Integer integer = (Integer) payload.get("integer");
        final String string = (String) payload.get("string");
        final Boolean bool = (Boolean) payload.get("bool");

        return new TaskInput(integer, string, bool);
    }
}
