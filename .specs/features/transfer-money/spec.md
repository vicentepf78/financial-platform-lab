# Transfer Money — Especificação

**Módulo:** `account-module`
**Endpoint:** `POST /api/v1/transfers`
**Sprint:** 1 — Core Banking
**Status:** Done
**Prioridade:** ⭐ MAIS IMPORTANTE DO SPRINT 1

---

## Problem Statement

A capacidade central de um core banking é movimentar valor entre contas de forma segura, auditável e consistente. Transferências devem validar contas ativas e saldo suficiente, registrar débito e crédito via ledger (partidas dobradas — mesmo que stub no S1), publicar `TransferExecuted` e nunca alterar saldo diretamente na entidade Account.

## Goals

- [x] Executar transferência entre conta origem e destino com valor positivo em BRL
- [x] Validar contas ACTIVE, distintas, existentes
- [x] Validar saldo suficiente na origem via projeção ledger
- [x] Registrar débito (origem) + crédito (destino) via `LedgerPort.recordTransfer`
- [x] Publicar evento `TransferExecuted` após sucesso transacional
- [x] Garantir idempotência via `idempotencyKey` opcional (P2)
- [x] Incluir `correlationId` para auditabilidade

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| TED/DOC externos | Apenas transferências internas S1 |
| PIX | Sprint 4 |
| Tarifas sobre transferência | Fora escopo v1 |
| Transferência agendada | Futuro |
| Estorno automático | Processo manual S1 |
| Limite diário | Fora escopo v1 |

---

## User Stories

### P1: Transferir entre contas com saldo suficiente ⭐ MVP

**User Story:** Como operador, quero transferir valor da conta A para conta B para movimentar recursos entre clientes.

**Why P1:** Demonstração principal do Core Banking + ledger-first.

**Acceptance Criteria**:

1. WHEN `POST /api/v1/transfers` com `{ "originAccountId", "destinationAccountId", "amount": "100.00", "correlationId" }` válido THEN sistema SHALL retornar `201 Created` com `{ data: { transferId, originAccountId, destinationAccountId, amount, currency: "BRL", status: "COMPLETED", createdAt }, metadata: {} }`
2. WHEN transferência completa THEN LedgerPort SHALL registrar débito na origem e crédito no destino no mesmo unit of work
3. WHEN transferência completa THEN saldo origem SHALL diminuir e destino aumentar (via projeção ledger)
4. WHEN transferência completa THEN sistema SHALL publicar `TransferExecuted` no topic `transfer-executed`
5. WHEN origin equals destination THEN 400 Bad Request

**Independent Test:** Seed saldo origem → transfer → balance origem/destino atualizados.

---

### P2: Rejeitar saldo insuficiente

**User Story:** Como sistema, quero impedir transferência quando origem não tem saldo para evitar saldo negativo.

**Acceptance Criteria**:

1. WHEN amount > saldo origem THEN sistema SHALL retornar `422 Unprocessable Entity` com detail "Insufficient balance"
2. WHEN rejeitado THEN NÃO SHALL haver lançamentos ledger nem evento publicado
3. WHEN rejeitado THEN saldos permanecem inalterados

**Independent Test:** Transfer amount > balance → 422, balances unchanged.

---

### P3: Validar contas inativas e idempotência

**User Story:** Como operador, quero que transferências envolvendo contas encerradas falhem e requisições duplicadas não gerem efeito financeiro duplo.

**Acceptance Criteria**:

1. WHEN origem ou destino CLOSED THEN 409 Conflict
2. WHEN conta inexistente THEN 404
3. WHEN `idempotencyKey` repetido com mesma payload THEN sistema SHALL retornar mesma resposta 201 sem duplicar ledger (P2)
4. WHEN amount zero ou negativo THEN 400

---

## Edge Cases

- WHEN amount com mais de 2 casas decimais THEN normalizar ou 400
- WHEN currency implícita BRL apenas
- WHEN falha Kafka pós-commit DB THEN retry/outbox (documentado no design)
- WHEN concorrência duas transferências mesma origem THEN serialização transacional — uma falha insufficient balance
- WHEN correlationId ausente THEN gerar UUID server-side

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| XFER-01 | P1: POST transfer sucesso | Execute | Done |
| XFER-02 | P1: Ledger débito+crédito | Execute | Done |
| XFER-03 | P1: Event TransferExecuted | Execute | Done |
| XFER-04 | P1: Contas distintas | Execute | Done |
| XFER-05 | P1: Contas ACTIVE | Execute | Done |
| XFER-06 | P2: Saldo insuficiente 422 | Execute | Done |
| XFER-07 | P2: Sem efeito colateral | Execute | Done |
| XFER-08 | P3: Conta CLOSED 409 | Execute | Done |
| XFER-09 | P3: IdempotencyKey | Execute | Done |
| XFER-10 | P3: Amount validation | Execute | Done |
| XFER-11 | Audit: correlationId | Execute | Done |

**Coverage:** 11 total, 11 mapped, 0 pending

---

## Success Criteria

- [x] Demo Sprint 1: create customer → create 2 accounts → credit stub → transfer → consult balances
- [x] Testes unitários use case cobrindo happy path e todos erros
- [x] Teste integração controller + DB + Kafka
- [x] Nenhum `setBalance` em codebase
- [x] Gate `mvn verify -Pintegration` verde para account-module
