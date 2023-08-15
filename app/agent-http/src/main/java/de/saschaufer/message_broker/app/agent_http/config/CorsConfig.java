package de.saschaufer.message_broker.app.agent_http.config;

import de.saschaufer.message_broker.app.agent_http.api.register_user.RegisterUserApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    CorsWebFilter corsWebFilter() {

        final CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        corsConfig.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.POST.name()));
        corsConfig.setAllowedOriginPatterns(List.of("http://localhost:*"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(RegisterUserApi.ROOT + "/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
