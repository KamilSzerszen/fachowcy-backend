# ADR-0004: Kubernetes (k3s) with staging and production, free tier first

Status: accepted (2026-11-15)

## Context
The product is meant to be sold, so it needs normal environments, HTTPS, backups and monitoring. Budget is zero until the first paying users.

## Decision
Deploy to Kubernetes (k3s) with a staging and a production environment, Helm charts and GitOps (Argo CD). Start on the Oracle Cloud free tier; move to a paid server (e.g. Hetzner) only after the owner agrees. Local development uses `docker compose`. Images are built with Jib in GitHub Actions and published to GitHub Container Registry. No spending without the owner's approval.

## Consequences
- Production-like setup that can be explained end to end.
- Free-tier machines have no availability guarantee, so a migration path is planned.
- Kubernetes, Helm and Argo CD are out of scope for stage 0 and arrive in stage 4.
