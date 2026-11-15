package pl.kajeto.jobs.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/** Maps Keycloak realm roles (claim realm_access.roles) to ROLE_* authorities. */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    Object realmAccess = jwt.getClaim("realm_access");
    if (!(realmAccess instanceof Map<?, ?> map) || !(map.get("roles") instanceof List<?> roles)) {
      return List.of();
    }
    return roles.stream()
        .filter(String.class::isInstance)
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
        .toList();
  }
}
