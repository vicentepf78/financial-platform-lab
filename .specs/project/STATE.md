# State

**Last Updated:** 2026-06-15
**Current Work:** Sprint 1 Execute — `query-customers` concluída; próxima feature: `transfer-money`

---

## Recent Decisions (Last 60 days)

### AD-001: Estrutura .specs/ com tlc-spec-driven (2026-06-15)

**Decision:** Adotar estrutura `.specs/` do tlc-spec-driven como fonte de verdade para planejamento, separada do `PROJECT.md` raiz (visão detalhada) e `AGENTS.md` (regras para IA).
**Reason:** Alinhar fluxo Spec → Testes → Implementação com memória persistente entre sessões.
**Trade-off:** Duplicação parcial com `PROJECT.md` raiz; `.specs/project/PROJECT.md` é a versão condensada operacional.
**Impact:** Features seguem `.specs/features/[feature]/spec.md` com IDs rastreáveis.

### AD-002: Monólito modular como arquitetura inicial (2026-06-15)

**Decision:** Monorepo + Modular Monolith + Hexagonal + Vertical Slice, sem microserviços na v1.
**Reason:** Menor complexidade operacional, melhor produtividade com IA, evolução gradual para microserviços.
**Trade-off:** Limites de escala horizontal por módulo até eventual extração.
**Impact:** Cada módulo (customer, account, ledger, etc.) é boundary lógico com ports/adapters explícitos.
**ADR:** [0001](../../adr/0001-modular-monolith-hexagonal-architecture.md)

### AD-003: Ledger-first — saldo como projeção (2026-06-15)

**Decision:** Nenhum saldo alterado diretamente; toda operação financeira gera débito + crédito no ledger.
**Reason:** Regra fundamental de domínio financeiro e auditabilidade.
**Trade-off:** Consulta de saldo requer agregação de lançamentos (otimizável com projeções/materialized views).
**Impact:** Bloqueia qualquer implementação que use `account.setBalance(...)`.
**ADR:** [0006](../../adr/0006-ledger-first-double-entry.md)

### AD-004: Mercado Pago Orders API para cobranças (2026-06-15)

**Decision:** Integrar cobranças PIX via **Checkout Transparente / Orders API** (`POST /v1/orders`, `type: online`), não via Payments API legacy (`/v1/payments`).
**Reason:** Recomendação oficial do MP para novas integrações; webhooks unificados (topic `order`); alinhamento com MCP e doc `checkout-api-orders`.
**Trade-off:** Curva de aprendizado da Orders API; distinção online vs QR presencial (`type: qr`).
**Impact:** Sprint 3 documentada em `docs/integrations/mercadopago/`; `.specs/codebase/INTEGRATIONS.md` atualizado.
**ADR:** [0007](../../adr/0007-mercadopago-orders-api.md)

### AD-005: ADRs iniciais formalizados (2026-06-15)

**Decision:** 7 ADRs em `adr/` cobrindo arquitetura, banco, mensageria, observabilidade, segurança, ledger e Mercado Pago.
**Reason:** AGENTS.md exige ADR para toda decisão arquitetural; formalizar decisões já tomadas no STATE.
**Impact:** Referência estável para implementação e revisões futuras.

### AD-006: Spec-driven completo antes da POC (2026-06-15)

**Decision:** 30 features especificadas em `.specs/features/` — Sprint 1 com spec+design+tasks (54 tarefas); Sprints 2-7 com spec.md.
**Reason:** Fechar fase Specify/Design/Tasks do tlc-spec-driven antes de iniciar desenvolvimento.
**Trade-off:** Design/tasks de Sprints 2-7 serão detalhados incrementalmente antes de cada sprint.
**Impact:** Execute em andamento; `create-customer` e `create-account` concluídas (gates full verdes).

---

## Active Blockers

_(Nenhum blocker ativo.)_

---

## Lessons Learned

- Subagents paralelos aceleram criação de ADRs e specs em massa sem perder coerência com PROJECT.md e AGENTS.md.
- Sprint 1 `transfer-money` é a feature crítica — depende de `LedgerPort` (stub S1, implementação real Sprint 2).
- Fechar feature exige atualizar **todos** os artefatos do Feature Close Checklist (`.rules/workflow.md`) — não apenas `tasks.md` e `STATE.md`; `INDEX.md` é crítico para agentes seguintes.

---

## Quick Tasks Completed

| #   | Description                              | Date       | Commit | Status  |
| --- | ---------------------------------------- | ---------- | ------ | ------- |
| —   | Spec inicial e mapeamento brownfield     | 2026-06-15 | —      | ✅ Done |
| —   | Scaffold monorepo (backend, frontend, infra) | 2026-06-15 | —  | ✅ Done |
| —   | ADRs iniciais (7 decisões)               | 2026-06-15 | —      | ✅ Done |
| —   | Features Sprint 1 (8 specs, 54 tasks)    | 2026-06-15 | —      | ✅ Done |
| —   | Features Sprints 2-7 (22 specs)          | 2026-06-15 | —      | ✅ Done |
| —   | Execute `create-customer` (T1–T11)     | 2026-06-15 | —      | ✅ Done |
| —   | Execute `create-account` (T1–T10)      | 2026-06-15 | —      | ✅ Done |
| —   | Execute `query-customers` (T1–T7)    | 2026-06-15 | —      | ✅ Done |

---

## Deferred Ideas

- [ ] Projeção de saldo materializada para performance em consultas frequentes — Captured during: Sprint 2 (Ledger)
- [ ] Saga pattern para operações distribuídas futuras — Captured during: arquitetura inicial
- [ ] Feature flags para integrações externas em sandbox — Captured during: Sprint 3 (Cobranças)
- [ ] Design/tasks detalhados para Sprints 2-7 — Captured during: fechamento spec-driven (antes de cada sprint)

---

## Todos

- [x] Criar scaffold do monorepo (`backend/`, `frontend/`, `infra/`, `docs/`, `adr/`, `scripts/`)
- [x] Escrever ADRs iniciais (arquitetura, banco, mensageria, observabilidade, segurança, ledger, MP)
- [x] Especificar features Sprint 1 (spec + design + tasks)
- [x] Especificar features Sprints 2-7 (spec.md)
- [ ] Configurar Docker Compose com PostgreSQL, Kafka e Kafka UI
- [x] Executar Sprint 1 — `create-customer` (T1–T11)
- [x] Executar Sprint 1 — `create-account` (T1–T10)
- [x] Executar Sprint 1 — `query-customers` (T1–T7)
- [ ] Executar Sprint 1 — `transfer-money`

---

## Spec-Driven Checklist (POC)

| Fase | Status | Artefatos |
| ---- | ------ | --------- |
| Brownfield mapping | ✅ | `.specs/codebase/*` (7 docs) |
| Project init | ✅ | PROJECT.md, ROADMAP.md, STATE.md |
| ADRs | ✅ | `adr/0001`–`0007` |
| Specify Sprint 1 | ✅ | 8 features, 54 tasks |
| Specify Sprints 2-7 | ✅ | 22 features |
| Design Sprint 1 | ✅ | 7 design.md |
| Tasks Sprint 1 | ✅ | 7 tasks.md |
| **Execute (POC)** | ⏳ | `create-customer` ✅; `create-account` ✅; `query-customers` ✅; próxima: `transfer-money` |

---

## Preferences

**Model Guidance Shown:** never
