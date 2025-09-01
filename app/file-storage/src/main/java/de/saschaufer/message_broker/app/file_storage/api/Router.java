package de.saschaufer.message_broker.app.file_storage.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
public class Router {

    public static final String FILE_STORAGE_PATH_V1 = "/file-storage/v1";

    @Bean
    @RouterDoc
    public RouterFunction<ServerResponse> routes(final Handler handler) {
        return route()
                .path(FILE_STORAGE_PATH_V1, builder -> builder
                        .POST(handler::postFiles)
                        .GET(handler::getFiles)
                )
                .build();
    }
}
