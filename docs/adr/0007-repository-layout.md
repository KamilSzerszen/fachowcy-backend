# ADR-0007: Maven multi-module monorepo on GitHub

Status: accepted (2026-11-15)

## Context
Several services are developed by one person and presented as one portfolio piece. Separate repositories would multiply CI setup and hide the whole picture.

## Decision
One Git repository with a parent POM and modules (`api-gateway`, `jobs-service`, `common-events`, more later). Maven Wrapper for reproducible builds. Trunk-based development with pull requests, Conventional Commits, semantic versioning. CI in GitHub Actions runs `./mvnw verify` (with Testcontainers), publishes coverage reports as artifacts and builds images with Jib; images go to GitHub Container Registry once the repository exists. The shared module contains only event contracts, never business logic.

## Consequences
- One place for versions, quality gates and documentation; easy to review and demo.
- Every build touches all modules (mitigated by Maven caching and `-pl` selection).
- Services can still be split into separate repositories later if teams grow.
