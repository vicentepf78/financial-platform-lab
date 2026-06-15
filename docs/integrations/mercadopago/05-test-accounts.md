# Contas de teste — Mercado Pago

Sandbox para validar cobranças PIX via **Orders API** (`type: online`).

## Aplicações (MCP)

| Nome | App ID | User ID vendedor |
|------|--------|------------------|
| `POC-FINANCIAL-PLATAFORM-LAB` | `3292380386767339` | A confirmar no painel |
| `POC-FINANCIAL-PLATAFORM-LAB-LEGACY` | `1509789861543837` | `3476216746` |

Use credenciais de **teste** da aplicação escolhida. O número `5460083726403790` presente no Access Token é identificador da conta/credencial — **não** é o App ID.

## Usuário de teste (comprador)

Dados em `.env.local` — não commitar senhas.

```env
MERCADO_PAGO_TEST_BUYER_USER=TESTUSER...
MERCADO_PAGO_TEST_BUYER_PASSWORD=<senha>
MERCADO_PAGO_TEST_BUYER_EMAIL=<email>@testuser.com
```

Usar `payer.email` de usuário de teste MP ao criar ordens em sandbox.

## Simular pagamento PIX

1. `POST /api/v1/charges` → obter QR Code
2. Pagar via `ticket_url`, app MP sandbox ou simulação no painel
3. Webhook `order` dispara → backend confirma com `GET /v1/orders/{id}`

## Simular webhook sem pagamento

1. Criar cobrança → anotar `mercadoPagoOrderId` (`ORD...`)
2. Painel → Webhooks → Simular → tipo `order` → colar order ID
3. Verificar resposta `200` e idempotência no retry

## Checklist manual (Sprint 3)

- [ ] Ordem criada via `POST /v1/orders` com `type: online`, `pix`
- [ ] **Não** usar `POST /v1/payments`
- [ ] Webhook configurado com topic **Order**, não Pagamentos
- [ ] QR Code renderiza no frontend
- [ ] `external_reference` = `chargeId` interno
- [ ] HMAC validado (`data.id` lowercase no manifest)
- [ ] `GET /v1/orders/{id}` confirma `processed` antes de liquidar
- [ ] Ledger: débito + crédito
- [ ] `ChargePaid` no Kafka
- [ ] Reenvio do webhook não duplica liquidação

## Referências

- [Usuários de teste](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/additional-content/your-integrations/test/accounts)
- [Teste integração PIX](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/integration-test/pix)
- [Notificações](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/notifications)
