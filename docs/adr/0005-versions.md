# ADR-0005: Java 21 and Spring Boot 3.x

Status: accepted (2026-11-15)

## Context
Job offers and production systems mostly run Java 17/21 and Spring Boot 3. Newer majors exist (Spring Boot 4), but the project should match what employers use today.

## Decision
- Java 21 (LTS), compiled with `--release 21` even when a newer JDK is installed locally.
- Spring Boot 3.5.16 and Spring Cloud 2025.0.3 (compatible train), springdoc 2.8.17, ArchUnit 1.4.2, JaCoCo 0.8.14, Jib 3.4.6.
- Versions are managed in the parent POM through imported BOMs. Upgrading to newer majors is a separate, documented step.

## Consequences
- Familiar, widely deployed stack; long community support.
- Boot 3.5 reaches end of open-source support earlier than Boot 4, so a planned upgrade is part of the roadmap.
- Dependabot keeps patch and minor versions current.
