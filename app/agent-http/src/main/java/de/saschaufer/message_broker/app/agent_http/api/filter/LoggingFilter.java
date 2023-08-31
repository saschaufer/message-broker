package de.saschaufer.message_broker.app.agent_http.api.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
@Order(value = Ordered.HIGHEST_PRECEDENCE + 1)
public class LoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {

        final ServerWebExchangeDecorator exchangeDecorator = new ServerWebExchangeDecorator(exchange) {

            @Override
            public ServerHttpRequest getRequest() {
                return new LoggingFilterRequestInterceptor(super.getRequest());
            }

            @Override
            public ServerHttpResponse getResponse() {
                return new LoggingFilterResponseInterceptor(super.getResponse());
            }
        };

        return chain.filter(exchangeDecorator);
    }
}
