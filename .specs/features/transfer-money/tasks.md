# Transfer Money — Tasks

**Design:** `.specs/features/transfer-money/design.md`
**Status:** Draft

---

## Execution Plan

### Phase 1: Domain & Ledger (Sequential)

```text
T1 → T2 → T3 → T4
```

### Phase 2: Application (Parallel OK)

```text
        ┌→ T5 [P] ─┐
T4 ─────┼→ T6 [P] ─┼──→ T8
        └→ T7 [P] ─┘
              T9 ──────→ T11
              T10 ─────→ T11
```

### Phase 3: Integration & E2E readiness

```text
T11 → T12 → T13
```

---

## Task Breakdown

### T1: Migration V4/V5 transfers + ledger_entries_stub

**What:** Tabelas transfers e entries stub débito/crédito
**Where:** `backend/application/src/main/resources/db/migration/`
**Depends on:** V3 accounts
**Requirement:** XFER-01, XFER-02

**Done when**:

- [ ] FK para accounts
- [ ] UNIQUE idempotency_key nullable
- [ ] ledger_entries_stub com entry_type DEBIT/CREDIT

**Tests:** none | **Gate:** build

---

### T2: Transfer entity + TransferDomainService + testes domínio

**What:** Entidade transfer e validações cross-account
**Where:** `backend/account-module/domain/`
**Depends on:** T1
**Reuses:** Money, Identifier
**Requirement:** XFER-04, XFER-05, XFER-10

**Done when**:

- [ ] Rejeita same account, amount <= 0
- [ ] Rejeita inactive account
- [ ] Gate: `mvn test -pl account-module`
- [ ] Test count: ≥6 passam

**Tests:** unit | **Gate:** quick

---

### T3: TransferExecuted event + Transfer status enum

**What:** Domain event record implementando DomainEvent
**Where:** `backend/account-module/domain/events/`
**Depends on:** T2
**Requirement:** XFER-03, XFER-11

**Tests:** unit (com T2) | **Gate:** quick

---

### T4: LedgerPort.recordTransfer + stub implementation

**What:** Partidas dobradas no stub (debit origin, credit dest)
**Where:** `backend/ledger-module/adapters/LedgerStubAdapter.java`
**Depends on:** T1, get-account-balance T1
**Requirement:** XFER-02, XFER-06, XFER-07

**Done when**:

- [ ] recordTransfer atômico
- [ ] Insufficient balance lança exceção
- [ ] getBalanceProjection reflete transfer
- [ ] Gate: `mvn test -pl ledger-module,account-module`
- [ ] Test count: ≥5 passam

**Tests:** unit | **Gate:** quick

---

### T5: TransferRepositoryPort + JPA adapter [P]

**What:** Persist transfer metadata
**Where:** `backend/account-module/adapters/persistence/`
**Depends on:** T2
**Requirement:** XFER-01, XFER-09

**Done when**:

- [ ] save, findByIdempotencyKey
- [ ] Gate: `mvn verify -Pintegration -pl account-module`
- [ ] Test count: ≥3 passam

**Tests:** integration | **Gate:** full

---

### T6: TransferMoneyRequest/Response DTOs [P]

**What:** Records API com validation
**Where:** `backend/account-module/features/transfer-money/`
**Depends on:** T2
**Requirement:** XFER-01, XFER-10

**Tests:** none | **Gate:** build

---

### T7: Credit account helper for tests (LedgerPort seed) [P]

**What:** Método test-only ou admin stub `creditAccount(accountId, amount)` no LedgerStub para setup de testes
**Where:** `backend/ledger-module/adapters/`
**Depends on:** T4
**Requirement:** Suporte testes XFER-01

**Done when**:

- [ ] Permite seed saldo em integration tests
- [ ] Documentado como test/dev only

**Tests:** integration | **Gate:** full

---

### T8: TransferMoneyUseCase + testes unitários

**What:** Orquestração completa com mocks
**Where:** `backend/account-module/features/transfer-money/TransferMoneyUseCase.java`
**Depends on:** T2, T3, T4, T5
**Requirement:** XFER-01 a XFER-11

**Done when**:

- [ ] Happy path
- [ ] Insufficient balance, closed account, not found, same account
- [ ] Idempotency retorna existente
- [ ] Gate: `mvn test -pl account-module`
- [ ] Test count: ≥10 passam

**Tests:** unit | **Gate:** quick

---

### T9: Kafka TransferExecuted publisher [P]

**What:** Estender EventPublisherPort ou adapter específico
**Where:** `backend/account-module/adapters/messaging/`
**Depends on:** T3, create-account T6
**Requirement:** XFER-03

**Done when**:

- [ ] Topic transfer-executed
- [ ] Gate: `mvn verify -Pintegration -pl account-module`
- [ ] Test count: ≥2 passam

**Tests:** integration | **Gate:** full

---

### T10: TransferMoneyController + testes integração

**What:** POST /api/v1/transfers
**Where:** `backend/account-module/features/transfer-money/TransferMoneyController.java`
**Depends on:** T6, T8
**Requirement:** XFER-01, XFER-06, XFER-08

**Done when**:

- [ ] 201, 400, 404, 409, 422
- [ ] Gate: `mvn verify -Pintegration -pl account-module`
- [ ] Test count: ≥8 passam

**Tests:** integration | **Gate:** full

---

### T11: Wiring AccountModuleConfig + transação

**What:** Beans, @Transactional boundary
**Depends on:** T8, T9, T10
**Requirement:** XFER-02

**Done when**:

- [ ] Endpoint registrado
- [ ] Rollback em falha ledger verificado

**Tests:** none (covered T10) | **Gate:** full

---

### T12: Fluxo integrado Sprint 1 verify

**What:** Script/documentação fluxo completo + migration integration test
**Depends on:** T11, T7
**Requirement:** XFER-01 a XFER-11

**Done when**:

- [ ] create customer → 2 accounts → credit → transfer → balances
- [ ] Gate: `mvn verify -Pintegration`
- [ ] Test count: ≥1 fluxo integrado passa

**Tests:** integration | **Gate:** full

**Verify:**
```bash
# 1. Create customer
# 2. Create two accounts
# 3. Credit origin (test helper)
# 4. POST /api/v1/transfers
# 5. GET balance origem e destino
```

---

### T13: Documentação + feature close

**What:** Run Feature Close Checklist (`.rules/workflow.md`)
**Where:** `.specs/`, `.specs/codebase/INDEX.md`, `.specs/codebase/ARCHITECTURE.md`
**Depends on:** T12
**Requirement:** XFER-01 a XFER-11

**Done when**:

- [ ] Feature Close Checklist (`.rules/workflow.md`) completo
- [ ] spec.md `Status: Done`; design.md `Status: Implemented`; tasks.md `Status: Done`
- [ ] INDEX.md, STATE.md, ROADMAP.md, ARCHITECTURE.md atualizados (transfer slice, evento `TransferExecuted`)
- [ ] Gate: `mvn verify -Pintegration`

**Tests:** none | **Gate:** full

---

## Parallel Execution Map

```text
Phase 1: T1 → T2 → T3 → T4
Phase 2: T5 [P], T6 [P], T7 [P], T9 [P] após T4; T8 após T5; T10 após T8
Phase 3: T11 → T12 → T13
```

---

## Task Granularity Check

| Task | Scope | Status |
| ---- | ----- | ------ |
| T2 | Domain entity + service | ✅ Granular (coeso) |
| T4 | 1 port method + stub | ✅ Granular |
| T8 | 1 use case | ✅ Granular |
| T10 | 1 endpoint | ✅ Granular |

---

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram | Status |
| ---- | ---------- | ------- | ------ |
| T2 | T1 | T1→T2 | ✅ |
| T4 | T1 | T1→T4 | ✅ |
| T8 | T2,T3,T4,T5 | T4→T8, T5→T8 | ✅ |
| T10 | T6,T8 | T8→T10 | ✅ |
| T11 | T8,T9,T10 | T10→T11 | ✅ |
| T12 | T11,T7 | T11→T12 | ✅ |
| T13 | T12 | T12→T13 | ✅ |

---

## Test Co-location Validation

| Task | Code Layer | Matrix Requires | Task Says | Status |
| ---- | ---------- | --------------- | --------- | ------ |
| T2 | Domain | unit | unit | ✅ OK |
| T4 | LedgerPort/stub | unit | unit | ✅ OK |
| T5 | Repository | integration | integration | ✅ OK |
| T8 | Use case | unit | unit | ✅ OK |
| T9 | Kafka producer | integration | integration | ✅ OK |
| T10 | Controller | integration | integration | ✅ OK |
| T12 | Full flow | integration | integration | ✅ OK |

**Total tasks: 13**

---

## Test Co-location (por task)

Alinhado ao `tlc-spec-driven` e `.rules/testing.md`:

1. Código + testes na mesma task (tipo conforme coluna Tests).
2. Gate check conforme coluna Gate.
3. Commit atômico: `feat(account): transfer-money T{N} - {description}`
