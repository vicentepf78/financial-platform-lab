# Update Customer — Especificação

**Módulo:** `customer-module`
**Endpoint:** `PATCH /api/v1/customers/{id}`
**Sprint:** 1 — Core Banking
**Status:** Done

---

## Problem Statement

Dados cadastrais de clientes mudam ao longo do tempo (e-mail, nome). O sistema precisa permitir atualização parcial controlada sem alterar documento (CPF/CNPJ) — identificador fiscal imutável após cadastro — mantendo trilha de auditoria.

## Goals

- [x] Atualizar campos mutáveis: `name`, `email`
- [x] Impedir alteração de `document` e `type` via PATCH
- [x] Registrar `updatedAt` e `updatedBy` em toda alteração
- [x] Retornar cliente atualizado no envelope padrão

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Alteração de CPF/CNPJ | Regra cadastral — exige novo cadastro/processo manual |
| Alteração de type PF↔PJ | Incompatível com documento existente |
| Atualização de telefone | Sprint 2+ — Customer aggregate has no phone field yet |
| Exclusão/inativação | Não previsto S1 |
| Histórico de versões | audit-module futuro |
| Validação de e-mail via link | Fora escopo v1 |

---

## User Stories

### P1: Atualizar e-mail e nome ⭐ MVP

**User Story:** Como operador, quero atualizar e-mail e nome de um cliente para manter cadastro correto.

**Why P1:** Operação cadastral básica do backoffice.

**Acceptance Criteria**:

1. WHEN `PATCH /api/v1/customers/{id}` com `{ "email": "novo@example.com" }` THEN sistema SHALL atualizar apenas e-mail e retornar `200 OK` com data completa
2. WHEN PATCH inclui `name` válido THEN sistema SHALL atualizar nome
3. WHEN atualização bem-sucedida THEN sistema SHALL atualizar `updatedAt` e `updatedBy`
4. WHEN cliente inexistente THEN sistema SHALL retornar `404 Not Found`

**Independent Test:** PATCH e-mail após create; GET confirma novo valor.

---

### P2: Rejeitar alteração de documento

**User Story:** Como compliance, quero que documento fiscal não seja alterável via API para evitar fraude cadastral.

**Why P2:** Integridade do vínculo fiscal.

**Acceptance Criteria**:

1. WHEN PATCH inclui campo `document` THEN sistema SHALL retornar `400 Bad Request` ignorando alteração
2. WHEN PATCH inclui campo `type` THEN sistema SHALL retornar `400 Bad Request`
3. WHEN body contém apenas campos mutáveis THEN sistema SHALL processar normalmente

**Independent Test:** PATCH com document diferente retorna 400; DB inalterado.

---

### P3: Atualização parcial (patch semantics)

**User Story:** Como operador, quero enviar apenas campos que mudaram sem obrigar payload completo.

**Why P3:** PATCH semântico reduz erros.

**Acceptance Criteria**:

1. WHEN PATCH com subset de campos THEN sistema SHALL alterar somente campos presentes
2. WHEN campo omitido THEN sistema SHALL preservar valor anterior
3. WHEN PATCH com body vazio `{}` THEN sistema SHALL retornar `400` (nada a atualizar)

---

## Edge Cases

- WHEN e-mail inválido THEN 400
- WHEN nome vazio string THEN 400
- WHEN UUID malformado THEN 400
- WHEN concorrência (dois PATCH simultâneos) THEN last-write-wins com updatedAt consistente

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| CUST-16 | P1: PATCH campos mutáveis | Execute | Done |
| CUST-17 | P1: Auditoria update | Execute | Done |
| CUST-18 | P1: 404 not found | Execute | Done |
| CUST-19 | P2: Bloqueio document/type | Execute | Done |
| CUST-20 | P3: Semântica parcial | Execute | Done |

**Coverage:** 5 total, 5 mapped, 0 pending

---

## Success Criteria

- [x] PATCH funcional com testes unitários e integração
- [x] Documento permanece imutável após cadastro
- [x] Regras de mutabilidade no domínio (`Customer.update(...)`)
