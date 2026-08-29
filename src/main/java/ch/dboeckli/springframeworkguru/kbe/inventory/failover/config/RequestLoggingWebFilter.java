package ch.dboeckli.springframeworkguru.kbe.inventory.failover.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class RequestLoggingWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (log.isDebugEnabled()) {
            log.debug("Before request [{} {}]", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
        }
        return chain.filter(exchange).doFinally(signalType -> {
            if (log.isDebugEnabled()) {
                log.debug("After request [{} {}] status={}", exchange.getRequest().getMethod(),
                        exchange.getRequest().getURI(), exchange.getResponse().getStatusCode());
            }
        });
    }

}
