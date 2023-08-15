package de.saschaufer.message_broker.app.agent_http.config;

import de.saschaufer.message_broker.app.agent_http.api.register_user.RegisterUserApi;
import de.saschaufer.message_broker.app.agent_http.management.UserAgent;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private final Contact CONTACT = new Contact()
            .name("You")
            .email("some@email.com")
            .url("http://something.com");

    private final OpenApiCustomizer noAdditionalProperties = openApi -> {
        if (openApi.getComponents().getSchemas() != null) {
            openApi.getComponents().getSchemas().values().forEach(schema -> schema.setAdditionalProperties(false));
        }
    };

    @Bean
    public OpenAPI common(final UserAgent userAgent) {
        return new OpenAPI()
                .specVersion(SpecVersion.V31)
                .info(new Info()
                        .contact(CONTACT)
                )
                .addServersItem(new Server()
                        .url("{schema}://{host}:{port}")
                        .variables(new ServerVariables()
                                .addServerVariable("schema", new ServerVariable()._default("http"))
                                .addServerVariable("host", new ServerVariable()._default(userAgent.getHostName()))
                                .addServerVariable("port", new ServerVariable()._default(userAgent.getPort()))
                        )
                        .description("custom")
                )
                .addServersItem(new Server()
                        .url("/")
                        .description("same machine")
                );
    }

    @Bean
    public GroupedOpenApi registerUserOpenApi() {
        return GroupedOpenApi.builder()
                .group(RegisterUserApi.TAG)
                .packagesToScan(RegisterUserApi.class.getPackageName())
                .addOpenApiCustomizer(openApi -> openApi
                        .info(new Info()
                                .version(RegisterUserApi.VERSION)
                                .title(RegisterUserApi.TITLE)
                                .description(
                                        "_(Download OpenAPI YAML: Add /yaml to /openapi/" + RegisterUserApi.TAG + ")_\n\n"
                                                + RegisterUserApi.DESCRIPTION
                                )
                                .contact(CONTACT)
                        )
                        .tags(List.of(new Tag()
                                .name(RegisterUserApi.TAG)
                                .description(RegisterUserApi.DESCRIPTION)
                        ))

                )
                .addOpenApiCustomizer(noAdditionalProperties)
                .build();
    }
}
