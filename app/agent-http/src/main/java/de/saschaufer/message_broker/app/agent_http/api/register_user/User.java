package de.saschaufer.message_broker.app.agent_http.api.register_user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class User {
    enum SEX {male, female, unknown}
    
    @NotNull
    @NotBlank
    @Schema(example = "John")
    private String firstName;
    @NotNull
    @NotBlank
    @Schema(example = "Doe")
    private String lastName;
    @Schema(example = "Sir")
    private String title;
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
