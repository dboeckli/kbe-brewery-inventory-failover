package ch.dboeckli.springframeworkguru.kbe.inventory.failover.config;

import ch.dboeckli.springframeworkguru.kbe.inventory.failover.web.InventoryHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> orderRouter(InventoryHandler handler) {
        return route(GET("/inventory-failover").and(accept(APPLICATION_JSON)), handler::listInventory);
    }
}
