package de.saschaufer.message_broker.app.file_storage.config;

import de.saschaufer.message_broker.app.file_storage.api.FileStorageController;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.version}")
    private String version;

    @Bean
    public OpenAPI openAPI() {
        //@formatter:off
        return new OpenAPI()
                .info(new Info()
                        .title("File Storage")
                        .description("""
                            A service providing APIs to store files.
                            
                            Swagger UI: /swagger-ui.html
                            
                            JSON      : /v3/api-docs
                            
                            YAML      : /v3/api-docs.yaml
                            """
                        )
                        .version(version)
                );
        //@formatter:on
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("file-storage")
                .packagesToScan(FileStorageController.class.getPackageName())
                .build();
    }
}
