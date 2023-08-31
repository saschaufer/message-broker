package de.saschaufer.message_broker.app.agent_http.api.filter;

import de.saschaufer.message_broker.plugin.spi.Constants;
import de.saschaufer.message_broker.utilities.Ksuid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdHeaderFilter implements WebFilter {

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {

        final CorrelationId correlation = new CorrelationId();
        correlation.id = Ksuid.generate();

        exchange.getResponse().beforeCommit(() -> {

            if (!exchange.getResponse().getHeaders().containsKey(Constants.Http.Header.CORRELATION_ID)) {

                log.atInfo().setMessage("Response has no CorrelationId (Header '{}'). Give it the one from the request.")
                        .addArgument(Constants.Http.Header.CORRELATION_ID)
                        .addKeyValue(Constants.Logging.CORRELATION_ID, correlation.id)
                        .log();

                exchange.getResponse().getHeaders().add(Constants.Http.Header.CORRELATION_ID, correlation.id);
            }

            return Mono.empty();
        });

        return chain.filter(exchange).doOnRequest(request -> {

            // If there is a CorrelationId, save it and don't generate a new one.
            if (exchange.getRequest().getHeaders().containsKey(Constants.Http.Header.CORRELATION_ID)) {
                correlation.id = exchange.getRequest().getHeaders().getFirst(Constants.Http.Header.CORRELATION_ID);
                return;
            }

            log.atInfo().setMessage("Request has no CorrelationId (Header '{}'). Generate one.")
                    .addArgument(Constants.Http.Header.CORRELATION_ID)
                    .addKeyValue(Constants.Logging.CORRELATION_ID, correlation.id)
                    .log();

            exchange.getRequest().getHeaders().add(Constants.Http.Header.CORRELATION_ID, correlation.id);
        });
    }

    /**
     * If we want to change a variable inside a lambda expression we get the following error:
     * <blockquote><i>Variable used in lambda expression should be final or effectively final.</i></blockquote>
     * To overcome this error, we wrap the variable inside an object and make the object final.
     */
    private class CorrelationId {
        public String id;
    }
}
