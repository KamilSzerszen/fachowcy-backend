package pl.kajeto.gateway.config;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  /** Role checks stay in the services; the gateway only verifies the token is valid. */
  @Bean
  SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchange ->
                exchange
                    .pathMatchers("/actuator/health/**", "/actuator/info")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}))
        .build();
  }

  /** Rate limit per authenticated user (JWT subject), fallback to client address. */
  @Bean
  KeyResolver userKeyResolver() {
    return exchange ->
        ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication().getName())
            .switchIfEmpty(
                Mono.fromSupplier(
                    () ->
                        Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                            .map(a -> a.getAddress().getHostAddress())
                            .orElse("anonymous")));
  }
}
