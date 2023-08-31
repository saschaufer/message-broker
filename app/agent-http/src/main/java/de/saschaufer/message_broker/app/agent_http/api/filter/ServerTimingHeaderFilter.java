package de.saschaufer.message_broker.app.agent_http.api.filter;

import de.saschaufer.message_broker.plugin.spi.Constants;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE + 2)
public class ServerTimingHeaderFilter implements WebFilter {

    final StopWatch stopWatch = new StopWatch();

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        
        exchange.getResponse().beforeCommit(() -> {

            stopWatch.stop();
            final long time = stopWatch.getLastTaskTimeMillis();

            final String duration = String.format("total;dur=%d;desc=\"Total duration of request\"", time);

            final String timing;

            if (exchange.getResponse().getHeaders().containsKey(Constants.Http.Header.SERVER_TIMING)) {
                timing = String.format("%s, %s", duration, exchange.getResponse().getHeaders().getFirst(Constants.Http.Header.SERVER_TIMING));
            } else {
                timing = duration;
            }

            exchange.getResponse().getHeaders().add(Constants.Http.Header.SERVER_TIMING, timing);

            return Mono.empty();
        });

        return chain.filter(exchange).doOnRequest(request -> stopWatch.start());
    }
}
