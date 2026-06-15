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
9. Update documentation

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
