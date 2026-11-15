package pl.kajeto.jobs.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakRealmRoleConverterTest {

  private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

  private static Jwt jwtWith(Map<String, Object> claims) {
    return Jwt.withTokenValue("token").header("alg", "none").claims(c -> c.putAll(claims)).build();
  }

  @Test
  void mapsRealmRolesToRoleAuthorities() {
    Jwt jwt =
        jwtWith(Map.of("sub", "u1", "realm_access", Map.of("roles", List.of("OWNER", "WORKER"))));

    assertThat(converter.convert(jwt))
        .extracting(GrantedAuthority::getAuthority)
        .containsExactlyInAnyOrder("ROLE_OWNER", "ROLE_WORKER");
  }

  @Test
  void returnsEmptyWhenRealmAccessMissing() {
    assertThat(converter.convert(jwtWith(Map.of("sub", "u1")))).isEmpty();
  }

  @Test
  void returnsEmptyWhenRolesMissing() {
    assertThat(converter.convert(jwtWith(Map.of("sub", "u1", "realm_access", Map.of())))).isEmpty();
  }

  @Test
  void ignoresNonStringRoles() {
    Jwt jwt = jwtWith(Map.of("sub", "u1", "realm_access", Map.of("roles", List.of("OWNER", 5))));

    assertThat(converter.convert(jwt))
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_OWNER");
  }
}
