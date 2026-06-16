# Get Account Balance — Tasks

**Design:** `.specs/features/get-account-balance/design.md`
**Status:** Draft

---

## Execution Plan

```text
T1 → T2 → T3 → T4 → T5
```

---

## Task Breakdown

### T1: Estender LedgerPort + stub getBalanceProjection

**What:** Implementação stub persistida ou in-memory
**Where:** `backend/ledger-module/` ou account adapters
**Depends on:** create-account T4
**Requirement:** ACCT-14, ACCT-16

**Done when**:

- [ ] initializeAccount cria projeção zero
- [ ] getBalanceProjection retorna Money
- [ ] Gate: `mvn test -pl account-module,ledger-module`
- [ ] Test count: ≥3 passam

**Tests:** unit | **Gate:** quick

---

### T2: GetAccountBalanceUseCase + testes

**What:** Orquestração exists + projection
**Where:** `backend/account-module/features/get-account-balance/`
**Depends on:** T1
**Requirement:** ACCT-13, ACCT-15, ACCT-17

**Done when**:

- [ ] Maps Money to amount string scale 2
- [ ] Gate: `mvn test -pl account-module`
- [ ] Test count: ≥4 passam

**Tests:** unit | **Gate:** quick

---

### T3: GetAccountBalanceController + testes integração

**What:** GET endpoint
**Where:** `backend/account-module/features/get-account-balance/`
**Depends on:** T2
**Requirement:** ACCT-13 a ACCT-17

**Done when**:

- [ ] 200 zero balance, 404
- [ ] Gate: `mvn verify -Pintegration -pl account-module`
- [ ] Test count: ≥3 passam

**Tests:** integration | **Gate:** full

---

### T4: Wiring + verify pós-transfer (forward dependency note)

**What:** Beans + documentar teste manual após transfer-money
**Depends on:** T3
**Requirement:** ACCT-13 a ACCT-17

**Tests:** none | **Gate:** full

---

### T5: Documentação + feature close

**What:** Run Feature Close Checklist (`.rules/workflow.md`)
**Where:** `.specs/`, `.specs/codebase/INDEX.md`
**Depends on:** T4
**Requirement:** ACCT-13 a ACCT-17

**Done when**:

- [ ] Feature Close Checklist (`.rules/workflow.md`) completo
- [ ] spec.md `Status: Done`; design.md `Status: Implemented`; tasks.md `Status: Done`
- [ ] INDEX.md, STATE.md, ROADMAP.md atualizados (get-account-balance slice, endpoint `GET /accounts/{id}/balance`)
- [ ] Gate: `mvn verify -Pintegration -pl account-module`

**Tests:** none | **Gate:** full

**Total tasks: 5**

---

## Test Co-location Validation

| Task | Layer | Status |
| ---- | ----- | ------ |
| T1 | Port/stub unit | ✅ |
| T2 | Use case unit | ✅ |
| T3 | Controller integration | ✅ |
