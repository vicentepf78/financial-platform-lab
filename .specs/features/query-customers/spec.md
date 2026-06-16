# Query Customers — Especificação

**Módulo:** `customer-module`
**Endpoints:** `GET /api/v1/customers`, `GET /api/v1/customers/{id}`
**Sprint:** 1 — Core Banking
**Status:** Done

---

## Problem Statement

Operadores e outros módulos (account) precisam localizar clientes cadastrados para abrir contas, consultar dados cadastrais e auditar cadastros. Sem API de consulta paginada e por ID, o backoffice não consegue operar nem validar existência de cliente antes de vincular conta.

## Goals

- [x] Listar clientes com paginação (`page`, `size`)
- [x] Filtrar por nome (parcial), tipo (PF/PJ) e documento (parcial ou exato)
- [x] Consultar cliente individual por UUID
- [x] Retornar envelope `{ data, metadata }` com metadados de paginação

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Cadastro/atualização | Features `create-customer`, `update-customer` |
| Busca full-text avançada | Fora do escopo S1 |
| Exportação CSV | Sprint posterior |
| Cache de consulta | YAGNI S1 |
| Soft delete / clientes inativos | Não previsto S1 |

---

## User Stories

### P1: Consultar cliente por ID ⭐ MVP

**User Story:** Como operador, quero buscar um cliente pelo ID para verificar dados antes de abrir conta.

**Why P1:** create-account depende de customerId válido.

**Acceptance Criteria**:

1. WHEN `GET /api/v1/customers/{id}` com UUID existente THEN sistema SHALL retornar `200 OK` com `{ data: { id, name, type, document, email, createdAt, updatedAt }, metadata: {} }`
2. WHEN UUID inexistente THEN sistema SHALL retornar `404 Not Found` com Problem Details
3. WHEN UUID malformado THEN sistema SHALL retornar `400 Bad Request`

**Independent Test:** GET após POST create retorna mesmo cliente.

---

### P2: Listar clientes paginados

**User Story:** Como operador, quero listar clientes paginados para navegar no backoffice.

**Why P2:** Essencial para UI de gestão de clientes.

**Acceptance Criteria**:

1. WHEN `GET /api/v1/customers?page=0&size=20` THEN sistema SHALL retornar lista paginada em `data.content` (ou `data` array) e `metadata` com `page`, `size`, `totalElements`, `totalPages`
2. WHEN `size` omitido THEN sistema SHALL usar default 20 (max 100)
3. WHEN página além do total THEN sistema SHALL retornar lista vazia com metadados corretos

**Independent Test:** Com 25 clientes seed, page=0 size=20 retorna 20; page=1 retorna 5.

---

### P3: Filtrar listagem

**User Story:** Como operador, quero filtrar clientes por nome, tipo ou documento para encontrar cadastros rapidamente.

**Why P3:** Melhora operabilidade sem complexidade de search engine.

**Acceptance Criteria**:

1. WHEN `GET /api/v1/customers?name=Maria` THEN sistema SHALL retornar clientes cujo nome contém "Maria" (case-insensitive)
2. WHEN `GET /api/v1/customers?type=INDIVIDUAL` THEN sistema SHALL filtrar apenas PF
3. WHEN `GET /api/v1/customers?document=52998224725` THEN sistema SHALL filtrar por documento (normalizado)
4. WHEN filtros combinados THEN sistema SHALL aplicar AND lógico

**Independent Test:** Filtro por documento retorna exatamente um registro.

---

## Edge Cases

- WHEN `size=0` ou `size>100` THEN sistema SHALL retornar `400` ou ajustar para limites configurados
- WHEN `page` negativo THEN sistema SHALL retornar `400`
- WHEN nenhum filtro e zero clientes THEN sistema SHALL retornar `200` com lista vazia
- WHEN documento filtrado com máscara THEN sistema SHALL normalizar

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| CUST-09 | P1: GET by id 200 | Execute | Done |
| CUST-10 | P1: GET by id 404 | Execute | Done |
| CUST-11 | P2: Paginação | Execute | Done |
| CUST-12 | P2: Metadata paginação | Execute | Done |
| CUST-13 | P3: Filtro nome | Execute | Done |
| CUST-14 | P3: Filtro tipo | Execute | Done |
| CUST-15 | P3: Filtro documento | Execute | Done |

**Coverage:** 7 total, 7 mapped to tasks (T1–T7), 7 done

---

## Success Criteria

- [x] Ambos endpoints funcionais com testes integração
- [x] Paginação consistente com convenção REST do projeto
- [x] Consulta read-only — sem regras de negócio em controller/repository além de query
- [x] Performance aceitável com índice em `document` e `name`
