# Create Account — Especificação

**Módulo:** `account-module`
**Endpoint:** `POST /api/v1/accounts`
**Sprint:** 1 — Core Banking

---

## Problem Statement

Após cadastrar um cliente, a plataforma precisa abrir contas bancárias vinculadas para permitir movimentações financeiras. A abertura deve validar existência do cliente, iniciar conta em status ACTIVE, registrar saldo zero via ledger (sem `setBalance`) e publicar evento `AccountCreated` para rastreabilidade event-driven.

## Goals

- [ ] Criar conta vinculada a `customerId` existente
- [ ] Status inicial `ACTIVE`
- [ ] Saldo inicial zero via projeção/ledger (não armazenar balance autoritativo)
- [ ] Publicar domain event `AccountCreated` após persistência bem-sucedida
- [ ] Retornar conta criada no envelope REST

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Encerramento | Feature `close-account` |
| Transferências | Feature `transfer-money` |
| Tipos de conta (poupança, etc.) | Apenas conta corrente simples S1 |
| Tarifas de abertura | Fora escopo v1 |
| Múltiplas moedas | Apenas BRL |
| Limite de contas por cliente | YAGNI S1 |

---

## User Stories

### P1: Abrir conta para cliente existente ⭐ MVP

**User Story:** Como operador, quero abrir uma conta para um cliente cadastrado para iniciar operações financeiras.

**Why P1:** Pré-requisito para transferência e saldo.

**Acceptance Criteria**:

1. WHEN `POST /api/v1/accounts` com `{ "customerId": "<uuid>" }` e cliente existe THEN sistema SHALL retornar `201 Created` com `{ data: { id, customerId, status: "ACTIVE", createdAt }, metadata: {} }`
2. WHEN conta criada THEN status SHALL ser `ACTIVE`
3. WHEN conta criada THEN saldo consultável SHALL ser `Money.zero()` (projeção ledger)
4. WHEN customerId inexistente THEN sistema SHALL retornar `404 Not Found`

**Independent Test:** POST account após create customer retorna 201 ACTIVE.

---

### P2: Publicar AccountCreated

**User Story:** Como arquiteto, quero evento `AccountCreated` publicado após abertura para desacoplar audit/monitoring.

**Acceptance Criteria**:

1. WHEN conta persistida com sucesso THEN sistema SHALL publicar `AccountCreated` via `EventPublisherPort`
2. WHEN evento publicado THEN payload SHALL conter `accountId`, `customerId`, `occurredAt`, `eventId`
3. WHEN falha na persistência THEN sistema SHALL NÃO publicar evento

**Independent Test:** Integration test com Kafka Testcontainer recebe mensagem no topic `account-created`.

---

### P3: Lançamento de abertura no ledger (zero balance)

**User Story:** Como responsável financeiro, quero que abertura de conta respeite ledger-first mesmo com saldo zero.

**Acceptance Criteria**:

1. WHEN conta criada THEN sistema SHALL invocar `LedgerPort` para registrar abertura (sem movimentação financeira OU entry zerada conforme design)
2. WHEN abertura THEN sistema SHALL NÃO chamar `account.setBalance(...)`
3. WHEN saldo consultado após abertura THEN SHALL retornar zero via projeção ledger

**Independent Test:** Ledger stub/registro confirma zero balance projection.

---

## Edge Cases

- WHEN customerId UUID malformado THEN 400
- WHEN POST duplicado simultâneo (mesmo customer, duas contas) THEN ambas permitidas S1 (sem limite)
- WHEN falha Kafka após commit DB THEN estratégia outbox/retry documentada no design
- WHEN ledger indisponível THEN transação SHALL rollback (consistência)

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| ACCT-01 | P1: POST create ACTIVE | Design | Pending |
| ACCT-02 | P1: Validar customer exists | Design | Pending |
| ACCT-03 | P1: Resposta 201 | Design | Pending |
| ACCT-04 | P2: Event AccountCreated | Design | Pending |
| ACCT-05 | P2: Payload evento | Design | Pending |
| ACCT-06 | P3: LedgerPort abertura | Design | Pending |
| ACCT-07 | P3: Saldo zero projeção | Design | Pending |

**Coverage:** 7 total, 0 mapped, 7 pending

---

## Success Criteria

- [ ] Fluxo create customer → create account demonstrável
- [ ] Evento publicado em Kafka (integration test)
- [ ] Nenhuma mutação direta de balance
- [ ] Regras no domain/application apenas
