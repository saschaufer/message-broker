package de.saschaufer.message_broker.app.agent_http.api.filter;

import de.saschaufer.message_broker.plugin.spi.Constants;
import de.saschaufer.message_broker.utilities.Ksuid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {

        final long startTime = System.currentTimeMillis();
        final String endpoint = exchange.getRequest().getPath().value();

        if (!exchange.getRequest().getHeaders().containsKey(Constants.Http.Header.CORRELATION_ID)) {
            final String correlationId = Ksuid.generate();
            log.atInfo().setMessage("Request has no CorrelationId. Generate one.").addKeyValue(Constants.Logging.CORRELATION_ID, correlationId).log();
            exchange.getRequest().mutate().header(Constants.Http.Header.CORRELATION_ID, correlationId).build();
        }

        final ServerWebExchangeDecorator exchangeDecorator = new ServerWebExchangeDecorator(exchange) {

            @Override
            public ServerHttpRequest getRequest() {
                return new LoggingFilterRequestInterceptor(super.getRequest());
            }

            @Override
            public ServerHttpResponse getResponse() {
                return new LoggingFilterResponseInterceptor(super.getResponse(), endpoint, startTime);
            }
        };

        return chain.filter(exchangeDecorator);
    }
}
