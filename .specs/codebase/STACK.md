# Tech Stack

**Analyzed:** 2026-06-15
**Status:** Pré-implementação — stack definida em `PROJECT.md` e `AGENTS.md`; nenhum manifest de dependências detectado no repositório.

## Core

- Framework: Spring Boot 3
- Language: Java 21 (backend), TypeScript (frontend)
- Runtime: JVM 21, Node.js (frontend build)
- Package manager: Maven (backend), npm/pnpm (frontend — a definir no scaffold)

## Frontend

- UI Framework: React + TypeScript
- Styling: Material UI (MUI)
- State Management: React Query (server state), React Router (navegação)
- HTTP Client: Axios
- Form Handling: A definir no scaffold (provável React Hook Form ou MUI forms)

## Backend

- API Style: REST (Spring Web MVC / Spring WebFlux — a confirmar no scaffold)
- Database: Spring Data JPA + PostgreSQL
- Authentication: Spring Security
- Validation: Spring Validation
- Messaging: Spring Kafka + Apache Kafka
- Observability: Spring Actuator + OpenTelemetry

## Testing

- Unit: JUnit 5, Mockito, AssertJ
- Integration: Spring Boot Test, Testcontainers (PostgreSQL, Kafka)
- E2E: Playwright
- Coverage target: 80% mínimo em testes unitários

## External Services

- Payments: Mercado Pago (Sprint 3 — cobranças, QR Code, webhooks)
- Banking/PIX: Banco do Brasil (Fase 3 — planejado)
- Billing/CNAB: Kobana (Fase 2 — planejado)

## Infrastructure

- Containerization: Docker, Docker Compose
- Orchestration: Kubernetes, Helm (Sprint 7)
- Observability: Prometheus, Grafana, Loki, OpenTelemetry

## Development Tools

- IDE/AI: Cursor com agentes de IA
- VCS: Git, GitHub
- Migrations: Flyway (obrigatório — `ddl-auto` proibido em produção)
- Spec workflow: tlc-spec-driven (`.specs/`)
