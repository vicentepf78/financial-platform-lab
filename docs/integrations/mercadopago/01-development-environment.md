# Ambiente de desenvolvimento — Mercado Pago

Configuração do ambiente local para integrar cobranças PIX no backoffice web (React) + backend (Spring Boot).

## Visão geral

```text
┌─────────────────┐     Public Key          ┌──────────────────┐
│  Frontend       │ ──────────────────────► │  MercadoPago.js  │
│  (React)        │   (somente client-side) │  (SDK v2)        │
└────────┬────────┘                         └──────────────────┘
         │ POST /api/v1/charges
         ▼
┌─────────────────┐     Access Token        ┌──────────────────┐
│  Backend        │ ──────────────────────► │  Orders API        │
│  billing-module │   X-Idempotency-Key     │  api.mercadopago   │
└────────┬────────┘                         └──────────────────┘
         │ HTTPS POST (webhook)
         ▼
┌─────────────────┐
│  /webhooks/     │ ◄── ngrok / tunnel (dev)
│  mercadopago    │
└─────────────────┘
```

| Credencial | Onde usar | Expor no frontend? |
|------------|-----------|-------------------|
| **Public Key** (teste) | Inicializar `MercadoPago.js` | Sim |
| **Access Token** (teste) | Backend — chamadas à Orders API | **Nunca** |
| **Webhook Secret** | Backend — validar `x-signature` | **Nunca** |

## 1. Credenciais no painel MP

1. Acesse [Suas integrações](https://www.mercadopago.com.br/developers/panel/app).
2. Selecione a aplicação da POC (ver tabela abaixo).
3. Em **Credenciais > Teste**, copie **Public Key** (frontend) e **Access Token** (backend).

### Aplicações disponíveis (MCP)

| Nome | App ID | Recomendação |
|------|--------|--------------|
| `POC-FINANCIAL-PLATAFORM-LAB` | `3292380386767339` | Usar nesta POC |
| `POC-FINANCIAL-PLATAFORM-LAB-LEGACY` | `1509789861543837` | Alternativa (credenciais já existentes) |

> O nome **LEGACY** é rótulo da aplicação — **não** indica uso da Payments API (`/v1/payments`).

> **Segurança:** não commite tokens. Use `.env.local` (gitignored).

## 2. Variáveis de ambiente

Copie o template na raiz do projeto:

```bash
cp .env.example .env.local
```

Preencha os valores de teste no `.env.local`. O backend e o frontend leem essas variáveis via Docker Compose ou export manual.

| Variável | Componente | Descrição |
|----------|------------|-----------|
| `MERCADO_PAGO_PUBLIC_KEY` | Frontend | Chave pública de teste |
| `MERCADO_PAGO_ACCESS_TOKEN` | Backend | Token privado de teste |
| `MERCADO_PAGO_APPLICATION_ID` | Backend | App ID do painel (ex.: `3292380386767339`) |
| `MERCADO_PAGO_WEBHOOK_SECRET` | Backend | Secret gerado em Webhooks > Configurar notificações |
| `MERCADO_PAGO_API_BASE_URL` | Backend | Default: `https://api.mercadopago.com` |
| `MERCADO_PAGO_WEBHOOK_URL` | Config MP | URL pública do webhook (tunnel em dev) |

## 3. Frontend — MercadoPago.js

A POC usa **website** (não mobile). O SDK client-side é necessário se futuramente houver captura de dados do pagador no browser; para PIX puro via backend, o SDK é opcional na v1, mas recomendamos instalá-lo desde já.

### Instalação

```bash
cd frontend
npm install @mercadopago/sdk-js
```

### Inicialização

```typescript
import { loadMercadoPago } from "@mercadopago/sdk-js";

await loadMercadoPago();
const mp = new window.MercadoPago(import.meta.env.VITE_MERCADO_PAGO_PUBLIC_KEY);
```

Adicione ao `frontend/.env.local`:

```env
VITE_MERCADO_PAGO_PUBLIC_KEY=<sua-public-key-de-teste>
```

Alternativa via CDN (não recomendada para o projeto React):

```html
<script src="https://sdk.mercadopago.com/js/v2"></script>
<script>
  const mp = new MercadoPago("YOUR_PUBLIC_KEY");
</script>
```

## 4. Backend — configuração Spring

Quando o adapter for implementado, as propriedades seguirão este padrão em `application.yml`:

```yaml
mercadopago:
  access-token: ${MERCADO_PAGO_ACCESS_TOKEN}
  webhook-secret: ${MERCADO_PAGO_WEBHOOK_SECRET}
  api-base-url: ${MERCADO_PAGO_API_BASE_URL:https://api.mercadopago.com}
```

O **Access Token** vai no header `Authorization: Bearer <token>` em todas as chamadas server-side.

## 5. Chave PIX no Mercado Pago

Para receber PIX via MP, a conta vendedor precisa ter **chaves PIX cadastradas** no Mercado Pago.

Verifique em: conta MP → área Pix → Minhas chaves.

Sem chave PIX registrada, a criação de ordem PIX pode falhar.

## 6. Webhook em desenvolvimento local

O Mercado Pago **não aceita** `localhost` como URL de notificação.

Para testar webhooks localmente:

1. Suba o backend (`localhost:8080`).
2. Exponha com tunnel HTTPS, por exemplo:
   - [ngrok](https://ngrok.com/): `ngrok http 8080`
   - [Cloudflare Tunnel](https://developers.cloudflare.com/cloudflare-one/connections/connect-apps/)
3. Configure no painel MP → Webhooks → URL de **modo teste**:
   - `https://<seu-tunnel>/api/v1/webhooks/mercadopago`
4. Selecione o evento **Order (Mercado Pago)** — **não** use "Pagamentos" (`payment`).
5. Salve e copie o **secret** gerado para `MERCADO_PAGO_WEBHOOK_SECRET`.

## 7. Checklist antes da Sprint 3

- [ ] Aplicação criada no painel MP
- [ ] Public Key e Access Token de **teste** em `.env.local`
- [ ] Chave PIX cadastrada na conta vendedor
- [ ] Tunnel HTTPS configurado para webhooks (dev)
- [ ] Evento `order` habilitado no painel
- [ ] Webhook secret salvo em variável de ambiente
- [ ] Simulação de notificação executada no painel MP

## Referências

- [Configurar ambiente de desenvolvimento (MP)](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/development-environment)
- [Credenciais](https://www.mercadopago.com.br/developers/pt/docs/checkout-api-orders/resources/credentials)
