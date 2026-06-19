# Query Customers — Tasks

**Design:** `.specs/features/query-customers/design.md`
**Status:** Done

---

## Execution Plan

### Phase 1: Foundation (Sequential)

```text
T1 → T2
```

### Phase 2: Core (Parallel OK)

```text
        ┌→ T3 [P] ─┐
T2 ─────┼→ T4 [P] ─┼──→ T6
        └→ T5 [P] ─┘
```

### Phase 3: Integration

```text
T6 → T7
```

---

## Task Breakdown

### T1: CustomerQueryPort e modelos de leitura

**What:** Port + `CustomerSummary`, `CustomerFilter`, `PageResult`
**Where:** `backend/customer-module/ports/`, `application/` read models
**Depends on:** create-customer concluído (T3 domain)
**Requirement:** CUST-09, CUST-11

**Done when**:

- [x] Port define findById e findAll
- [x] Records imutáveis sem Spring

**Tests:** none
**Gate:** build

---

### T2: Implementação JPA CustomerQueryAdapter

**What:** Queries com filtros e paginação no adapter
**Where:** `backend/customer-module/adapters/persistence/`
**Depends on:** T1
**Requirement:** CUST-11, CUST-13, CUST-14, CUST-15

**Done when**:

- [x] Filtros AND aplicados
- [x] Documento normalizado na query
- [x] Gate check passes: `mvn verify -Pintegration -pl customer-module`
- [x] Test count: ≥4 testes repositório passam

**Tests:** integration
**Gate:** full

---

### T3: GetCustomerByIdUseCase + testes [P]

**What:** Use case busca por ID
**Where:** `backend/customer-module/features/query-customers/GetCustomerByIdUseCase.java`
**Depends on:** T1
**Requirement:** CUST-09, CUST-10

**Done when**:

- [x] Encontrado retorna detail
- [x] Não encontrado lança CustomerNotFoundException
- [x] Gate check passes: `mvn test -pl customer-module`
- [x] Test count: ≥3 testes passam

**Tests:** unit
**Gate:** quick

---

### T4: QueryCustomersUseCase + testes [P]

**What:** Use case listagem paginada
**Where:** `backend/customer-module/features/query-customers/QueryCustomersUseCase.java`
**Depends on:** T1
**Requirement:** CUST-11, CUST-12, CUST-13

**Done when**:

- [x] Defaults page=0 size=20 max=100
- [x] Metadata preenchida corretamente
- [x] Gate check passes: `mvn test -pl customer-module`
- [x] Test count: ≥4 testes passam

**Tests:** unit
**Gate:** quick

---

### T5: DTOs de resposta query [P]

**What:** Response records para list e detail
**Where:** `backend/customer-module/features/query-customers/`
**Depends on:** T1
**Requirement:** CUST-12

**Done when**:

- [x] Envelope data/metadata conforme CONVENTIONS

**Tests:** none
**Gate:** build

---

### T6: QueryCustomersController (GET list + GET by id) + testes integração

**What:** Dois endpoints REST na mesma feature slice
**Where:** `backend/customer-module/features/query-customers/QueryCustomersController.java`
**Depends on:** T2, T3, T4, T5
**Requirement:** CUST-09 a CUST-15

**Done when**:

- [x] GET /customers e GET /customers/{id} funcionais
- [x] 404, 400, 200 cobertos
- [x] Gate check passes: `mvn verify -Pintegration -pl customer-module`
- [x] Test count: ≥6 testes passam

**Tests:** integration
**Gate:** full

---

### T7: Registrar beans e smoke verify

**What:** Wiring no CustomerModuleConfig
**Depends on:** T6
**Requirement:** CUST-09 a CUST-15

**Done when**:

- [x] Endpoints expostos na application
- [x] Verify curl documentado

**Tests:** none (coberto por `ApplicationWiringIntegrationTest`)
**Gate:** full

**Verified:** 2026-06-15 — `mvn verify -Pintegration -pl customer-module,application` passou; beans e endpoints GET registrados na application.

**Verify:**

```bash
# Listagem paginada (requer app rodando: mvn spring-boot:run -pl application)
curl -s http://localhost:8080/api/v1/customers | jq .

# Detalhe por ID (substitua {id} pelo UUID retornado no POST ou listagem)
curl -s http://localhost:8080/api/v1/customers/{id} | jq .
```

Esperado: HTTP 200 com envelope `{ data, metadata }`; listagem vazia retorna `data: []` e `metadata.totalElements: 0`; ID inexistente → HTTP 404 Problem Details.

**Total tasks: 7**

---

## Task Granularity Check

| Task | Scope | Status |
| ---- | ----- | ------ |
| T3 | 1 use case | ✅ Granular |
| T6 | 1 controller, 2 GET | ✅ Granular (coeso) |

---

## Diagram-Definition Cross-Check

| Task | Depends On | Diagram | Status |
| ---- | ---------- | ------- | ------ |
| T2 | T1 | T1→T2 | ✅ |
| T3 | T1 | T2→T3 parallel | ✅ |
| T6 | T2,T3,T4,T5 | merge→T6 | ✅ |

---

## Test Co-location Validation

| Task | Layer | Matrix | Task | Status |
| ---- | ----- | ------ | ---- | ------ |
| T2 | Repository | integration | integration | ✅ |
| T3 | Use case | unit | unit | ✅ |
| T4 | Use case | unit | unit | ✅ |
| T6 | Controller | integration | integration | ✅ |

---

> **Feature close:** This feature predates the mandatory doc-close task. Before marking Done, run the **Feature Close Checklist** in `AGENTS.md` (update `spec.md`, `design.md`, brownfield docs). Template for future features: jwt-auth T13.
