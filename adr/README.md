# Architecture Decision Records

Decisões arquiteturais do Financial Platform Lab.

Formato: [MADR](https://adr.github.io/madr/) — Contexto, Decisão, Consequências (Positivas, Negativas, Neutras).

| ADR | Título | Status |
|-----|--------|--------|
| [0001](0001-modular-monolith-hexagonal-architecture.md) | Monólito Modular com Arquitetura Hexagonal e Vertical Slice | Accepted |
| [0002](0002-postgresql-as-primary-database.md) | PostgreSQL como Banco de Dados Relacional Principal | Accepted |
| [0003](0003-kafka-event-driven-architecture.md) | Arquitetura Event-Driven com Apache Kafka | Accepted |
| [0004](0004-observability-stack.md) | Stack de Observabilidade | Accepted |
| [0005](0005-spring-security-authentication.md) | Autenticação e Segurança com Spring Security | Accepted |
| [0006](0006-ledger-first-double-entry.md) | Ledger-First com Partidas Dobradas | Accepted |
| [0007](0007-mercadopago-orders-api.md) | Integração de Cobranças via Mercado Pago Orders API | Accepted |

## Referências cruzadas

| ADR | STATE.md | Outros |
|-----|----------|--------|
| 0001 | AD-002 | ARCHITECTURE.md, AGENTS.md |
| 0006 | AD-003 | AGENTS.md Rule 3 |
| 0007 | AD-004 | docs/integrations/mercadopago/ |
