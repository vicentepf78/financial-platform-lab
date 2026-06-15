# Criação de Cobrança com QR Code PIX

**Módulo:** `billing-module`  
**Sprint:** 3 — Cobranças  
**Evento de domínio:** `ChargeCreated`  
**Referência técnica:** [`docs/integrations/mercadopago/`](../../../docs/integrations/mercadopago/README.md)

## Problem Statement

A plataforma precisa gerar cobranças PIX para receber pagamentos de clientes. Sem cobrança estruturada, não há como iniciar o fluxo de pagamento via Mercado Pago nem registrar a liquidação no ledger após confirmação do webhook.

## Goals

- [ ] Criar cobrança associada a conta e valor com status inicial PENDING
- [ ] Gerar QR Code PIX via Mercado Pago Orders API (`POST /v1/orders`, `type: online`)
- [ ] Persistir referência externa (`externalOrderId`) e dados do QR Code
- [ ] Publicar evento `ChargeCreated` após criação bem-sucedida
- [ ] Expor APIs de criação, consulta e listagem de cobranças

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Cobrança via boleto ou cartão | POC limitada a PIX online |
| QR presencial / PDV (`type: qr`) | Fora de escopo v1 — ver doc Mercado Pago |
| Cancelamento automático por expiração | Job agendado — Sprint futura |
| Split de pagamento / marketplace | Complexidade fora da POC |

---

## User Stories

### P1: Criar cobrança PIX com QR Code ⭐ MVP

**User Story**: Como operador do backoffice, quero criar uma cobrança PIX informando conta e valor, para que o pagador receba um QR Code e possa pagar.

**Why P1**: Sem criação de cobrança, todo o fluxo de billing (webhook, liquidação, UI) é impossível.

**Acceptance Criteria**:

1. WHEN operador envia `POST /api/v1/charges` com `accountId`, `amount` e `description` THEN o sistema SHALL criar cobrança com status PENDING e chamar `PaymentGatewayPort.createPixCharge`
2. WHEN Mercado Pago retorna ordem criada THEN o sistema SHALL persistir `externalOrderId`, `qrCode` (copia-e-cola) e `qrCodeBase64` (imagem)
3. WHEN cobrança é criada com sucesso THEN o sistema SHALL publicar evento `ChargeCreated` com chargeId, accountId, amount e externalOrderId
4. WHEN `accountId` não existe ou conta está encerrada THEN o sistema SHALL rejeitar com 422 sem chamar gateway
5. WHEN mesma `X-Idempotency-Key` é reenviada THEN o sistema SHALL retornar cobrança existente sem duplicar

**Independent Test**: Criar cobrança via API e verificar resposta com QR Code e status PENDING no banco.

---

### P2: Consultar e listar cobranças

**User Story**: Como operador, quero consultar cobranças por ID e listar cobranças pendentes/liquidadas, para acompanhar recebimentos.

**Why P2**: Essencial para operação; criação isolada não basta para gestão.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/charges/{id}` THEN o sistema SHALL retornar cobrança com status, valor, QR Code e timestamps
2. WHEN operador solicita `GET /api/v1/charges?status=PENDING` THEN o sistema SHALL retornar lista paginada filtrada por status
3. WHEN cobrança não existe THEN o sistema SHALL retornar 404

**Independent Test**: Criar 3 cobranças (2 PENDING, 1 PAID) e verificar filtros de listagem.

---

### P3: Descrição e metadados customizados

**User Story**: Como operador, quero incluir descrição e referência externa na cobrança, para identificar pagamentos no extrato.

**Why P3**: Melhora rastreabilidade; não bloqueia fluxo principal.

**Acceptance Criteria**:

1. WHEN operador informa `description` e `externalReference` THEN o sistema SHALL persistir e enviar ao Mercado Pago no payload da ordem
2. WHEN cobrança é liquidada THEN o sistema SHALL incluir `description` nos lançamentos do ledger

---

## Edge Cases

- WHEN valor é menor que mínimo do Mercado Pago THEN o sistema SHALL retornar erro de validação com detalhe do gateway
- WHEN Mercado Pago está indisponível THEN o sistema SHALL retornar 502 e não persistir cobrança parcial
- WHEN `amount` possui mais de 2 casas decimais THEN o sistema SHALL rejeitar com 400

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| CHG-01 | P1: Criar cobrança PIX | Design | Pending |
| CHG-02 | P1: Persistir QR Code | Design | Pending |
| CHG-03 | P1: Publicar ChargeCreated | Design | Pending |
| CHG-04 | P1: Validar conta | Design | Pending |
| CHG-05 | P1: Idempotência | Design | Pending |
| CHG-06 | P2: Consultar cobrança | Design | Pending |
| CHG-07 | P2: Listar por status | Design | Pending |
| CHG-08 | P3: Metadados customizados | Design | Pending |
| CHG-09 | Edge: Falha gateway | Design | Pending |

**Coverage:** 9 total, 0 mapped to tasks, 9 unmapped ⚠️

---

## Success Criteria

- [ ] Cobrança criada retorna QR Code válido testável no sandbox Mercado Pago
- [ ] Evento `ChargeCreated` publicado em 100% das criações bem-sucedidas
- [ ] Nenhuma lógica de negócio no controller — apenas delegação ao use case
- [ ] Testes de integração com mock do PaymentGatewayPort cobrem fluxo completo
