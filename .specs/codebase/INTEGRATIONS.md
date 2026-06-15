# External Integrations

**Status:** Pré-implementação — integrações planejadas em `PROJECT.md`; nenhum adapter implementado.

## Payment Gateway

**Service:** Mercado Pago — Checkout Transparente (**Orders API**)
**Purpose:** Cobranças PIX online com QR Code, webhooks de ordem
**Phase:** Sprint 3
**Implementation (planejada):** `backend/billing-module/adapters/mercadopago/`
**Documentation:** [`docs/integrations/mercadopago/README.md`](../../docs/integrations/mercadopago/README.md)
**Validação:** MCP Mercado Pago (`search_documentation`, MLB, jun/2026)

### Decisão de API

| Usar | Não usar |
|------|----------|
| `POST /v1/orders` (`type: online`, PIX) | `POST /v1/payments` (Payments API **legacy**) |
| `GET /v1/orders/{id}` | `GET /v1/payments/{id}` |
| Webhook topic **`order`** | Webhook topic `payment` ou `merchant_order` |
| Doc `checkout-api-orders` | Doc `checkout-api-payments` |

MP: *"Se você está começando uma nova integração, recomendamos utilizar a API de Orders."*

### Aplicações sandbox (MCP)

| Nome | App ID |
|------|--------|
| `POC-FINANCIAL-PLATAFORM-LAB` (recomendada) | `3292380386767339` |
| `POC-FINANCIAL-PLATAFORM-LAB-LEGACY` | `1509789861543837` |

### Configuration

Ver [`.env.example`](../../.env.example)

| Variável | Componente |
|----------|------------|
| `MERCADO_PAGO_ACCESS_TOKEN` | Backend |
| `MERCADO_PAGO_WEBHOOK_SECRET` | Backend |
| `VITE_MERCADO_PAGO_PUBLIC_KEY` | Frontend (SDK opcional v1) |

### Capabilities (Orders API)

| Capability | Endpoint | Uso |
|------------|----------|-----|
| Criar cobrança PIX | `POST /v1/orders` | `processing_mode: automatic`, `payment_method.id: pix` |
| Consultar ordem | `GET /v1/orders/{id}` | Confirmar status **antes** de liquidar |
| Cancelar ordem | `POST /v1/orders/{id}/cancel` | Cobrança pendente |
| Webhook interno | `POST /api/v1/webhooks/mercadopago` | Recebe topic `order` do MP |

**Idempotency:**

- Criação: header `X-Idempotency-Key`
- Webhook: `order_id + action` em `billing_webhook_events`

**Fora do escopo v1:** `type: qr` (PDV), reembolso, cartão, boleto, Checkout Pro.

## Banking / PIX

**Service:** Banco do Brasil
**Purpose:** PIX real, cobranças PIX, webhooks
**Phase:** Fase 3 (pós-POC)
**Implementation (planejada):** `backend/pix-module/adapters/bancodobrasil/`
**Configuration:** Certificados mTLS, client_id, client_secret
**Authentication:** OAuth2 + certificado digital

**Nota:** Sprint 4 implementa PIX interno (simulado/domínio) com interface pronta para adapter BB.

## Billing / CNAB

**Service:** Kobana
**Purpose:** Boletos, CNAB, retornos bancários
**Phase:** Fase 2 (pós-Sprint 5 parcial)
**Implementation (planejada):** `backend/reconciliation-module/adapters/kobana/`
**Configuration:** API key Kobana
**Authentication:** Bearer token

## Messaging

**Service:** Apache Kafka
**Purpose:** Domain events entre módulos
**Implementation (planejada):** `backend/*/adapters/messaging/`
**Configuration:** `KAFKA_BOOTSTRAP_SERVERS` via Docker Compose
**Topics (planejados):**

| Topic | Evento | Produtor | Consumidor |
|-------|--------|----------|------------|
| account-created | AccountCreated | account-module | audit-module |
| transfer-executed | TransferExecuted | account-module | ledger-module, audit-module |
| ledger-entry-created | LedgerEntryCreated | ledger-module | audit-module |
| pix-sent | PixSent | pix-module | audit-module |
| pix-received | PixReceived | pix-module | ledger-module, audit-module |
| charge-created | ChargeCreated | billing-module | audit-module |
| charge-paid | ChargePaid | billing-module | ledger-module, audit-module |
| reconciliation-executed | ReconciliationExecuted | reconciliation-module | audit-module |

**Serialization:** JSON (proibido Java serialization)

## Database

**Service:** PostgreSQL
**Purpose:** Persistência transacional
**Implementation:** Spring Data JPA + Flyway migrations
**Configuration:** `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`
**Migrations:** `backend/application/src/main/resources/db/migration/`

## Observability

**Service:** OpenTelemetry → Prometheus + Grafana + Loki
**Purpose:** Métricas, traces e logs
**Implementation:** Spring Actuator + OTel SDK
**Configuration:** `OTEL_EXPORTER_OTLP_ENDPOINT`
**Metrics obrigatórias:** throughput, latência, erros, retries

## API Integrations (Internal)

### REST API (Backoffice → Backend)

**Purpose:** Operações do backoffice financeiro
**Location:** `backend/*/features/*/*Controller.java`
**Authentication:** Spring Security (JWT ou session — a definir no ADR de segurança)
**Response format:**

```json
{
  "data": {},
  "metadata": {}
}
```

**Error format:** RFC 9457 Problem Details

## Webhooks

### Mercado Pago Webhook

**Purpose:** Notificação de mudança de status de ordem (cobrança PIX)
**Location (planejada):** `backend/billing-module/adapters/webhook/MercadoPagoWebhookController.java`
**Topic:** `order` — evento **Order (Mercado Pago)** no painel
**Actions:** `order.action_required`, `order.processed`, `order.canceled`, `order.expired`
**Security:** HMAC-SHA256 (`x-signature`); `data.id` em lowercase no manifest
**Processing:** `GET /v1/orders/{id}` antes de liquidar; idempotência por `order_id + action`

### Banco do Brasil Webhook (futuro)

**Purpose:** Notificação de PIX recebido/enviado
**Location (planejada):** `backend/pix-module/adapters/webhook/`
**Events:** A definir na integração Fase 3

## Background Jobs

**Queue system:** Kafka (event-driven; sem job queue separado na v1)
**Location:** Consumers em `backend/*/adapters/messaging/`
**Jobs planejados:**

| Consumer | Responsabilidade |
|----------|------------------|
| AuditEventConsumer | Registrar eventos para auditoria |
| LedgerEventConsumer | Processar eventos que geram lançamentos |
| ReconciliationProcessor | Reprocessar divergências (trigger manual ou evento) |

**Requirements:** Consumers idempotentes e retryable com dead-letter topic
