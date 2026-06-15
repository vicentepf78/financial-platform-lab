# Integração Mercado Pago

Documentação de referência para a Sprint 3 (Cobranças) do Financial Platform Lab.

**Validação:** decisões desta pasta foram confirmadas via **MCP Mercado Pago** (`search_documentation`, site MLB, jun/2026).

## Escopo da POC

| Incluído (v1) | Fora de escopo (v1) |
|---------------|---------------------|
| Cobrança PIX online via QR Code (`type: online`) | QR presencial / PDV (`type: qr`, `config.qr.mode`) |
| Checkout Transparente + **Orders API** | Payments API legacy (`/v1/payments`) |
| Webhooks topic `order` (**Order Mercado Pago**) | Webhooks topic `payment` ou `merchant_order` |
| Sandbox / credenciais de teste | Produção |
| Idempotência (`X-Idempotency-Key` + webhook) | Checkout Pro, Bricks, cartão, boleto |

## Decisão de API (confirmada pelo MP)

O Mercado Pago recomenda explicitamente para **novas integrações**:

> *"Checkout Transparente agora processa pagamentos com **Orders**. Se você está começando uma nova integração, recomendamos utilizar a [API de Orders](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/integration-model)."*

| | **Orders API (usar)** | **Payments API (não usar)** |
|---|----------------------|----------------------------|
| Documentação | `checkout-api-orders` | `checkout-api-payments` (**legacy**) |
| Criar PIX | `POST /v1/orders` | `POST /v1/payments` |
| Consultar | `GET /v1/orders/{id}` | `GET /v1/payments/{id}` |
| Webhook no painel | **Order (Mercado Pago)** | Pagamentos (`payment`) |
| `notification_url` no body | Não existe — configurar no painel | Suportado (legacy) |
| Novas funcionalidades MP | Sim — foco futuro do MP | Manutenção apenas |

**Endpoint único da POC:** `POST /v1/orders` com `type: "online"` (não confundir com `type: "qr"` de pagamentos presenciais).

## Documentos

| Documento | Conteúdo |
|-----------|----------|
| [Ambiente de desenvolvimento](01-development-environment.md) | Credenciais, SDK, variáveis, aplicações |
| [Cobranças PIX (Orders API)](02-orders-api-pix.md) | Payload online, status, QR Code |
| [Webhooks](03-webhooks.md) | Topic `order`, HMAC, idempotência |
| [Mapeamento na plataforma](04-platform-mapping.md) | Ports, adapters, eventos |
| [Contas de teste](05-test-accounts.md) | Sandbox, checklist |

## Aplicações no painel (MCP)

Duas aplicações registradas na conta:

| Nome | App ID (painel) | Uso na POC |
|------|-----------------|------------|
| `POC-FINANCIAL-PLATAFORM-LAB` | `3292380386767339` | **Recomendada** — nome alinhado ao projeto |
| `POC-FINANCIAL-PLATAFORM-LAB-LEGACY` | `1509789861543837` | Alternativa — credenciais já geradas |

> O sufixo **LEGACY** no nome da aplicação **não** indica uso da Payments API. É apenas o rótulo da app no painel.

### Identificadores (não confundir)

| Campo | Exemplo | Onde aparece |
|-------|---------|--------------|
| **App ID** | `3292380386767339` | Painel → Suas integrações; campo `application_id` no webhook |
| **User ID (vendedor)** | `3476216746` | Webhook `user_id`; conta MP |
| **Número no Access Token** | `5460083726403790` | Formato `APP_USR-{numero}-...` — identificador da conta/credencial, **não** é o App ID |

Credenciais sensíveis ficam em `.env.local`. Ver [`.env.example`](../../../.env.example).

## Referências oficiais

- [Modelo de integração (Orders vs Payments)](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/integration-model)
- [Checkout API Orders — Overview](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/overview)
- [Integração PIX online](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/payment-integration/pix)
- [Notificações Orders](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/notifications)
- [Create Order — API Reference](https://www.mercadopago.com.br/developers/pt/reference/online-payments/checkout-api/create-order/post)

## Próximo passo (spec-driven)

A feature `create-charge` em `.specs/features/` deve referenciar esta pasta como requisitos técnicos externos.
