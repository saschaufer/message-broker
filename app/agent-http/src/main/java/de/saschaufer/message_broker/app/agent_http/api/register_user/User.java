package de.saschaufer.message_broker.app.agent_http.api.register_user;

import de.saschaufer.message_broker.app.agent_http.config.validator.NotBlank;
import de.saschaufer.message_broker.app.agent_http.config.validator.OneOf;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class User {
    private final String male = "male";
    private final String female = "female";
    private final String unknown = "unknown";

    @NotBlank(nullable = false)
    @Schema(example = "John")
    private String firstName;
    @NotBlank(nullable = false)
    @Schema(example = "Doe")
    private String lastName;
    @NotBlank(nullable = true)
    @Schema(example = "Sir")
    private String title;
    @NotBlank(nullable = true)
    @Schema(example = "BSc")
    private String degree;
    @NotNull
    @OneOf(allowableValues = {male, female, unknown})
    @Schema(allowableValues = {male, female, unknown}, example = male)
    private String sex;

    public Map<String, Object> toMap() {

        final Map<String, Object> map = new HashMap<>();
        map.put("firstName", getFirstName());
        map.put("lastName", getLastName());
        map.put("title", getTitle());
        map.put("degree", getDegree());
        map.put("sex", getSex());

        return map;
    }
}
