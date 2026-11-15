# ADR-0001: A small number of microservices behind a gateway

Status: accepted (2026-11-15)

## Context
The product is a real app for tradespeople and a portfolio project. Pure microservices from day one cost 3-5x more configuration for one developer; a pure monolith hides the distributed-systems skills the portfolio should show.

## Decision
Four services plus a gateway (`api-gateway`, `jobs-service`, `documents-service`, `notification-service`; later `workforce-service` and reporting). Each service has a real reason to be separate (heavy files, external async communication, separate personal data). Module boundaries inside a service are guarded by Spring Modulith and ArchUnit.

## Consequences
- Shows the full microservice toolbox without dozens of services.
- Needs discipline: database per service, no shared business code (only event contracts in `common-events`).
- More infrastructure than a monolith (gateway, broker, per-service deployment).
