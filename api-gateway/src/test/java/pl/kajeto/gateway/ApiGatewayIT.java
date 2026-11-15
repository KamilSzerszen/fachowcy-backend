package pl.kajeto.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

/** Boots the gateway with real Redis (Testcontainers) and a stub downstream service. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"RATE_LIMIT_REPLENISH=1", "RATE_LIMIT_BURST=3"})
@AutoConfigureWebTestClient(timeout = "30s")
@Testcontainers
class ApiGatewayIT {

  private static final AtomicReference<com.sun.net.httpserver.Headers> DOWNSTREAM_HEADERS =
      new AtomicReference<>();
  private static HttpServer downstream;

  @Container
  @ServiceConnection(name = "redis")
  static GenericContainer<?> redis =
      new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  @MockitoBean private ReactiveJwtDecoder jwtDecoder;

  @Autowired private WebTestClient client;

  @BeforeAll
  static void startDownstream() throws IOException {
    downstream = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    downstream.createContext(
        "/api/v1/jobs/ping",
        exchange -> {
          DOWNSTREAM_HEADERS.set(exchange.getRequestHeaders());
          byte[] body = "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, body.length);
          exchange.getResponseBody().write(body);
          exchange.close();
        });
    downstream.start();
  }

  @AfterAll
  static void stopDownstream() {
    downstream.stop(0);
  }

  @DynamicPropertySource
  static void routeToStub(DynamicPropertyRegistry registry) throws IOException {
    // started lazily here because properties are resolved before @BeforeAll
    if (downstream == null) {
      startDownstream();
    }
    registry.add("JOBS_SERVICE_URL", () -> "http://localhost:" + downstream.getAddress().getPort());
  }

  private void stubValidToken(String token, String subject) {
    Jwt jwt =
        new Jwt(
            token,
            Instant.now(),
            Instant.now().plusSeconds(300),
            Map.of("alg", "none"),
            Map.of("sub", subject));
    when(jwtDecoder.decode(anyString()))
        .thenAnswer(
            inv ->
                token.equals(inv.getArgument(0))
                    ? Mono.just(jwt)
                    : Mono.error(
                        new org.springframework.security.oauth2.jwt.BadJwtException("bad")));
  }

  @Test
  void withoutTokenReturns401AndCorrelationId() {
    client
        .get()
        .uri("/api/v1/jobs/ping")
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectHeader()
        .exists("X-Correlation-Id");
  }

  @Test
  void healthIsPublic() {
    client.get().uri("/actuator/health").exchange().expectStatus().isOk();
  }

  @Test
  void validTokenIsProxiedWithCorrelationIdAndAuthorization() {
    stubValidToken("valid-owner", "owner-1");

    client
        .get()
        .uri("/api/v1/jobs/ping")
        .header("Authorization", "Bearer valid-owner")
        .header("X-Correlation-Id", "corr-42")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .valueEquals("X-Correlation-Id", "corr-42");

    assertThat(DOWNSTREAM_HEADERS.get().getFirst("X-Correlation-Id")).isEqualTo("corr-42");
    assertThat(DOWNSTREAM_HEADERS.get().getFirst("Authorization")).isEqualTo("Bearer valid-owner");
  }

  @Test
  void generatesCorrelationIdForDownstreamWhenMissing() {
    stubValidToken("valid-gen", "owner-2");

    client
        .get()
        .uri("/api/v1/jobs/ping")
        .header("Authorization", "Bearer valid-gen")
        .exchange()
        .expectStatus()
        .isOk();

    assertThat(DOWNSTREAM_HEADERS.get().getFirst("X-Correlation-Id")).isNotBlank();
  }

  @Test
  void rateLimitReturns429AfterBurstIsExhausted() {
    stubValidToken("valid-burst", "burst-user");

    List<Integer> statuses =
        java.util.stream.IntStream.range(0, 10)
            .mapToObj(
                i ->
                    client
                        .get()
                        .uri("/api/v1/jobs/ping")
                        .header("Authorization", "Bearer valid-burst")
                        .exchange()
                        .returnResult(String.class)
                        .getStatus()
                        .value())
            .toList();

    assertThat(statuses).contains(200).contains(429);
  }
}
