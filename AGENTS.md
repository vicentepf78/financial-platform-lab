# AGENTS.md — Financial Platform Lab

Mandatory, always-loaded rules for AI agents. Keep this file under 200 lines; use global skills only on demand for task-specific guidance.

---

## Purpose

Portfolio-grade financial platform for the Brazilian market: Core Banking, Ledger, PIX, Billing, Reconciliation, Event-Driven Architecture, Observability, and Cloud Native delivery.

---

## Technology Stack

| Area | Stack |
| ---- | ----- |
| Backend | Java 21, Spring Boot 3, Spring Data JPA, Spring Security, PostgreSQL, Kafka |
| Frontend | React, TypeScript, Material UI, React Query |
| Infra | Docker, Docker Compose, Kubernetes |
| Testing | JUnit 5, Mockito, AssertJ, Testcontainers, Playwright |

---

## Golden Rules

| # | Rule |
| - | ---- |
| 1 | Business rules only in **Domain** and **Application** — never in controllers, consumers, repositories, handlers, or adapters |
| 2 | Every feature must include tests in the same task/slice; no feature is complete without tests |
| 3 | **Never** `account.setBalance(...)`; balances come from ledger entries only |
| 4 | Domain must not depend on Spring, JPA, Kafka, HTTP, controllers, or repository implementations |
| 5 | Composition over inheritance |
| 6 | Small code: ~200 lines/class, ~30 lines/method |
| 7 | Favor immutability and explicit names (`TransferMoneyUseCase`, not `TransferManager`) |

---

## Architecture

Primary style: Modular Monolith + Hexagonal Architecture + Vertical Slice Architecture + DDD Light.

Allowed dependency direction:

```text
Controller -> UseCase
UseCase -> Domain
UseCase -> Ports
Adapters -> Ports
Infrastructure -> Adapters
```

Module structure:

```text
module/
  domain/
  application/
  ports/
  adapters/
  infrastructure/
  features/
```

Feature code lives in one vertical slice. Java package directories use lowercase names without hyphens (`features/createaccount/`); planning docs use kebab-case (`.specs/features/create-account/`).

---

## Financial Rules

- Ledger is the source of truth; account balance is a projection.
- Every financial transaction generates debit and credit entries.
- Every financial action must be traceable with user, timestamp, correlation ID, and operation.
- External operations (PIX, webhooks, billing) must be idempotent and must not duplicate financial effects.
- Use `Money`, identifiers, and domain value objects for critical financial concepts; never use floating point for money.

---

## API, Database, And Events

- REST controllers adapt HTTP to DTOs and delegate to use cases; no business rules in controllers.
- Successful REST responses use envelope `{ data, metadata }` when applicable.
- REST errors use Problem Details (RFC 9457 style).
- Database changes use explicit Flyway migrations; never use `ddl-auto=create` or `ddl-auto=create-drop` in production.
- PostgreSQL schemas must use UUID identifiers, explicit foreign keys, and indexes for frequent filters/joins/uniqueness.
- Domain/integration events are published only after successful state changes/transactions.
- Kafka topics are named by business capability (`account-created`, `transfer-executed`, etc.).
- Event serialization is JSON only; consumers must be idempotent and retryable.

---

## Testing

- Use `.specs/codebase/TESTING.md` for the coverage matrix and gate commands.
- Domain/use case tasks require unit tests (`mvn test -pl {module}`).
- Controller, repository, Kafka, and integration tasks require integration tests (`mvn verify -Pintegration -pl {module}`).
- PostgreSQL and Kafka integration tests use Testcontainers.
- Gate command exit code is the verdict: non-zero means stop and fix.
- Never bypass, delete, or weaken tests to pass a gate.

---

## Spec-Driven Development

Follow `tlc-spec-driven`: Specify → (Design) → (Tasks) → Execute.

- Planning docs: `.specs/features/{feature}/spec.md`, `design.md`, `tasks.md`
- Brownfield index: `.specs/codebase/INDEX.md` — consult before exploring patterns already indexed
- Testing matrix: `.specs/codebase/TESTING.md`
- Project memory: `.specs/project/STATE.md`
- ADRs: `adr/`

Execute session:

1. Read `.specs/project/PROJECT.md`.
2. Read the target feature `tasks.md`.
3. Read `design.md` only when the task depends on it.
4. Do not load multiple feature specs or full spec + design + tasks together unless explicitly needed.
5. Reuse `.specs/codebase/INDEX.md` reference slices before broad exploration.
6. Implement code and tests in the same task.
7. Run the gate command from `tasks.md`.

---

## Feature Close Checklist

On the final task, or on a dedicated **Documentação + feature close** task, update every applicable artifact before considering the feature done:

| # | Artifact | Required update |
| - | -------- | --------------- |
| 1 | `features/{feature}/spec.md` | `Status: Done`; goals `[x]`; requirements `Done`; success criteria `[x]` |
| 2 | `features/{feature}/design.md` | `Status: Implemented` |
| 3 | `features/{feature}/tasks.md` | `Status: Done`; all `Done when` checkboxes `[x]` |
| 4 | `.specs/project/STATE.md` | Current Work, Todos, Quick Tasks, Execute checklist |
| 5 | `.specs/project/ROADMAP.md` | Feature marked done in current sprint |
| 6 | `.specs/codebase/INDEX.md` | Module status, reference paths, tests, migrations, API |
| 7 | `.specs/codebase/ARCHITECTURE.md` | Status, examples, events table when applicable |
| 8 | `.specs/codebase/CONCERNS.md` | Remove resolved blockers; update coverage notes |
| 9 | `.specs/codebase/TESTING.md` | E2E flows table, coverage status when applicable |
| 10 | `.specs/codebase/STRUCTURE.md` | Module status/tree when layout changes |

Updating only `tasks.md` and `STATE.md` is insufficient. Future agents depend on `INDEX.md` to avoid re-exploration.

---

## Skills Usage

Use global skills on demand, not as permanent project memory:

- `tlc-spec-driven` for feature planning/execution workflow.
- `coding-guidelines` when writing or modifying code.
- `modular-architecture`, `modular-design-principles`, `domain-analysis`, `tactical-ddd` for architecture/DDD analysis.
- `create-e2e-tests` for E2E design/review.
- `security-*` for security review/threat modeling.
- `frontend-*`, `impeccable`, `web-design-guidelines` for UI work.
- `docs-writer` for documentation edits.

---

## Success Criteria

- Business requirements implemented.
- Tests pass with the required gate.
- Architecture and financial rules preserved.
- Documentation and long-term memory updated.
