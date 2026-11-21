package ch.dboeckli.springframeworkguru.kbe.inventory.failover.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingWebFilterTest {

    private final RequestLoggingWebFilter filter = new RequestLoggingWebFilter();

    private final Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingWebFilter.class);

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private Level originalLevel;

    @BeforeEach
    void setUp() {
        originalLevel = logger.getLevel();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        logger.setLevel(originalLevel);
    }

    @Test
    void filterShouldLogRequestAndResponseWhenDebugEnabled() {
        logger.setLevel(Level.DEBUG);
        MockServerWebExchange exchange = exchangeWithStatus(HttpStatus.OK);

        StepVerifier.create(filter.filter(exchange, chainWithStatus(HttpStatus.OK))).verifyComplete();

        assertThat(appender.list).extracting(ILoggingEvent::getFormattedMessage)
            .containsExactly("Before request [GET /inventory-failover]",
                    "After request [GET /inventory-failover] status=200 OK");
    }

    @Test
    void filterShouldNotLogWhenDebugDisabled() {
        logger.setLevel(Level.INFO);
        MockServerWebExchange exchange = exchangeWithStatus(HttpStatus.ACCEPTED);

        StepVerifier.create(filter.filter(exchange, chainWithStatus(HttpStatus.ACCEPTED))).verifyComplete();

        assertThat(appender.list).isEmpty();
    }

    private MockServerWebExchange exchangeWithStatus(HttpStatus status) {
        MockServerWebExchange exchange = MockServerWebExchange
            .from(MockServerHttpRequest.method(HttpMethod.GET, "/inventory-failover").build());
        exchange.getResponse().setStatusCode(status);
        return exchange;
    }

    private WebFilterChain chainWithStatus(HttpStatus status) {
        return exchange -> {
            exchange.getResponse().setStatusCode(status);
            return Mono.empty();
        };
    }

}
