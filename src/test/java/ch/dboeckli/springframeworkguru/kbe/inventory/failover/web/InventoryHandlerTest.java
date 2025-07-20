package ch.dboeckli.springframeworkguru.kbe.inventory.failover.web;

import ch.guru.springframework.kbe.lib.dto.BeerInventoryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebFluxTest
@Import({InventoryHandler.class, InventoryHandlerRouterFunction.class})
class InventoryHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testListInventory() {
        webTestClient.get()
            .uri("/inventory-failover")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(BeerInventoryDto.class)
            .value(list -> {
                assertEquals(1, list.size());
                BeerInventoryDto dto = list.getFirst();
                assertNotNull(dto.getId());
                assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000000"), dto.getBeerId());
                assertEquals(999, dto.getQuantityOnHand());
                assertNotNull(dto.getCreatedDate());
                assertNotNull(dto.getLastModifiedDate());
            });
    }

}