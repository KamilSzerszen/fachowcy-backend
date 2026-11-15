# ADR-0002: Kafka (KRaft) as message broker, outbox for publishing

Status: accepted (2026-11-15)

## Context
Services must react to events such as "job finished", "payment added", "receipt added" (notifications, reports) without synchronous coupling. RabbitMQ and Kafka were considered.

## Decision
Apache Kafka in KRaft mode (no ZooKeeper). Events are published with the transactional outbox pattern so no event is lost when a service crashes between the database commit and the publish. Event contracts are defined contract-first in `common-events`.

## Consequences
- Industry standard, strong portfolio value, replayable event log.
- Heavier to run than RabbitMQ (acceptable: single node locally).
- Consumers must be idempotent. In stage 0 Kafka only runs in docker compose; producers and the outbox come in stage 2.
