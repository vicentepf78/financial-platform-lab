# Create Customer — Tasks

**Design:** `.specs/features/create-customer/design.md`
**Reference:** `.specs/codebase/INDEX.md` (esta feature é o reference slice)
**Status:** Done

**Total tasks:** 11

---

## Execution Plan

### Phase 1: Foundation (Parallel then Sequential)

```text
T1 [P]  (migration)
T2 [P]  (domain VOs — não depende de T1)
T2 → T3 → T4
```

### Phase 2: Core Implementation (Parallel after T4)

```text
        ┌→ T5 [P] ─┐
T4 ─────┼→ T6 [P] ─┼──→ T9
        └→ T7 [P] ─┘
              T8 ──────→ T9
```

### Phase 3: Integration (Sequential)

```text
T1 → T10          (migration IT — não depende de T9)
T9 → T11          (smoke E2E manual)
```

---

## Task Breakdown

### T1: Migration Flyway tabela customers

**What:** Criar `V2__customers.sql` com tabela, índice UNIQUE em `document`, colunas de auditoria
**Where:** `backend/application/src/main/resources/db/migration/V2__customers.sql`
**Depends on:** None
**Reuses:** Padrão `V1__init.sql`, extensão `pgcrypto`
**Requirement:** CUST-01, CUST-04, CUST-05

**Done when**:

- [x] Tabela `customers` com PK UUID, campos name, type, document, email, audit
- [x] Constraint UNIQUE em `document`
- [x] Migration idempotente e versionada

**Tests:** none (schema validado em T10)
**Gate:** build

---

### T2: Enum CustomerType e value objects de domínio

**What:** `CustomerType`, `Email`, sealed `Document` (CpfDocument/CnpjDocument)
**Where:** `backend/customer-module/domain/`
**Depends on:** None
**Reuses:** `Cpf`, `Cnpj` — INDEX.md → shared-kernel
**Requirement:** CUST-02, CUST-08

**Done when**:

- [x] Factory valida consistência type/document
- [x] CPF inválido lança exceção de domínio
- [x] Sem dependência Spring/JPA
- [x] Gate check passes: `mvn test -pl customer-module`
- [x] Test count: ≥4 testes unitários passam

**Tests:** unit
**Gate:** quick

---

### T3: Agregado Customer

**What:** Classe `Customer extends AggregateRoot` com factory `create(...)`
**Where:** `backend/customer-module/domain/Customer.java`
**Depends on:** T2
**Reuses:** `Identifier`, `AggregateRoot`, `AuditableEntity` — INDEX.md → shared-kernel
**Requirement:** CUST-01, CUST-04, CUST-07

**Done when**:

- [x] Factory gera Identifier e timestamps
- [x] Sem dependência Spring/JPA
- [x] Gate check passes: `mvn test -pl customer-module`
- [x] Test count: ≥3 testes passam

**Tests:** unit
**Gate:** quick

---

### T4: CustomerRepositoryPort e DuplicateDocumentException

**What:** Interface outbound + exceção de domínio para duplicata
**Where:** `backend/customer-module/ports/`, `backend/customer-module/domain/`
**Depends on:** T3
**Requirement:** CUST-05, CUST-06

**Done when**:

- [x] Port define `existsByDocument`, `save`, `findById`
- [x] Exceção específica para 409 mapping

**Tests:** none
**Gate:** build

---

### T5: CreateCustomerUseCase + testes unitários [P]

**What:** Use case com verificação de unicidade e persistência
**Where:** `backend/customer-module/features/createcustomer/CreateCustomerUseCase.java`
**Depends on:** T4
**Reuses:** Mockito para port
**Requirement:** CUST-01, CUST-05, CUST-06

**Done when**:

- [x] Cadastro válido retorna result
- [x] Duplicata lança `DuplicateDocumentException`
- [x] Gate check passes: `mvn test -pl customer-module`
- [x] Test count: ≥5 testes passam

**Tests:** unit
**Gate:** quick

---

### T6: CustomerEntity e JpaCustomerRepository [P]

**What:** Entidade JPA + adapter implementando port
**Where:** `backend/customer-module/adapters/persistence/`
**Depends on:** T4
**Reuses:** Test migrations em `customer-module/src/test/resources/db/migration/`
**Requirement:** CUST-01, CUST-05

**Done when**:

- [x] Mapper domain ↔ entity sem regras de negócio
- [x] `existsByDocument` consulta índice UNIQUE
- [x] Gate check passes: `mvn verify -Pintegration -pl customer-module`
- [x] Test count: ≥2 testes integração repositório passam

**Tests:** integration
**Gate:** full

---

### T7: DTOs CreateCustomerRequest/Response [P]

**What:** Records imutáveis para API e command interno
**Where:** `backend/customer-module/features/createcustomer/`
**Depends on:** T4
**Requirement:** CUST-03

**Done when**:

- [x] Bean Validation em Request (not blank, email)
- [x] Response mapeia envelope data/metadata

**Tests:** none
**Gate:** build

---

### T8: CreateCustomerController + teste integração

**What:** REST adapter `POST /api/v1/customers` com Problem Details
**Where:** `backend/customer-module/features/createcustomer/CreateCustomerController.java`
**Depends on:** T5, T7
**Reuses:** `AbstractCustomerIntegrationTest` — INDEX.md
**Requirement:** CUST-01, CUST-03, CUST-06, CUST-08

**Done when**:

- [x] 201 em cadastro válido
- [x] 400 documento inválido / tipo inconsistente
- [x] 409 documento duplicado
- [x] Gate check passes: `mvn verify -Pintegration -pl customer-module`
- [x] Test count: ≥4 testes controller passam

**Tests:** integration
**Gate:** full

---

### T9: CustomerModuleConfig (beans Spring)

**What:** Wiring use case, repository, controller
**Where:** `backend/customer-module/infrastructure/CustomerModuleConfig.java`
**Depends on:** T5, T6, T8
**Reuses:** `CustomerPersistenceConfig` para beans JPA condicionais
**Requirement:** CUST-01

**Done when**:

- [x] Application context carrega beans do módulo
- [x] Endpoint registrado em `/api/v1/customers`

**Tests:** none (coberto por T8)
**Gate:** build

---

### T10: Teste integração migration customers

**What:** Validar Flyway aplica V2 e schema correto
**Where:** `backend/application/src/test/java/.../CustomersMigrationIntegrationTest.java`
**Depends on:** T1
**Requirement:** CUST-04, CUST-05

**Done when**:

- [x] Testcontainers PostgreSQL sobe migration
- [x] UNIQUE constraint verificada
- [x] Gate check passes: `mvn verify -Pintegration`
- [x] Test count: ≥1 teste passa

**Tests:** integration
**Gate:** full

---

### T11: Verificação end-to-end manual da feature

**What:** Smoke test curl/documentado no Verify
**Depends on:** T9, T10
**Requirement:** CUST-01 a CUST-08

**Done when**:

- [x] Fluxo POST válido + POST duplicado documentado
- [x] Todos requisitos CUST-* rastreados

**Tests:** none
**Gate:** full

**Verified:** 2026-06-15 — `mvn verify -Pintegration -pl customer-module,application` passou; fluxos 201/400/409 cobertos por `CreateCustomerControllerIntegrationTest`.

**Verify:**

```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"Maria","type":"INDIVIDUAL","document":"529.982.247-25","email":"m@ex.com"}'
```

Esperado: HTTP 201 com `data.id` UUID; segundo POST com mesmo documento → HTTP 409.

---

> **Feature close:** This feature predates the mandatory doc-close task. Before marking Done, run the **Feature Close Checklist** in `AGENTS.md` (update `spec.md`, `design.md`, brownfield docs). Template for future features: jwt-auth T13.
