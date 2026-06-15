# Envio de PIX

**Módulo:** `pix-module`  
**Sprint:** 4 — PIX  
**Evento de domínio:** `PixSent`  
**Dependências:** `register-pix-key`, `register-ledger-entry`, `calculate-balance`

## Problem Statement

Usuários da plataforma precisam enviar transferências PIX para chaves de destino, com débito na conta de origem e registro no ledger. Sem envio PIX, o módulo não demonstra o fluxo de pagamento instantâneo essencial em fintechs brasileiras.

## Goals

- [ ] Executar transferência PIX debitando conta de origem via ledger (double entry)
- [ ] Validar saldo suficiente antes do envio
- [ ] Registrar transação com status e identificador end-to-end (E2E)
- [ ] Publicar evento `PixSent` após conclusão
- [ ] Consultar status de transferência PIX

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Integração SPI real com Banco do Brasil | Fase 3 — v1 simula processamento interno |
| PIX agendado | Fora do escopo da POC |
| PIX com valor alterado (troco) | Não aplicável |
| Devolução de PIX (refund) | Sprint futura |

---

## User Stories

### P1: Enviar PIX para chave de destino ⭐ MVP

**User Story**: Como operador, quero enviar um PIX informando conta de origem, chave de destino e valor, para transferir fundos instantaneamente.

**Why P1**: Operação central do módulo PIX.

**Acceptance Criteria**:

1. WHEN operador envia `POST /api/v1/pix/transfers` com `originAccountId`, `destinationKey`, `amount` e `description` THEN o sistema SHALL validar saldo, registrar débito na origem e crédito em conta transitória PIX via ledger
2. WHEN transferência é processada com sucesso THEN o sistema SHALL persistir transação com status COMPLETED e `e2eId` gerado
3. WHEN transferência é concluída THEN o sistema SHALL publicar evento `PixSent` com originAccountId, destinationKey, amount e e2eId
4. WHEN saldo insuficiente THEN o sistema SHALL rejeitar com 422 `InsufficientBalance` sem lançamentos
5. WHEN mesma `idempotencyKey` é reenviada THEN o sistema SHALL retornar transação existente

**Independent Test**: Enviar PIX de R$ 50,00 e verificar débito no ledger e evento publicado.

---

### P2: Consultar status de transferência PIX

**User Story**: Como operador, quero consultar o status de uma transferência PIX pelo ID ou e2eId, para acompanhar processamento.

**Why P2**: Rastreabilidade operacional.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/pix/transfers/{id}` THEN o sistema SHALL retornar transação com status, valor, chaves e timestamps
2. WHEN operador solicita `GET /api/v1/pix/transfers?accountId={id}` THEN o sistema SHALL retornar histórico paginado de envios da conta
3. WHEN transferência não existe THEN o sistema SHALL retornar 404

**Independent Test**: Enviar PIX e consultar por ID verificando status COMPLETED.

---

### P3: Validação de limites de transferência

**User Story**: Como sistema, quero validar limites diários e por transação de PIX, para mitigar riscos operacionais.

**Why P3**: Controle de risco — configurável mas não bloqueante para MVP.

**Acceptance Criteria**:

1. WHEN valor excede limite por transação configurado THEN o sistema SHALL rejeitar com 422 `LimitExceeded`
2. WHEN soma de PIX do dia excede limite diário da conta THEN o sistema SHALL rejeitar com 422
3. WHEN limites não estão configurados THEN o sistema SHALL processar sem restrição adicional

---

## Edge Cases

- WHEN chave de destino não existe THEN o sistema SHALL rejeitar com 404 `DestinationKeyNotFound`
- WHEN conta de origem está encerrada THEN o sistema SHALL rejeitar envio
- WHEN processamento simulado falha THEN o sistema SHALL marcar transação como FAILED sem débito no ledger (rollback transacional)
- WHEN valor possui centavos THEN o sistema SHALL usar aritmética `Money` sem perda de precisão

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| PIX-08 | P1: Enviar PIX | Design | Pending |
| PIX-09 | P1: Lançamentos ledger | Design | Pending |
| PIX-10 | P1: Publicar PixSent | Design | Pending |
| PIX-11 | P1: Validação saldo | Design | Pending |
| PIX-12 | P1: Idempotência | Design | Pending |
| PIX-13 | P2: Consultar transferência | Design | Pending |
| PIX-14 | P2: Histórico por conta | Design | Pending |
| PIX-15 | P3: Limites de transferência | Design | Pending |
| PIX-16 | Edge: Chave destino inexistente | Design | Pending |

**Coverage:** 9 total, 0 mapped to tasks, 9 unmapped ⚠️

---

## Success Criteria

- [ ] Envio PIX debita conta de origem via ledger (nunca `setBalance`)
- [ ] Evento `PixSent` publicado em 100% dos envios bem-sucedidos
- [ ] Idempotência garante zero débitos duplicados
- [ ] Testes de integração com PostgreSQL e Kafka passam
