# ADR-0007: Integração de Cobranças via Mercado Pago Orders API

**Status:** Accepted  
**Data:** 2026-06-15  
**Decisores:** Equipe Financial Platform Lab  
**Referências:** [STATE.md AD-004](../.specs/project/STATE.md), [docs/integrations/mercadopago/](../docs/integrations/mercadopago/)

## Contexto

A Sprint 3 (Cobranças) exige integração com Mercado Pago para gerar cobranças PIX com QR Code e receber notificações de pagamento. O Mercado Pago oferece duas famílias de API:

| API | Status | Endpoint de criação |
|-----|--------|---------------------|
| **Orders API** (Checkout Transparente) | Recomendada para novas integrações | `POST /v1/orders` |
| **Payments API** (legacy) | Manutenção apenas | `POST /v1/payments` |

A documentação oficial do MP recomenda explicitamente Orders para integrações novas. Webhooks, consulta de status e funcionalidades futuras do MP concentram-se na Orders API. A decisão AD-004 em STATE.md e a validação via MCP Mercado Pago confirmam esta escolha.

A POC cobre cobrança PIX **online** via backoffice (`type: "online"`), não QR presencial/PDV (`type: "qr"`).

## Decisão

Integrar cobranças PIX via **Mercado Pago Orders API** (Checkout Transparente), com as seguintes diretrizes:

### API de criação

- Endpoint: `POST https://api.mercadopago.com/v1/orders`
- Parâmetros obrigatórios na POC:
  - `type: "online"` (não confundir com `type: "qr"` de pagamentos presenciais)
  - `processing_mode: "automatic"`
  - `payment_method.id: "pix"`
  - `payment_method.type: "bank_transfer"`
- Consulta de status: `GET /v1/orders/{order_id}` — **obrigatório** após webhook antes de liquidar.
- **Não utilizar** Payments API legacy (`POST /v1/payments`, doc `checkout-api-payments`).

### Headers

| Header | Uso |
|--------|-----|
| `Authorization: Bearer <ACCESS_TOKEN>` | Autenticação |
| `Content-Type: application/json` | Payload |
| `X-Idempotency-Key` | UUID único por cobrança — evita duplicação em retries |

### Webhooks

- Topic configurado no painel MP: **Order (Mercado Pago)** — `type: order`
- **Não utilizar** topics `payment` ou `merchant_order` (API legacy).
- Endpoint interno: `POST /api/v1/webhooks/mercadopago`
- Validação **HMAC-SHA256** via header `X-Signature` e secret do painel.
- Responder `200`/`201` em até ~22 segundos; processamento assíncrono permitido.
- **Nunca liquidar** cobrança apenas pelo `action` do webhook — sempre confirmar via `GET /v1/orders/{id}`.

### Idempotência

| Camada | Chave |
|--------|-------|
| Criação de ordem | `X-Idempotency-Key` (UUID por cobrança) |
| Webhook | `mercadopago:webhook:{order_id}:{action}` |

### Mapeamento na plataforma

- Adapter HTTP em `billing-module/adapters/mercadopago/`.
- Port de saída `MercadoPagoOrderPort` na camada `ports/`.
- `external_reference` = `chargeId` interno (UUID da cobrança na plataforma).
- Persistir: `mercadoPagoOrderId`, `mercadoPagoPaymentId`, `external_reference`.
- Eventos: `ChargeCreated` após QR gerado; `ChargePaid` após GET confirmar `status: processed`.
- Liquidação segue ADR-0006 (ledger-first, débito + crédito).

### Escopo v1

| Incluído | Fora de escopo |
|----------|----------------|
| PIX online (`type: online`) | QR presencial/PDV (`type: qr`) |
| Sandbox / credenciais de teste | Produção |
| Cancelamento de cobranças pendentes | Reembolso |

Documentação técnica detalhada em `docs/integrations/mercadopago/`.

## Consequências

### Positivas

- Alinhamento com recomendação oficial do Mercado Pago para novas integrações.
- Webhooks unificados (topic `order`) simplificam configuração e troubleshooting.
- Idempotência em criação e webhook previne cobranças e liquidações duplicadas.
- Base para funcionalidades futuras do MP concentradas na Orders API.

### Negativas

- Curva de aprendizado da Orders API (payload diferente da Payments API legacy).
- Distinção `type: online` vs `type: qr` exige atenção para evitar QR vazio ou malformado.
- Tunnel HTTPS necessário em desenvolvimento para receber webhooks do sandbox.

### Neutras

- Aplicação recomendada no painel: `POC-FINANCIAL-PLATAFORM-LAB` (App ID `3292380386767339`).
- Credenciais sensíveis em `.env.local`; nunca commitadas.
- Contract tests com sandbox obrigatórios para validar mapeamento de status.
- Feature `create-charge` em `.specs/features/` deve referenciar esta ADR e `docs/integrations/mercadopago/`.
