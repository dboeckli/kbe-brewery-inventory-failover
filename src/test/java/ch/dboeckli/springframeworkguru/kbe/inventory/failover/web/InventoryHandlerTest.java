package ch.dboeckli.springframeworkguru.kbe.inventory.failover.web;

import ch.guru.springframework.kbe.lib.dto.BeerInventoryDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebFluxTest
@Import({InventoryHandler.class, InventoryHandlerRouterFunction.class})
@Slf4j
class InventoryHandlerTest {

    @Autowired
    ObjectMapper objectMapper;

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
            .consumeWith(result -> {
                String jsonResponse = new String(Objects.requireNonNull(result.getResponseBodyContent()), StandardCharsets.UTF_8);
                log.info("Response:\n{}", pretty(jsonResponse));
            })
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

    private String pretty(String jsonResponse) {
        try {
            Object json = objectMapper.readValue(jsonResponse, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            // Falls kein valides JSON: unverändert zurückgeben
            return jsonResponse;
        }
    }

}