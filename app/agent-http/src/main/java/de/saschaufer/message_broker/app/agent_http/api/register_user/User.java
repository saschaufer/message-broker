package de.saschaufer.message_broker.app.agent_http.api.register_user;

import de.saschaufer.message_broker.app.agent_http.config.validator.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class User {
    enum SEX {male, female, unknown}

    @NotBlank
    @Schema(example = "John")
    private String firstName;
    @NotBlank
    @Schema(example = "Doe")
    private String lastName;
    @NotBlank(nullable = true)
    @Schema(example = "Sir")
    private String title;
    @NotBlank(nullable = true)
    @Schema(example = "BSc")
    private String degree;
    @NotNull
    @Schema(example = "male")
    private SEX sex;

    public Map<String, Object> toMap() {

        final Map<String, Object> map = new HashMap<>();
        map.put("firstName", getFirstName());
        map.put("lastName", getLastName());
        map.put("title", getTitle());
        map.put("degree", getDegree());
        map.put("sex", getSex().name());

        return map;
    }
}
