# Event-Driven Rules

Domain events are published after successful transactions.

Examples: `AccountCreated`, `TransferExecuted`, `ChargeCreated`, `PixReceived`.

## Topics

One topic per business capability: `account-created`, `transfer-executed`, `charge-created`, `pix-received`.

## Serialization

JSON only. Avoid Java serialization.

## Consumers

Must be idempotent and retryable.
