# Testing Rules

Aligned with `tlc-spec-driven`: tests are **co-located in the same task** that creates the code layer. The **gate check** (Maven exit code) is the verdict — not a mandatory red/green TDD ritual.

## Rule 2

Every feature must have tests. No feature is complete without tests.

## Co-location (per task)

| Task creates... | Tests in same task | Gate |
| --------------- | ------------------ | ---- |
| Domain / use case | Unit | quick (`mvn test -pl {module}`) |
| Controller / repository / Kafka | Integration | full (`mvn verify -Pintegration -pl {module}`) |
| Config / migration only | As covered by later tasks | build |

See `.specs/codebase/TESTING.md` for Coverage Matrix and Gate Check Commands.

## Stack

JUnit 5, Mockito, AssertJ, Testcontainers, Playwright (E2E).

## Naming

```java
shouldTransferMoneyWhenOriginAccountHasEnoughBalance()
```

Avoid `testTransfer()`.

## Integration containers

PostgreSQL and Kafka are mandatory for integration tests (Testcontainers).

## Reference

Implemented patterns: `.specs/codebase/INDEX.md` → create-customer tests.

## Execute workflow (per task)

1. Implement code + tests in the same task (order flexible).
2. Run gate command from `tasks.md`.
3. Non-zero exit = stop and fix.
4. Atomic commit per task.

Never bypass tests. Never delete or weaken tests to pass the gate.
