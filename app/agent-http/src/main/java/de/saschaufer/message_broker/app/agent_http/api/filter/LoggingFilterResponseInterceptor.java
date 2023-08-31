package de.saschaufer.message_broker.app.agent_http.api.filter;

import de.saschaufer.message_broker.plugin.spi.Constants;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
public class LoggingFilterResponseInterceptor extends ServerHttpResponseDecorator {

    public LoggingFilterResponseInterceptor(final ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> bodyBuffer) {

        final Flux<DataBuffer> buffer = Flux.from(bodyBuffer);
        return super.writeWith(buffer.doOnNext(dataBuffer -> {

            String body = "Could not read body";
            Throwable throwable = null;

            try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                Channels.newChannel(stream).write(dataBuffer.toByteBuffer().asReadOnlyBuffer());
                body = stream.toString(StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throwable = e;
            }

            LoggingEventBuilder logBuilder = log.atInfo()
                    .setMessage("Response sent.")
                    .addKeyValue(Constants.Logging.CORRELATION_ID, getDelegate().getHeaders().getFirst(Constants.Http.Header.CORRELATION_ID))
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
        }));
    }
}
