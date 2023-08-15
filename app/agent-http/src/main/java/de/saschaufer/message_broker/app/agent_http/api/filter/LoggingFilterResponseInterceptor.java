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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.saschaufer.message_broker.utilities.JsonSupplier.json;

@Slf4j
public class LoggingFilterResponseInterceptor extends ServerHttpResponseDecorator {
    private final String endpoint;
    private final long startTime;

    public LoggingFilterResponseInterceptor(final ServerHttpResponse delegate, final String endpoint, final long startTime) {
        super(delegate);
        this.endpoint = endpoint;
        this.startTime = startTime;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> bodyBuffer) {

        final Flux<DataBuffer> buffer = Flux.from(bodyBuffer);
        return super.writeWith(buffer.doOnNext(dataBuffer -> {

            final String correlationId = getDelegate().getHeaders().getOrDefault(Constants.Http.Header.CORRELATION_ID, List.of("")).get(0);

            String body = "Could not read body";
            Throwable throwable = null;

            try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                Channels.newChannel(stream).write(dataBuffer.toByteBuffer().asReadOnlyBuffer());
                body = stream.toString(StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throwable = e;
            }

            // For all endpoints, but could be restricted here.
            if (endpoint.startsWith("/")) {

                final String duration = String.format("total;dur=%d;desc=\"Total duration of request\"", (System.currentTimeMillis() - startTime));

                String timing = getDelegate().getHeaders().getFirst(Constants.Http.Header.SERVER_TIMING);

                if (timing == null) {
                    timing = duration;
                } else {
                    timing = String.format("%s, %s", duration, timing);
                }

                getDelegate().getHeaders().add(Constants.Http.Header.SERVER_TIMING, timing);
            }

            LoggingEventBuilder logBuilder = log.atInfo()
                    .setMessage("Response sent.")
                    .addKeyValue(Constants.Logging.CORRELATION_ID, correlationId)
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
