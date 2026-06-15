# Webhooks — Mercado Pago Orders

Configuração e processamento de notificações para cobranças PIX via **Orders API**.

> **Validado via MCP MP:** topic `order` é o correto para Checkout Transparente (Orders). Topics `payment` e `merchant_order` são da API **legacy** e **não são compatíveis** com Orders.

## Configuração no painel

1. [Suas integrações](https://www.mercadopago.com.br/developers/panel/app) → app da POC
2. **Webhooks > Configurar notificações**
3. URL de **modo teste** (HTTPS via tunnel em dev):
   - `https://<tunnel>/api/v1/webhooks/mercadopago`
4. Evento: **Order (Mercado Pago)** — topic `order`
5. Salvar → copiar **secret signature** → `MERCADO_PAGO_WEBHOOK_SECRET`

### Topics — compatibilidade

| Topic no painel | API | Usar na POC? |
|-----------------|-----|--------------|
| **Order (Mercado Pago)** | Orders API | **Sim** |
| Pagamentos (`payment`) | Payments API legacy | **Não** |
| Ordens comerciais (`merchant_order`) | Checkout Pro / QR legacy | **Não** |

Na Orders API o campo `notification_url` no body da requisição **não existe**. Notificações são configuradas exclusivamente no painel (ou via MCP `save_webhook`).

## Formato da notificação

```http
POST /api/v1/webhooks/mercadopago?data.id=ORD01JQ4S4KY8HWQ6NA5PXB65B3D3&type=order
Content-Type: application/json
X-Request-Id: 2066ca19-c6f1-498a-be75-1923005edd06
X-Signature: ts=1742505638683,v1=ced36ab6d33566bb1e16c125819b8d840d6b8ef136b0b9127c76064466f5229b

{
  "action": "order.processed",
  "api_version": "v1",
  "application_id": "3292380386767339",
  "date_created": "2021-11-01T02:02:02Z",
  "id": "123456",
  "live_mode": false,
  "type": "order",
  "user_id": 3476216746,
  "data": {
    "id": "ORD01JQ4S4KY8HWQ6NA5PXB65B3D3"
  }
}
```

> `application_id` deve corresponder ao App ID da aplicação usada (ex.: `3292380386767339` para `POC-FINANCIAL-PLATAFORM-LAB`).

### Query params

| Param | Uso |
|-------|-----|
| `data.id` | ID da ordem — validação HMAC (**lowercase** no manifest) |
| `type` | Sempre `order` |

### Actions relevantes

| `action` | Interpretação |
|----------|---------------|
| `order.action_required` | Aguardando pagamento PIX |
| `order.processed` | Processada — confirmar via GET |
| `order.canceled` | Cancelada |
| `order.expired` | Expirada |
| `order.refunded` | Estornada (fora v1) |

**Regra:** nunca liquidar só pelo `action`. Sempre `GET /v1/orders/{data.id}` antes de marcar `PAID`.

## Validação HMAC-SHA256

Header `x-signature`: `ts=<timestamp>,v1=<hash>`

Manifest (atenção ao **lowercase** de `data.id`):

```text
id:<data.id em minúsculas>;request-id:<x-request-id>;ts:<ts>;
```

Comparar `HMAC-SHA256(manifest, webhook_secret)` com `v1` (timing-safe).

Falha na validação → `401` sem processar.

## Resposta HTTP

| Requisito | Valor |
|-----------|-------|
| Status | `200` ou `201` |
| Timeout MP | ~22 segundos |
| Retry | A cada 15 min se não receber 200/201 |

Processar de forma assíncrona se necessário, mas responder 200 rapidamente.

## Fluxo de processamento

```text
Webhook POST
    ├─► Validar HMAC
    ├─► Responder 200
    └─► ProcessMercadoPagoWebhookUseCase
            ├─► Idempotência (order_id + action)
            ├─► GET /v1/orders/{data.id}
            ├─► Se processed → MarkChargeAsPaidUseCase → ledger + ChargePaid
            └─► Senão → UpdateChargeStatusUseCase
```

Chave de idempotência: `mercadopago:webhook:{order_id}:{action}`

## Simulação

Painel → Webhooks → **Simular** → tipo `order` → informar `order_id` real do sandbox.

## Endpoint interno (planejado)

| Item | Valor |
|------|-------|
| `POST` | `/api/v1/webhooks/mercadopago` |
| Auth | HMAC (sem JWT) |
| Módulo | `billing-module/adapters/webhook/` |

## Referências

- [Notificações Orders](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/notifications)
- [Webhooks gerais](https://www.mercadopago.com.br/developers/pt/docs/your-integrations/notifications/webhooks)
