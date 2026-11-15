# ADR-0003: Keycloak for authentication (OAuth2 / OpenID Connect)

Status: accepted (2026-11-15)

## Context
The app needs accounts, roles (`OWNER`, `WORKER`, later customers) and secure tokens. Writing our own identity service is risky and adds no portfolio value beyond what an integration shows.

## Decision
Use Keycloak as identity provider. Clients get a JWT; the gateway and every service validate it as an OAuth2 resource server. Realm roles are mapped to Spring `ROLE_*` authorities. The realm is stored as code (`infra/keycloak/realm-fachowcy.json`, dev only credentials). Role checks stay in the services, the gateway only verifies that the token is valid.

## Consequences
- Standard solution, security handled by a mature product (password policy, MFA, social login later).
- One more container to run and upgrade.
- Inside Docker the issuer must stay stable: Keycloak is started with a fixed public hostname (`http://localhost:8180`) and dynamic backchannel, services fetch keys from `keycloak:8080` but validate the issuer against the public URL.
