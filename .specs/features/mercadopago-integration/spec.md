# Integração Mercado Pago (Orders API)

**Módulo:** `billing-module`  
**Sprint:** 3 — Cobranças  
**Referência:** [`docs/integrations/mercadopago/`](../../../docs/integrations/mercadopago/README.md)

## Problem Statement

O billing-module precisa de um adapter que abstraia a comunicação com o Mercado Pago via Orders API (Checkout Transparente), sem acoplar o domínio ao SDK ou payloads externos. A integração deve seguir as decisões confirmadas: `POST /v1/orders` com `type: online`, webhooks topic `order`, e idempotência via `X-Idempotency-Key`.

## Goals

- [ ] Implementar `PaymentGatewayPort` com adapter Mercado Pago
- [ ] Criar ordem PIX online via `POST /v1/orders`
- [ ] Consultar status via `GET /v1/orders/{id}`
- [ ] Isolar DTOs do Mercado Pago nos adapters (sem vazamento para domain)
- [ ] Configurar credenciais via variáveis de ambiente (sandbox)

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Payments API legacy (`/v1/payments`) | Deprecada — MP recomenda Orders para novas integrações |
| Webhooks topic `payment` ou `merchant_order` | Usar exclusivamente topic `order` |
| Produção / credenciais reais | Sandbox apenas na POC |
| Checkout Pro, Bricks, cartão, boleto | Fora do escopo v1 |
| SDK oficial Mercado Pago | HTTP client direto ou RestClient — decisão de implementação |

---

## User Stories

### P1: Criar ordem PIX no Mercado Pago ⭐ MVP

**User Story**: Como sistema, quero criar uma ordem PIX no Mercado Pago via Orders API, para obter QR Code de pagamento.

**Why P1**: Sem adapter de criação, `create-charge` não funciona.

**Acceptance Criteria**:

1. WHEN `MercadoPagoPaymentGatewayAdapter.createPixCharge(command)` é invocado THEN o adapter SHALL enviar `POST /v1/orders` com `type: "online"` e método de pagamento PIX
2. WHEN Mercado Pago retorna 201 THEN o adapter SHALL mapear resposta para `PaymentOrderResult` contendo `externalOrderId`, `qrCode`, `qrCodeBase64` e `status`
3. WHEN requisição inclui `X-Idempotency-Key` THEN o adapter SHALL enviar header idêntico ao Mercado Pago
4. WHEN Mercado Pago retorna erro 4xx/5xx THEN o adapter SHALL lançar exceção de infraestrutura sem expor detalhes sensíveis ao cliente

**Independent Test**: Mock do servidor MP ou sandbox — criar ordem e verificar payload conforme doc `02-orders-api-pix.md`.

---

### P2: Consultar status de ordem

**User Story**: Como sistema, quero consultar o status de uma ordem no Mercado Pago, para confirmar liquidação antes de creditar conta.

**Why P2**: Webhook notifica evento; confirmação via GET é obrigatória antes de liquidação financeira.

**Acceptance Criteria**:

1. WHEN `getOrderStatus(externalOrderId)` é invocado THEN o adapter SHALL enviar `GET /v1/orders/{id}`
2. WHEN ordem está com status `paid` THEN o adapter SHALL retornar `PaymentOrderStatus.PAID` com valor e timestamp de pagamento
3. WHEN ordem está `pending` ou `expired` THEN o adapter SHALL retornar status correspondente sem efeito financeiro

**Independent Test**: Consultar ordem criada no sandbox e verificar mapeamento de status.

---

### P3: Validação de assinatura HMAC de webhook

**User Story**: Como sistema, quero validar a assinatura HMAC dos webhooks do Mercado Pago, para rejeitar notificações fraudulentas.

**Why P3**: Segurança — importante mas webhook pode ser processado em feature separada inicialmente.

**Acceptance Criteria**:

1. WHEN webhook é recebido THEN `MercadoPagoWebhookSignatureValidator` SHALL validar header `x-signature` conforme doc `03-webhooks.md`
2. WHEN assinatura é inválida THEN o validator SHALL rejeitar com exceção de segurança
3. WHEN `x-request-id` está ausente THEN o validator SHALL rejeitar a requisição

---

## Edge Cases

- WHEN access token expirado THEN o adapter SHALL retornar erro claro para renovação de credenciais
- WHEN timeout na API do MP (> 30s) THEN o adapter SHALL falhar com retry elegível
- WHEN resposta do MP possui campos inesperados THEN o adapter SHALL ignorar campos desconhecidos (forward compatibility)

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| MP-01 | P1: POST /v1/orders PIX online | Design | Pending |
| MP-02 | P1: Mapear PaymentOrderResult | Design | Pending |
| MP-03 | P1: Header X-Idempotency-Key | Design | Pending |
| MP-04 | P1: Tratamento de erros gateway | Design | Pending |
| MP-05 | P2: GET /v1/orders/{id} | Design | Pending |
| MP-06 | P2: Mapeamento de status | Design | Pending |
| MP-07 | P3: Validação HMAC | Design | Pending |
| MP-08 | Edge: Timeout e retry | Design | Pending |

**Coverage:** 8 total, 0 mapped to tasks, 8 unmapped ⚠️

---

## Success Criteria

- [ ] Adapter implementa `PaymentGatewayPort` sem imports de Mercado Pago no domain/application
- [ ] Payload de criação conforme documentação oficial Orders API PIX online
- [ ] Contract test com mock server valida request/response shapes
- [ ] Credenciais nunca commitadas — apenas `.env.example` com placeholders
