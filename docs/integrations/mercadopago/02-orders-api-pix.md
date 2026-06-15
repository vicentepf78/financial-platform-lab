# Cobranças PIX — Orders API

Fluxo de cobrança PIX **online** para o Financial Platform Lab usando `POST /v1/orders`.

> **Validado via MCP MP:** endpoint e payload abaixo correspondem à doc oficial de [Integração PIX](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/payment-integration/pix) do Checkout Transparente (Orders).

## PIX online vs QR presencial

Ambos usam `/v1/orders`, mas com payloads diferentes. A POC usa apenas **online**:

| | **POC (usar)** | **Não usar na POC** |
|---|----------------|---------------------|
| `type` | `"online"` | `"qr"` |
| Cenário | Backoffice web — operador gera cobrança | PDV / caixa física / app presencial |
| Config extra | — | `config.qr.mode`, `external_pos_id` |
| Doc MP | [payment-integration/pix](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/payment-integration/pix) | [qr-code](https://www.mercadopago.com.br/developers/pt/docs/qr-code/overview) |

## API legada — não usar

| Endpoint legado | Substituição |
|-----------------|--------------|
| `POST /v1/payments` | `POST /v1/orders` |
| Doc `checkout-api-payments` | Doc `checkout-api-orders` |

O MP classifica `checkout-api-payments` como **legacy** e recomenda Orders para integrações novas.

## Modelo de processamento

| Parâmetro | Valor | Motivo |
|-----------|-------|--------|
| `processing_mode` | `automatic` | Cria e processa em um passo (modo padrão MP) |
| `type` | `online` | Pagamento online — backoffice |
| `payment_method.id` | `pix` | Método PIX |
| `payment_method.type` | `bank_transfer` | Obrigatório para PIX na Orders API |

Modo `manual` fica fora do escopo da v1 (`client_token` + processamento em etapas).

## Fluxo end-to-end

```text
Operador (UI)          Backend                    Mercado Pago
     │                    │                            │
     │ POST /charges      │ POST /v1/orders            │
     │───────────────────►│ (type: online, pix)        │
     │                    │───────────────────────────►│
     │                    │◄───────────────────────────│
     │                    │  ORD..., qr_code,          │
     │                    │  qr_code_base64            │
     │◄───────────────────│                            │
     │  exibe QR Code     │                            │
     │                    │◄──── webhook (order) ──────│
     │                    │ GET /v1/orders/{id}        │
     │                    │───────────────────────────►│
     │                    │ status: processed          │
     │                    │──► ChargePaid + ledger       │
```

## Criar cobrança PIX

**Endpoint:** `POST https://api.mercadopago.com/v1/orders`

**Headers obrigatórios:**

| Header | Valor |
|--------|-------|
| `Authorization` | `Bearer <ACCESS_TOKEN>` |
| `Content-Type` | `application/json` |
| `X-Idempotency-Key` | UUID único por cobrança |

**Body (validado MCP):**

```json
{
  "type": "online",
  "total_amount": "100.00",
  "external_reference": "charge_<uuid-da-cobranca-interna>",
  "processing_mode": "automatic",
  "transactions": {
    "payments": [
      {
        "amount": "100.00",
        "payment_method": {
          "id": "pix",
          "type": "bank_transfer"
        },
        "expiration_time": "P1D"
      }
    ]
  },
  "payer": {
    "email": "test_user_br@testuser.com"
  }
}
```

### Campos relevantes

| Campo | Obrigatório | Notas |
|-------|-------------|-------|
| `total_amount` | Sim | String com 2 casas decimais |
| `external_reference` | Sim | `chargeId` interno — reconciliação |
| `payer.email` | Sim | Em sandbox, e-mail de usuário de teste MP |
| `expiration_time` | Não | ISO 8601 duration; default 24h; min 30min, max 30 dias |

### Resposta (aguardando pagamento)

```json
{
  "id": "ORD01HRYFWNYRE1MR1E60MW3X0T2P",
  "status": "action_required",
  "status_detail": "waiting_transfer",
  "external_reference": "charge_<uuid>",
  "transactions": {
    "payments": [
      {
        "id": "PAY01HRYFXQ53Q3JPEC48MYWMR0TE",
        "status": "action_required",
        "status_detail": "waiting_transfer",
        "payment_method": {
          "id": "pix",
          "ticket_url": "https://www.mercadopago.com.br/sandbox/payments/.../ticket?...",
          "qr_code": "00020126580014br.gov.bcb.pix...",
          "qr_code_base64": "iVBORw0KGgoAAAANSUhEUgAA..."
        }
      }
    ]
  }
}
```

### Dados para o frontend

| Campo MP | Uso na UI |
|----------|-----------|
| `qr_code_base64` | `<img src="data:image/png;base64,...">` |
| `qr_code` | Pix Copia e Cola |
| `ticket_url` | Link com instruções (sandbox: remover `/sandbox` da URL se necessário) |

Persistir: `mercadoPagoOrderId`, `mercadoPagoPaymentId`, `external_reference` (= `chargeId`).

## Consultar status

**Endpoint:** `GET https://api.mercadopago.com/v1/orders/{order_id}`

Obrigatório após webhook antes de liquidar. Polling é apenas fallback em dev.

### Mapeamento de status (POC)

| Status MP (order) | Status domínio `Charge` | Ação |
|-------------------|-------------------------|------|
| `action_required` + `waiting_transfer` | `PENDING` | Aguardar pagamento |
| `processed` + `accredited` | `PAID` | `ChargePaid` + ledger |
| `canceled` / `expired` | `CANCELED` / `EXPIRED` | Encerrar cobrança |
| `refunded` | `REFUNDED` | Fora do escopo v1 |

Validar valores exatos em contract tests com sandbox.

## Cancelamento e reembolso

| Operação | Endpoint Orders API |
|----------|---------------------|
| Cancelar pendente | `POST /v1/orders/{id}/cancel` |
| Reembolsar pago | `POST /v1/orders/{id}/refund` |

Cancelamento v1: apenas cobranças `PENDING`. Reembolso: fora do escopo v1.

## Regras de domínio

1. **Ledger-first** — liquidação gera débito + crédito; nunca `account.setBalance(...)`.
2. **Idempotência** — `X-Idempotency-Key` na criação; `order_id + action` no webhook.
3. **Auditoria** — `correlationId`, `mercadoPagoOrderId`, operador, timestamp.
4. **Eventos** — `ChargeCreated` após QR gerado; `ChargePaid` após GET confirmar pagamento.

## Erros comuns

| Situação | Causa provável |
|----------|----------------|
| 401 | Access Token inválido ou ambiente errado (teste vs produção) |
| 400 PIX | Chave PIX não cadastrada na conta vendedor |
| QR vazio | `type` incorreto (`qr` em vez de `online`) ou `payment_method` malformado |
| Webhook sem liquidação | Topic `payment` configurado em vez de `order` |
| Pagamento não confirma | Tunnel offline ou webhook não retorna 200 em 22s |

## Referências

- [Modelo de integração](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/integration-model)
- [Integração PIX](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/payment-integration/pix)
- [Teste de integração PIX](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/integration-test/pix)
- [Create Order](https://www.mercadopago.com.br/developers/pt/reference/online-payments/checkout-api/create-order/post)
- [Get Order](https://www.mercadopago.com.br/developers/pt/reference/online-payments/checkout-api/get-order/get)
