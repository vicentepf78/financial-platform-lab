# Recebimento de PIX

**Módulo:** `pix-module`  
**Sprint:** 4 — PIX  
**Evento de domínio:** `PixReceived`  
**Dependências:** `register-pix-key`, `register-ledger-entry`

## Problem Statement

Quando um pagamento PIX é direcionado a uma chave cadastrada na plataforma, o sistema precisa registrar o recebimento, creditar a conta de destino no ledger e publicar evento para rastreabilidade. Sem recebimento PIX, o fluxo financeiro fica incompleto (apenas saídas, sem entradas).

## Goals

- [ ] Registrar PIX recebido vinculado a chave e conta de destino
- [ ] Creditar conta via lançamentos no ledger (débito transitório + crédito na conta)
- [ ] Publicar evento `PixReceived` após registro
- [ ] Garantir idempotência por `e2eId` (evitar crédito duplicado)
- [ ] Expor API de consulta de recebimentos

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Webhook do Banco do Brasil para PIX recebido | Fase 3 — v1 usa endpoint interno/simulado |
| Conciliação automática PIX recebido vs CNAB | Sprint 5 — reconciliation-module |
| Split de recebimento entre contas | Fora do escopo da POC |
| PIX recebido com valor divergente do esperado | Tratamento manual na v1 |

---

## User Stories

### P1: Registrar PIX recebido e creditar conta ⭐ MVP

**User Story**: Como sistema, quero registrar um PIX recebido e creditar a conta de destino, para refletir entrada de fundos no ledger.

**Why P1**: Complementa envio PIX — sem recebimento, saldo só diminui.

**Acceptance Criteria**:

1. WHEN sistema recebe `POST /api/v1/pix/incoming` com `destinationKey`, `amount`, `e2eId`, `payerInfo` e `description` THEN o use case SHALL localizar conta via chave PIX e registrar crédito no ledger
2. WHEN PIX é registrado com sucesso THEN o sistema SHALL persistir transação com status COMPLETED e publicar `PixReceived`
3. WHEN `e2eId` já foi processado THEN o sistema SHALL retornar transação existente sem novo crédito (idempotência)
4. WHEN chave de destino não existe ou está INACTIVE THEN o sistema SHALL rejeitar com 404
5. WHEN lançamento no ledger é criado THEN o sistema SHALL gerar débito em conta transitória PIX e crédito na conta de destino

**Independent Test**: Simular PIX recebido para chave cadastrada e verificar crédito no ledger.

---

### P2: Consultar recebimentos PIX

**User Story**: Como operador, quero consultar PIX recebidos por conta, para verificar entradas de fundos.

**Why P2**: Visibilidade operacional do fluxo de recebimento.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/pix/incoming?accountId={id}` THEN o sistema SHALL retornar lista paginada de recebimentos
2. WHEN operador solicita `GET /api/v1/pix/incoming/{e2eId}` THEN o sistema SHALL retornar detalhe do recebimento
3. WHEN operador filtra por `startDate` e `endDate` THEN o sistema SHALL retornar recebimentos no período

**Independent Test**: Registrar 2 recebimentos e verificar listagem por conta.

---

### P3: Vincular recebimento a cobrança existente

**User Story**: Como sistema, quero vincular automaticamente PIX recebido a cobrança pendente quando valor e referência coincidem, para liquidar cobranças via PIX direto.

**Why P3**: Integração billing↔pix — melhora fluxo mas não bloqueia recebimento isolado.

**Acceptance Criteria**:

1. WHEN PIX recebido possui `externalReference` correspondente a cobrança PENDING com mesmo valor THEN o sistema SHALL atualizar cobrança para PAID e publicar `ChargePaid`
2. WHEN valor difere da cobrança THEN o sistema SHALL registrar PIX recebido sem liquidar cobrança
3. WHEN múltiplas cobranças PENDING com mesmo valor existem THEN o sistema SHALL vincular à mais antiga (FIFO)

---

## Edge Cases

- WHEN `e2eId` é nulo ou vazio THEN o sistema SHALL rejeitar com 400
- WHEN valor é zero ou negativo THEN o sistema SHALL rejeitar com 400
- WHEN conta de destino está encerrada THEN o sistema SHALL rejeitar recebimento
- WHEN ledger falha após persistir transação THEN o sistema SHALL garantir consistência via transação atômica

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| PIX-17 | P1: Registrar PIX recebido | Design | Pending |
| PIX-18 | P1: Crédito via ledger | Design | Pending |
| PIX-19 | P1: Publicar PixReceived | Design | Pending |
| PIX-20 | P1: Idempotência e2eId | Design | Pending |
| PIX-21 | P2: Consultar recebimentos | Design | Pending |
| PIX-22 | P2: Filtro por período | Design | Pending |
| PIX-23 | P3: Vincular a cobrança | Design | Pending |
| PIX-24 | Edge: Conta encerrada | Design | Pending |

**Coverage:** 8 total, 0 mapped to tasks, 8 unmapped ⚠️

---

## Success Criteria

- [ ] PIX recebido credita conta via ledger (double entry)
- [ ] Idempotência por `e2eId` impede créditos duplicados em testes de replay
- [ ] Evento `PixReceived` publicado com todos os metadados de auditoria
- [ ] Saldo da conta após recebimento corresponde ao calculado por `calculate-balance`
