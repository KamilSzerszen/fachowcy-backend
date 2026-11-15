package pl.kajeto.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class CorrelationIdFilterTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  @Test
  void keepsIncomingCorrelationId() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/x").header(CorrelationIdFilter.HEADER, "abc-123"));
    AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

    filter
        .filter(
            exchange,
            e -> {
              forwarded.set(e);
              return Mono.empty();
            })
        .block();

    assertThat(forwarded.get().getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER))
        .isEqualTo("abc-123");
    assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER))
        .isEqualTo("abc-123");
  }

  @Test
  void generatesCorrelationIdWhenMissingOrBlank() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/x").header(CorrelationIdFilter.HEADER, " "));
    AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

    filter
        .filter(
            exchange,
            e -> {
              forwarded.set(e);
              return Mono.empty();
            })
        .block();

    String id = forwarded.get().getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER);
    assertThat(id).isNotBlank();
    assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER))
        .isEqualTo(id);
  }
}
