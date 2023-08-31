package de.saschaufer.message_broker.app.agent_http.api.filter;

import de.saschaufer.message_broker.plugin.spi.Constants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static de.saschaufer.message_broker.utilities.JsonSupplier.json;


@Slf4j
public class LoggingFilterRequestInterceptor extends ServerHttpRequestDecorator {

    public LoggingFilterRequestInterceptor(final ServerHttpRequest delegate) {
        super(delegate);
    }

    @Override
    public Flux<DataBuffer> getBody() {

        return super.getBody().doOnNext(dataBuffer -> {
            
            String body = "Could not read body";
            Throwable throwable = null;

            try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                Channels.newChannel(stream).write(dataBuffer.toByteBuffer().asReadOnlyBuffer());
                body = stream.toString(StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throwable = e;
            }

            LoggingEventBuilder logBuilder = log.atInfo()
                    .setMessage("Request received.")
                    .addKeyValue(Constants.Logging.CORRELATION_ID, getDelegate().getHeaders().getFirst(Constants.Http.Header.CORRELATION_ID))
                    .addKeyValue(Constants.Logging.METHOD, getDelegate().getMethod().name())
                    .addKeyValue(Constants.Logging.ENDPOINT, getDelegate().getPath().value())
                    .addKeyValue(Constants.Logging.HEADER, json(getDelegate().getHeaders().entrySet().stream().map(entry -> {

                        if (entry.getValue() == null) {
                            return Map.entry(entry.getKey(), null);
                        }

                        return switch (entry.getValue().size()) {
                            case 1 -> Map.entry(entry.getKey(), entry.getValue().get(0));
                            default -> Map.entry(entry.getKey(), entry.getValue());
                        };
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))::get)
                    .addKeyValue(Constants.Logging.PAYLOAD, body);

            if (throwable != null) {
                logBuilder = logBuilder.setCause(throwable);
            }

            logBuilder.log();
        });
    }
}
