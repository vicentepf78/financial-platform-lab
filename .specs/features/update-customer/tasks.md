# Update Customer — Tasks

**Design:** `.specs/features/update-customer/design.md`
**Status:** Done

---

## Execution Plan

```text
T1 → T2 → T3 → T4 → T5
```

---

## Task Breakdown

### T1: Método Customer.update no domínio + testes

**What:** Lógica mutável e touch auditoria
**Where:** `backend/customer-module/domain/Customer.java`
**Depends on:** create-customer T3
**Requirement:** CUST-16, CUST-17, CUST-19, CUST-20

**Done when**:

- [x] update parcial funciona
- [x] document/type imutáveis lançam exceção
- [x] Gate: `mvn test -pl customer-module`
- [x] Test count: ≥5 passam

**Tests:** unit | **Gate:** quick

---

### T2: UpdateCustomerUseCase + testes

**What:** Orquestração load/save
**Where:** `backend/customer-module/features/update-customer/`
**Depends on:** T1
**Requirement:** CUST-16, CUST-18

**Done when**:

- [x] 404 quando não encontrado
- [x] Gate: `mvn test -pl customer-module`
- [x] Test count: ≥4 passam

**Tests:** unit | **Gate:** quick

---

### T3: UpdateCustomerRequest DTO

**What:** Record PATCH com validação
**Where:** `backend/customer-module/features/update-customer/`
**Depends on:** T1
**Requirement:** CUST-19, CUST-20

**Done when**:

- [x] Campos opcionais Optional/nullable
- [x] Validator rejeita document/type

**Tests:** none | **Gate:** build

---

### T4: UpdateCustomerController + testes integração

**What:** PATCH endpoint
**Where:** `backend/customer-module/features/update-customer/UpdateCustomerController.java`
**Depends on:** T2, T3
**Requirement:** CUST-16 a CUST-20

**Done when**:

- [x] 200, 400, 404 cobertos
- [x] Gate: `mvn verify -Pintegration -pl customer-module`
- [x] Test count: ≥5 passam

**Tests:** integration | **Gate:** full

---

### T5: Wiring beans + verify

**What:** Registrar no CustomerModuleConfig
**Depends on:** T4
**Requirement:** CUST-16 a CUST-20

**Done when**:

- [x] PATCH exposto
- [x] Smoke curl documentado

**Tests:** none | **Gate:** full

**Total tasks: 5**

---

## Smoke Verify

```bash
curl -s -X PATCH http://localhost:8080/api/v1/customers/{id} \
  -H 'Content-Type: application/json' \
  -d '{"name":"Updated Name"}' | jq .
```

---

## Test Co-location Validation

| Task | Layer | Matrix | Status |
| ---- | ----- | ------ | ------ |
| T1 | Domain | unit | ✅ |
| T2 | Use case | unit | ✅ |
| T4 | Controller | integration | ✅ |
