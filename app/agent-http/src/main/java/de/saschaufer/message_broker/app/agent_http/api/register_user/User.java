package de.saschaufer.message_broker.app.agent_http.api.register_user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

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
    @Schema(example = "Sir")
    private String title;
    @Schema(example = "BSc")
    private String degree;
    @NotNull
    @Schema(example = "male")
    private SEX sex;

    public Map<String, Object> toMap() {
        return Map.of(
                "firstName", getFirstName(),
                "lastName", getLastName(),
                "title", getTitle(),
                "degree", getDegree(),
                "sex", getSex().name()
        );
    }
}
