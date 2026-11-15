# ADR-0006: PostgreSQL per service, Redis, S3-compatible object storage

Status: accepted (2026-11-15)

## Context
Data differs in shape and lifetime: core records, short-lived or frequently read data, and large files. The app is offline-first, so records need versions for synchronization.

## Decision
- PostgreSQL for primary data, one database and one user per service (init script in `infra/postgres`), no cross-service table access. Schema changes only through Flyway migrations (never edit an applied one). Hibernate runs with `ddl-auto: validate`.
- Records carry a `version` column (optimistic locking now, offline sync later).
- Redis for data that may be lost: rate limiting in the gateway now; cache, idempotency keys and SMS codes later.
- S3-compatible object storage (MinIO locally) for photos, receipts and recordings; files never go into the database.

## Consequences
- Clear ownership of data, services can be deployed and scaled independently.
- Cross-service queries need events or API calls instead of joins.
- The community MinIO images are no longer published on Docker Hub or Quay, so local compose uses the Chainguard MinIO build. Production can use any S3-compatible storage.
