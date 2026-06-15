# Processamento de Webhook de Pagamento

**Módulo:** `billing-module`  
**Sprint:** 3 — Cobranças  
**Evento de domínio:** `ChargePaid`  
**Dependências:** `create-charge`, `mercadopago-integration`, `register-ledger-entry`

## Problem Statement

Quando um pagador liquida uma cobrança PIX no Mercado Pago, a plataforma precisa receber a notificação (webhook topic `order`), processá-la de forma idempotente, confirmar o status via API e creditar a conta no ledger. Sem processamento confiável de webhook, cobranças ficam eternamente PENDING.

## Goals

- [ ] Receber webhooks do Mercado Pago no endpoint dedicado
- [ ] Processar notificações de forma idempotente (sem duplicar liquidação)
- [ ] Confirmar pagamento via `GET /v1/orders/{id}` antes de creditar
- [ ] Registrar lançamento de crédito no ledger (débito em conta transitória + crédito na conta do cliente)
- [ ] Publicar evento `ChargePaid` após liquidação

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Webhooks topic `payment` (legacy) | Usar exclusivamente topic `order` |
| Reprocessamento manual de webhook via UI | Sprint futura — v1 confia em retry do MP |
| Estorno/chargeback | Fora do escopo da POC |
| Notificação push ao pagador | Sem canal de comunicação na v1 |

---

## User Stories

### P1: Processar webhook de ordem paga ⭐ MVP

**User Story**: Como sistema, quero processar webhook de ordem paga do Mercado Pago de forma idempotente, para liquidar cobranças e creditar contas automaticamente.

**Why P1**: Liquidação automática é o objetivo central do Sprint 3.

**Acceptance Criteria**:

1. WHEN Mercado Pago envia `POST /api/v1/webhooks/mercadopago` com topic `order` e `action` indicando pagamento THEN o controller SHALL delegar a `ProcessMercadoPagoWebhookUseCase` e retornar 200 imediatamente
2. WHEN webhook é recebido THEN o use case SHALL validar assinatura HMAC antes de processar
3. WHEN `externalOrderId` já foi processado (registro em `webhook_events`) THEN o use case SHALL retornar sucesso sem re-liquidar
4. WHEN status confirmado via `GET /v1/orders/{id}` é `paid` THEN o use case SHALL atualizar cobrança para PAID, registrar débito+crédito no ledger e publicar `ChargePaid`
5. WHEN status confirmado não é `paid` THEN o use case SHALL registrar evento sem efeito financeiro

**Independent Test**: Enviar webhook simulado duas vezes para mesma ordem e verificar que ledger recebe apenas um par de lançamentos.

---

### P2: Persistir histórico de eventos de webhook

**User Story**: Como operador, quero que todos os webhooks recebidos sejam registrados, para auditoria e troubleshooting.

**Why P2**: Essencial para debug de integrações; não bloqueia liquidação.

**Acceptance Criteria**:

1. WHEN webhook é recebido (válido ou inválido) THEN o sistema SHALL persistir payload, headers relevantes, timestamp e resultado do processamento
2. WHEN operador consulta `GET /api/v1/webhooks/events?chargeId={id}` THEN o sistema SHALL retornar histórico de eventos relacionados
3. WHEN assinatura é inválida THEN o sistema SHALL persistir evento com status REJECTED

**Independent Test**: Processar webhook e verificar registro em tabela `webhook_events`.

---

### P3: Retry e dead-letter para falhas

**User Story**: Como sistema, quero reprocessar webhooks que falharam por erro transitório, para garantir liquidação eventual.

**Why P3**: Resiliência — importante mas MP já faz retry de webhooks.

**Acceptance Criteria**:

1. WHEN processamento falha por erro transitório (timeout, DB indisponível) THEN o sistema SHALL retornar 500 para MP reenviar
2. WHEN processamento falha após 5 tentativas THEN o sistema SHALL marcar evento como FAILED para intervenção manual
3. WHEN operador solicita reprocessamento de evento FAILED THEN o sistema SHALL reexecutar use case de liquidação

---

## Edge Cases

- WHEN webhook referencia ordem inexistente na plataforma THEN o sistema SHALL registrar evento com status ORPHAN sem efeito financeiro
- WHEN valor pago difere do valor da cobrança THEN o sistema SHALL marcar cobrança como DIVERGENT e não liquidar automaticamente
- WHEN cobrança já está PAID e webhook duplicado chega THEN o sistema SHALL retornar 200 sem novos lançamentos (idempotência)
- WHEN ledger falha após atualizar cobrança para PAID THEN o sistema SHALL garantir consistência via transação ou compensação

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| WH-01 | P1: Receber webhook topic order | Design | Pending |
| WH-02 | P1: Validar assinatura HMAC | Design | Pending |
| WH-03 | P1: Idempotência por externalOrderId | Design | Pending |
| WH-04 | P1: Confirmar via GET antes de liquidar | Design | Pending |
| WH-05 | P1: Lançamentos ledger + ChargePaid | Design | Pending |
| WH-06 | P2: Persistir histórico webhook | Design | Pending |
| WH-07 | P2: Consultar eventos por cobrança | Design | Pending |
| WH-08 | P3: Retry e dead-letter | Design | Pending |
| WH-09 | Edge: Divergência de valor | Design | Pending |

**Coverage:** 9 total, 0 mapped to tasks, 9 unmapped ⚠️

---

## Success Criteria

- [ ] Webhook duplicado não gera lançamentos duplicados no ledger
- [ ] Cobrança PENDING transita para PAID após pagamento no sandbox MP
- [ ] Evento `ChargePaid` publicado com correlationId vinculado ao webhook
- [ ] Teste de integração simula webhook completo: notificação → GET → liquidação → ledger
