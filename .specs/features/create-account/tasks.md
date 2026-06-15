# Create Account — Tasks

**Design:** `.specs/features/create-account/design.md`
**Reference:** `.specs/codebase/INDEX.md` (create-customer slice)
**Status:** Draft

**Total tasks:** 10

---

## Execution Plan

### Phase 1: Foundation (Parallel)

```text
T1 [P]  (migration)
T2 [P]  (domain — não depende de T1)
```

### Phase 2: Ports & Adapters (Parallel after T2)

```text
T2 → T3 [P]
  → T4 [P]
```

### Phase 3: Cross-cutting adapters (Parallel after T4)

```text
T4 → T5 [P]
  → T6 [P]
  → T7 [P]
```

### Phase 4: Application & wiring (Sequential)

```text
T5, T6, T7, T3 → T8 → T9 → T10
```

---

## Task Breakdown

### T1: Migration V3 accounts

**What:** Tabela accounts com FK customer, sem balance
**Where:** `backend/application/src/main/resources/db/migration/V3__accounts.sql`
**Depends on:** V2 customers migration (já no repo — ver INDEX.md)
**Reuses:** `V2__customers.sql`, padrão `V1__init.sql`
**Requirement:** ACCT-01

**Done when**:

- [ ] Tabela `accounts` com PK UUID, FK `customer_id`, sem coluna balance
- [ ] Migration versionada e idempotente

**Tests:** none | **Gate:** build

---

### T2: Account aggregate + AccountStatus + AccountCreated event

**What:** Domínio com factory open() e evento
**Where:** `backend/account-module/domain/`
**Depends on:** None
**Reuses:** AggregateRoot, DomainEvent, Identifier (INDEX.md → shared-kernel)
**Requirement:** ACCT-01, ACCT-04, ACCT-05

**Done when**:

- [ ] Account.open registra AccountCreated
- [ ] Sem dependência Spring/JPA
- [ ] Gate: `mvn test -pl account-module`
- [ ] Test count: ≥4 passam

**Tests:** unit | **Gate:** quick

---

### T3: AccountRepositoryPort + JPA adapter

**What:** Persistência conta
**Where:** `backend/account-module/adapters/persistence/`
**Depends on:** T2
**Reuses:** JPA pattern em INDEX.md → customer-module/adapters/persistence
**Requirement:** ACCT-01

**Done when**:

- [ ] save/findById
- [ ] Gate: `mvn verify -Pintegration -pl account-module`
- [ ] Test count: ≥2 passam

**Tests:** integration | **Gate:** full

---

### T4: LedgerPort interface + LedgerStubAdapter

**What:** Port outbound e stub S1 initializeAccount
**Where:** `backend/account-module/ports/`, stub em account-module ou ledger-module adapters
**Depends on:** T2
**Requirement:** ACCT-06, ACCT-07

**Done when**:

- [ ] initializeAccount idempotente
- [ ] getBalanceProjection retorna zero
- [ ] Gate: `mvn test -pl account-module`
- [ ] Test count: ≥2 passam

**Tests:** unit | **Gate:** quick

---

### T5: CustomerLookupPort + adapter in-process [P]

**What:** Verifica existência de cliente
**Where:** `backend/account-module/adapters/customer/`
**Depends on:** T4
**Reuses:** `customer-module` implementado (INDEX.md) — não explorar query-customers
**Requirement:** ACCT-02

**Done when**:

- [ ] exists() delega ao customer-module
- [ ] Gate: `mvn test -pl account-module`
- [ ] Test count: ≥2 passam

**Tests:** unit | **Gate:** quick

---

### T6: EventPublisherPort + Kafka adapter [P]

**What:** Publicação account-created
**Where:** `backend/account-module/adapters/messaging/`
**Depends on:** T4
**Requirement:** ACCT-04, ACCT-05

**Done when**:

- [ ] JSON serialization
- [ ] Gate: `mvn verify -Pintegration -pl account-module`
- [ ] Test count: ≥2 passam

**Tests:** integration | **Gate:** full

---

### T7: CreateAccountRequest/Response DTOs [P]

**What:** Records API
**Where:** `backend/account-module/features/create-account/`
**Depends on:** T4
**Reuses:** DTO pattern em INDEX.md → CreateCustomerRequest/Response
**Requirement:** ACCT-03

**Tests:** none | **Gate:** build

---

### T8: CreateAccountUseCase + testes unitários

**What:** Orquestração completa
**Where:** `backend/account-module/features/create-account/CreateAccountUseCase.java`
**Depends on:** T3, T4, T5, T6 (mocks nos testes)
**Reuses:** CreateCustomerUseCase (INDEX.md)
**Requirement:** ACCT-01 a ACCT-07

**Done when**:

- [ ] Happy path + customer not found + ledger failure
- [ ] Gate: `mvn test -pl account-module`
- [ ] Test count: ≥6 passam

**Tests:** unit | **Gate:** quick

---

### T9: CreateAccountController + testes integração

**What:** POST /api/v1/accounts
**Where:** `backend/account-module/features/create-account/`
**Depends on:** T7, T8
**Reuses:** CreateCustomerController + AbstractCustomerIntegrationTest (INDEX.md)
**Requirement:** ACCT-01, ACCT-02, ACCT-03

**Done when**:

- [ ] 201, 404, 400
- [ ] Gate: `mvn verify -Pintegration -pl account-module`
- [ ] Test count: ≥4 passam

**Tests:** integration | **Gate:** full

---

### T10: AccountModuleConfig + wiring + verify

**What:** Beans Spring + smoke
**Where:** `backend/account-module/infrastructure/`
**Depends on:** T9
**Reuses:** CustomerModuleConfig (INDEX.md)
**Requirement:** ACCT-01 a ACCT-07

**Done when**:

- [ ] Application context carrega beans do módulo
- [ ] Endpoint registrado em `/api/v1/accounts`
- [ ] Gate: `mvn verify -Pintegration`

**Tests:** none (coberto por T9) | **Gate:** full
