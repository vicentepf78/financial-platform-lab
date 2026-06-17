# Financial Platform Lab Overlay

Use these sources as the project rules of truth when they are present in the repository context:

- `AGENTS.md`
- `.rules/architecture.md`
- `.rules/financial.md`
- `.rules/testing.md`
- `.rules/rest.md`
- `.rules/database.md`
- `.rules/kafka-events.md`
- `.specs/codebase/INDEX.md`
- `.specs/codebase/TESTING.md`
- `.specs/codebase/CONVENTIONS.md`

Key invariants:

- Business rules live only in Domain and Application.
- Domain must not depend on Spring, JPA, Kafka, or HTTP.
- Every feature must include tests in the same task/slice.
- Never set account balance directly; balances come from ledger entries.
- REST APIs use the project envelope and Problem Details for errors.
- Database changes use Flyway migrations.
- Kafka/events must preserve explicit contracts and idempotency.
- Prefer small classes, small methods, explicit names, immutability, and composition.
