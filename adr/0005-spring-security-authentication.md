# ADR-0005: Autenticação e Segurança com Spring Security

**Status:** Accepted  
**Data:** 2026-06-15  
**Decisores:** Equipe Financial Platform Lab  
**Referências:** [AGENTS.md](../AGENTS.md), [PROJECT.md](../.specs/project/PROJECT.md)

## Contexto

O backoffice financeiro expõe APIs REST consumidas por uma SPA React. Operações financeiras (transferências, cobranças, conciliação) exigem autenticação, autorização e trilha de auditoria rastreável. Erros da API devem seguir padrão interoperável para consumo pelo frontend.

A arquitetura é stateless no backend (monólito modular sem sessão server-side compartilhada entre instâncias). Webhooks externos (Mercado Pago) usam autenticação própria (HMAC), separada da API autenticada por usuário.

## Decisão

Adotar **Spring Security** para proteção da API REST, com as seguintes diretrizes:

### Autenticação

- **JWT (JSON Web Token)** como mecanismo de autenticação para API stateless + SPA.
- Fluxo: login via `POST /api/v1/auth/login` → emissão de access token (e refresh token opcional na v1).
- SPA React armazena token e envia via header `Authorization: Bearer <token>`.
- Sessão server-side **não** adotada na v1 (incompatível com escala horizontal sem store de sessão).

### Autorização

- Roles mínimas na v1: `OPERATOR`, `ADMIN` (extensível).
- Endpoints protegidos por padrão; exceções explícitas:
  - `POST /api/v1/auth/login`
  - `POST /api/v1/webhooks/mercadopago` (autenticação HMAC, não JWT)
  - `GET /actuator/health` (liveness)

### Erros

- Respostas de erro no formato **Problem Details** ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html)):
  ```json
  {
    "type": "https://api.financial-platform.lab/problems/insufficient-balance",
    "title": "Saldo insuficiente",
    "status": 422,
    "detail": "Conta de origem não possui saldo para a transferência.",
    "instance": "/api/v1/transfers",
    "correlationId": "550e8400-e29b-41d4-a716-446655440000"
  }
  ```
- Envelope `{ "data": {}, "metadata": {} }` reservado para respostas de sucesso (AGENTS.md).

### Trilha de auditoria

Toda operação financeira e mutação de estado deve registrar:

| Campo | Origem |
|-------|--------|
| `user` | Subject do JWT (`sub` ou claim `userId`) |
| `timestamp` | `Instant` UTC no momento da operação |
| `correlationId` | Header `X-Correlation-Id` ou gerado na entrada da requisição |
| `operation` | Nome semântico (ex.: `TRANSFER_MONEY`, `CREATE_CHARGE`) |

Correlation-id propagado para logs (OpenTelemetry), eventos Kafka e registros de auditoria.

### Implementação

- Configuração Spring Security em `infrastructure/` ou módulo `application/`.
- Filtros JWT em adapters; regras de autorização não contêm lógica de negócio.
- `audit-module` persiste eventos de auditoria consumindo domain events e metadados de requisição.

## Consequências

### Positivas

- API stateless escala horizontalmente sem sticky sessions.
- JWT é padrão de mercado para SPAs; integração simples com React + Axios.
- Problem Details padroniza tratamento de erros no frontend.
- Auditoria com correlation-id permite rastrear operação de ponta a ponta.

### Negativas

- JWT exige gestão de expiração e rotação de chaves de assinatura.
- Refresh token e logout global são mais complexos que sessão server-side.
- Tokens comprometidos não podem ser revogados sem blacklist (fora do escopo v1).

### Neutras

- OAuth2/OIDC com provedor externo pode ser adotado em fase futura.
- Webhooks Mercado Pago permanecem com HMAC, independente do JWT.
- Spring Security já está no stack definido em PROJECT.md.
