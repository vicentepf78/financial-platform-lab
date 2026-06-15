# Close Account — Tasks

**Design:** `.specs/features/close-account/design.md`
**Status:** Draft

---

## Execution Plan

```text
T1 → T2 → T3 → T4 → T5
```

---

## Task Breakdown

### T1: Account.close() domínio + testes

**What:** Método close com guards ACTIVE/CLOSED
**Where:** `backend/account-module/domain/Account.java`
**Depends on:** create-account T2
**Requirement:** ACCT-08, ACCT-09, ACCT-10

**Done when**:

- [ ] close() transiciona CLOSED
- [ ] already closed lança exceção
- [ ] Gate: `mvn test -pl account-module`
- [ ] Test count: ≥4 passam

**Tests:** unit | **Gate:** quick

---

### T2: LedgerPort.getBalanceProjection (se ausente)

**What:** Método projeção saldo no port + stub
**Where:** `backend/account-module/ports/LedgerPort.java`
**Depends on:** T1
**Requirement:** ACCT-11

**Done when**:

- [ ] Retorna Money
- [ ] Gate: `mvn test -pl account-module`

**Tests:** unit | **Gate:** quick

---

### T3: CloseAccountUseCase + testes

**What:** Orquestração com validação saldo
**Where:** `backend/account-module/features/close-account/`
**Depends on:** T1, T2
**Requirement:** ACCT-08, ACCT-11

**Done when**:

- [ ] Saldo > 0 rejeita
- [ ] Gate: `mvn test -pl account-module`
- [ ] Test count: ≥5 passam

**Tests:** unit | **Gate:** quick

---

### T4: CloseAccountController + testes integração

**What:** POST /accounts/{id}/close
**Where:** `backend/account-module/features/close-account/`
**Depends on:** T3
**Requirement:** ACCT-08 a ACCT-11

**Done when**:

- [ ] 200, 404, 409, 422
- [ ] Gate: `mvn verify -Pintegration -pl account-module`
- [ ] Test count: ≥5 passam

**Tests:** integration | **Gate:** full

---

### T5: Wiring + verify

**Depends on:** T4
**Requirement:** ACCT-08 a ACCT-12

**Tests:** none | **Gate:** full

**Total tasks: 5**

---

## Test Co-location Validation

| Task | Layer | Status |
| ---- | ----- | ------ |
| T1 | Domain unit | ✅ |
| T3 | Use case unit | ✅ |
| T4 | Controller integration | ✅ |
