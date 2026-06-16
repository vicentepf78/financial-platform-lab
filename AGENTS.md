# AGENTS.md — Financial Platform Lab

Mandatory rules for AI agents. **Load extended rules on demand** from `.rules/` — do not load all rule files every turn.

---

## Purpose

Portfolio-grade financial platform: Core Banking, Ledger, PIX, Billing, Reconciliation, Event-Driven Architecture, Observability, Cloud Native.

---

## Technology Stack

| Area | Stack |
| ---- | ----- |
| Backend | Java 21, Spring Boot 3, Spring Data JPA, Spring Security, PostgreSQL, Kafka |
| Frontend | React, TypeScript, Material UI, React Query |
| Infra | Docker, Docker Compose, Kubernetes |
| Testing | JUnit 5, Mockito, AssertJ, Testcontainers, Playwright |

---

## Golden Rules (summary)

| # | Rule |
| - | ---- |
| 1 | Business rules only in **Domain** and **Application** — never in controllers, consumers, repositories |
| 2 | Every feature must have **tests** (co-located per task — see `.rules/testing.md`) |
| 3 | **Never** `account.setBalance(...)` — balance from ledger entries only |
| 4 | Domain must not depend on Spring, JPA, Kafka, HTTP |
| 5 | Composition over inheritance |
| 6 | ~200 lines/class, ~30 lines/method |
| 7 | Favor immutability |

Full architecture: `.rules/architecture.md`  
Financial rules: `.rules/financial.md`  
Testing (co-location + gates): `.rules/testing.md`  
REST envelope + Problem Details: `.rules/rest.md`  
Database / Flyway: `.rules/database.md`  
Kafka / events: `.rules/kafka-events.md`  
Workflow + context loading: `.rules/workflow.md`

---

## Spec-Driven Development

Follow `tlc-spec-driven`: Specify → (Design) → (Tasks) → Execute.

- Planning docs: `.specs/features/{feature}/spec.md`, `design.md`, `tasks.md`
- Brownfield index: `.specs/codebase/INDEX.md` — **consult before exploring repo**
- Testing matrix: `.specs/codebase/TESTING.md`
- Project memory: `.specs/project/STATE.md`

**Execute session:** load `tasks.md` + relevant `.rules/` only — not full spec + design together.

**Feature close:** on the **final task** or a dedicated **"Documentação + feature close"** task (template: jwt-auth T13), load `.rules/workflow.md` **Feature Close Checklist** and complete every applicable item. A feature is **not** done until the checklist is complete — updating only `tasks.md` is insufficient. Update spec, design, tasks, STATE, ROADMAP, and brownfield docs (`INDEX.md`, etc.).

**Sub-agents:** delegate implementation per task; orchestrator coordinates. See `.rules/workflow.md`.

---

## Code Quality

SOLID, DRY, KISS, YAGNI. Explicit names (`TransferMoneyUseCase`, not `TransferManager`).

---

## Success Criteria

- Business requirements implemented
- Tests pass (gate check green)
- Architecture and financial rules preserved
- Documentation updated

---

End of core context. See `.rules/` for details.
