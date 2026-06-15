# Mapeamento na plataforma

Como a integração Mercado Pago se encaixa na arquitetura hexagonal do `billing-module`.

## Bounded context

**Módulo:** `backend/billing-module`  
**Sprint:** 3 — Cobranças  
**Integração externa:** Mercado Pago Orders API (PIX)

## Estrutura de pacotes (planejada)

```text
billing-module/
├── domain/
│   ├── Charge.java                 # Aggregate
│   ├── ChargeStatus.java           # PENDING, PAID, CANCELED, EXPIRED
│   └── events/
│       ├── ChargeCreated.java
│       └── ChargePaid.java
├── application/
│   ├── CreateChargeUseCase.java
│   ├── GetChargeUseCase.java
│   ├── ListChargesUseCase.java
│   └── ProcessMercadoPagoWebhookUseCase.java
├── ports/
│   ├── PaymentGatewayPort.java     # Abstração do gateway
│   ├── ChargeRepositoryPort.java
│   └── WebhookEventRepositoryPort.java
├── adapters/
│   ├── mercadopago/
│   │   ├── MercadoPagoPaymentGatewayAdapter.java
│   │   ├── MercadoPagoOrderClient.java
│   │   ├── MercadoPagoWebhookSignatureValidator.java
│   │   └── dto/                    # Request/response MP (não vazar para domain)
│   ├── persistence/
│   │   └── JpaChargeRepositoryAdapter.java
│   └── webhook/
│       └── MercadoPagoWebhookController.java
└── features/
    ├── create-charge/
    ├── get-charge/
    ├── list-charges/
    └── mercadopago-webhook/
```

## Port — PaymentGatewayPort

Interface no domínio da aplicação (não conhece Mercado Pago):

```java
public interface PaymentGatewayPort {
    PaymentOrderResult createPixCharge(CreatePixChargeCommand command);
    PaymentOrderStatus getOrderStatus(String externalOrderId);
}
```

O adapter `MercadoPagoPaymentGatewayAdapter` traduz comandos internos para `POST /v1/orders` e `GET /v1/orders/{id}`.

## Fluxo CreateCharge

```text
CreateChargeController
        │
        ▼
CreateChargeUseCase
        │
        ├─► Charge.create(...)           # domain
        ├─► PaymentGatewayPort.createPixCharge(...)
        ├─► ChargeRepositoryPort.save(...)
        └─► EventPublisher.publish(ChargeCreated)
```

**Regra:** o controller não chama a API do MP diretamente.

## Fluxo Webhook → Liquidação

```text
MercadoPagoWebhookController          # apenas HTTP + delegação
        │
        ▼
ProcessMercadoPagoWebhookUseCase
        │
        ├─► MercadoPagoWebhookSignatureValidator
        ├─► WebhookEventRepositoryPort (idempotência)
        ├─► PaymentGatewayPort.getOrderStatus(orderId)
        ├─► MarkChargeAsPaidUseCase (se PAID)
        │       ├─► LedgerPort.createEntries(...)   # débito + crédito
        │       └─► EventPublisher.publish(ChargePaid)
        └─► ChargeRepositoryPort.updateStatus(...)
```

**Regra:** liquidação financeira ocorre no use case, após confirmação via GET na ordem.

## API REST interna (backoffice)

| Método | Path | Feature |
|--------|------|---------|
| `POST` | `/api/v1/charges` | create-charge |
| `GET` | `/api/v1/charges/{id}` | get-charge |
| `GET` | `/api/v1/charges` | list-charges |
| `POST` | `/api/v1/webhooks/mercadopago` | mercadopago-webhook |

### POST /api/v1/charges (request)

```json
{
  "accountId": "uuid",
  "amount": "150.00",
  "description": "Mensalidade junho",
  "payerEmail": "cliente@example.com"
}
```

### POST /api/v1/charges (response)

```json
{
  "data": {
    "id": "uuid-interno",
    "status": "PENDING",
    "amount": "150.00",
    "mercadoPagoOrderId": "ORD01...",
    "pix": {
      "qrCode": "00020126...",
      "qrCodeBase64": "iVBORw0...",
      "ticketUrl": "https://..."
    }
  },
  "metadata": {
    "correlationId": "uuid"
  }
}
```

## Eventos Kafka

| Topic | Evento | Quando |
|-------|--------|--------|
| `charge-created` | `ChargeCreated` | Cobrança persistida com QR gerado |
| `charge-paid` | `ChargePaid` | Pagamento confirmado + ledger atualizado |

Consumers: `audit-module`, `ledger-module` (se aplicável).

## Persistência (campos sugeridos)

Tabela `charges`:

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | UUID | PK interna |
| `account_id` | UUID | FK conta cobrada |
| `amount` | NUMERIC(19,2) | Valor |
| `status` | VARCHAR | PENDING, PAID, ... |
| `external_reference` | VARCHAR | Enviado ao MP |
| `mercado_pago_order_id` | VARCHAR | ID ordem MP |
| `mercado_pago_payment_id` | VARCHAR | ID pagamento MP |
| `pix_qr_code` | TEXT | Copia e cola |
| `pix_qr_code_base64` | TEXT | Imagem QR |
| `idempotency_key` | VARCHAR | UNIQUE — criação |
| `correlation_id` | UUID | Rastreabilidade |
| `created_at` | TIMESTAMPTZ | Auditoria |
| `paid_at` | TIMESTAMPTZ | Liquidação |

Tabela `billing_webhook_events`:

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | UUID | PK |
| `idempotency_key` | VARCHAR | UNIQUE — `order_id:action` |
| `payload_hash` | VARCHAR | SHA-256 do body |
| `processed_at` | TIMESTAMPTZ | Quando processado |

## Frontend (Sprint 3 UI)

| Tela | Componentes |
|------|-------------|
| Listagem de cobranças | Tabela com status, valor, data |
| Detalhe / QR | `<img>` com base64, botão copiar `qr_code` |
| Criar cobrança | Form → `POST /api/v1/charges` |

O frontend **não** chama `api.mercadopago.com` para criar cobranças — apenas exibe dados retornados pelo backend.

## Testes

| Tipo | Escopo |
|------|--------|
| Unit | `CreateChargeUseCase`, `ProcessMercadoPagoWebhookUseCase`, validador HMAC |
| Integration | Adapter com WireMock simulando MP |
| Contract | Gravação de requests/responses reais do sandbox MP |

Contract tests são obrigatórios por `AGENTS.md` para integrações externas.

## Decisão de API (validada MCP jun/2026)

O MP recomenda Orders API para novas integrações. Referência: [Modelo de integração](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/integration-model).

| Opção | Decisão | Motivo |
|-------|---------|--------|
| Orders API — `type: online` | **Usar** | PIX backoffice; doc atual do Checkout Transparente |
| Orders API — `type: qr` | **Não usar** | QR presencial / PDV — fora do escopo |
| Payments API `/v1/payments` | **Não usar** | Legacy (`checkout-api-payments`) |
| Webhook topic `payment` | **Não usar** | Incompatível com Orders |
| Webhook topic `order` | **Usar** | Único compatível com Orders API |
| Checkout Pro / Bricks | Fora v1 | Escopo POC |

Formalizar em ADR quando `adr/` for populada.
