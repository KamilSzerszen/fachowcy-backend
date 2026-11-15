package pl.fachowcy.gateway.config;

import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Ensures every request has an X-Correlation-Id: keeps the one sent by the client or generates a
 * new one, forwards it to the downstream service and returns it in the response. Runs before
 * security so that even 401 responses carry the id.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements WebFilter {

  public static final String HEADER = "X-Correlation-Id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String incoming = exchange.getRequest().getHeaders().getFirst(HEADER);
    String correlationId =
        (incoming == null || incoming.isBlank()) ? UUID.randomUUID().toString() : incoming;

    ServerHttpRequest request =
        exchange.getRequest().mutate().headers(h -> h.set(HEADER, correlationId)).build();
    exchange.getResponse().getHeaders().set(HEADER, correlationId);
    return chain.filter(exchange.mutate().request(request).build());
  }
}
