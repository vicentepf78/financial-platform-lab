# Financial Domain Rules

## Ledger First

The ledger is the source of truth. Account balance is a projection. Never store balance as authoritative data.

## Rule 3 — Never update balance directly

Forbidden:

```java
account.setBalance(...)
```

Every financial operation must generate ledger entries. Balance is derived from ledger entries.

## Double Entry Accounting

Every financial transaction generates:

- Debit entry
- Credit entry

Example transfer: debit Account A, credit Account B.

## Auditability

Every financial action must be traceable. Store: user, timestamp, correlation ID, operation.

## Idempotency

External operations (PIX, webhooks, billing) must be idempotent. Repeated requests must not duplicate financial effects.
