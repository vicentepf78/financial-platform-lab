# Development Workflow

## Philosophy

Spec-Driven Development, Clean Code, Refactoring, Evolutionary Architecture.

Follow `tlc-spec-driven` for feature lifecycle: Specify → (Design) → (Tasks) → Execute.

## Before implementing a feature

1. Read `.specs/project/PROJECT.md`
2. Read feature `spec.md` (Specify session)
3. Read `design.md` if exists (Design session)
4. Read `tasks.md` only in Execute session — not together with full spec+design
5. Consult `.specs/codebase/INDEX.md` for reference slices — do not re-explore
6. Read relevant ADRs
7. Implement per task (code + tests co-located)
8. Run gate check
9. Update documentation — **mandatory Feature Close Checklist** (below)

## Feature Close Checklist (mandatory)

When the last task of a feature is complete, update **all** applicable artifacts before considering the feature done:

| # | Artifact | What to update |
| - | -------- | -------------- |
| 1 | `features/{feature}/spec.md` | `Status: Done`; Goals `[x]`; Requirements → `Done`; Success Criteria `[x]` |
| 2 | `features/{feature}/design.md` | `Status: Implemented` |
| 3 | `features/{feature}/tasks.md` | `Status: Done`; all `Done when` checkboxes `[x]` |
| 4 | `.specs/project/STATE.md` | Current Work, Todos, Quick Tasks, Execute checklist |
| 5 | `.specs/project/ROADMAP.md` | Feature marked ✅ DONE in current sprint |
| 6 | `.specs/codebase/INDEX.md` | Module status, reference slice paths, tests, migrations, API |
| 7 | `.specs/codebase/ARCHITECTURE.md` | Status, examples, domain events table (if applicable) |
| 8 | `.specs/codebase/CONCERNS.md` | Remove resolved blockers; update test coverage notes |
| 9 | `.specs/codebase/TESTING.md` | E2E flows table, coverage status (if applicable) |
| 10 | `.specs/codebase/STRUCTURE.md` | Module status, directory tree (if new module or major layout change) |

**Rule:** Updating only `tasks.md` and `STATE.md` is insufficient. The feature is not closed until INDEX.md reflects the implementation — future agents depend on it to avoid re-exploration.

**Execute rule:** Every feature `tasks.md` MUST include a final task **"Documentação + feature close"** (see jwt-auth T13).

**Execute session:** When running the final task, agents MUST load this checklist — do not skip `spec.md` / `design.md` updates.

## Context loading (agents)

**Always:** `AGENTS.md` + task-specific `.rules/` files as needed.

**On demand:**

| Working on... | Also load |
| ------------- | --------- |
| Any implementation | `.rules/architecture.md` |
| Financial features | `.rules/financial.md` |
| Tasks with tests | `.rules/testing.md`, `.specs/codebase/TESTING.md` |
| Controllers / API | `.rules/rest.md` |
| Migrations | `.rules/database.md` |
| Kafka / events | `.rules/kafka-events.md` |

**Never load simultaneously:** multiple feature specs, or spec + design + tasks during Execute.

## Documentation

Every feature: description, acceptance criteria, business rules. Every architecture decision: ADR.

## AI output expectations

Code must compile, be tested, follow architecture and financial rules. No placeholders. No TODO instead of implementation.
