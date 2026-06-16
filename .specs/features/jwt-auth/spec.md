# JWT Auth — Especificação

**Módulo:** `application` (cross-cutting security)
**Endpoints:** `POST /api/v1/auth/login` (emissão); demais `/api/v1/**` protegidos por JWT
**Sprint:** 1 — Core Banking (fundação de segurança)
**Status:** Done
**ADR:** [ADR-0005](../../../adr/0005-spring-security-authentication.md)

---

## Problem Statement

O backoffice financeiro expõe APIs REST consumidas por uma SPA React. Operações de Core Banking (cadastro, contas, transferências) exigem autenticação e autorização antes de qualquer mutação de estado. Hoje os endpoints S1 estão abertos (`actor` fixo `system`), o que impede rastreabilidade por usuário e viola o requisito de auditoria definido em ADR-0005.

Sem JWT stateless e Problem Details padronizados para 401/403, o frontend não consegue tratar erros de autenticação de forma interoperável nem escalar o backend horizontalmente.

## Goals

- [x] Emitir access token JWT via `POST /api/v1/auth/login` com credenciais válidas
- [x] Validar JWT em requisições subsequentes via header `Authorization: Bearer <token>`
- [x] Proteger endpoints `/api/v1/**` por padrão, com exceções explícitas (login, webhook MP, health)
- [x] Retornar erros 401/403 no formato Problem Details (RFC 9457)
- [x] Suportar roles mínimas v1: `OPERATOR`, `ADMIN`
- [x] Permitir rollout gradual via flag `security.jwt.enabled` (permitAll → proteção ativa)

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Refresh token | Opcional na v1; ADR-0005 deixa para fase futura |
| OAuth2/OIDC com provedor externo | Fase futura (ADR-0005) |
| Logout global / blacklist de tokens | Complexidade fora do escopo v1 |
| Persistência de usuários em banco | v1 usa `InMemoryUserDetailsService` (operator/admin) |
| Autenticação HMAC de webhooks Mercado Pago | Feature separada; não usa JWT |
| Auditoria completa (`audit-module`) | Feature `audit-trail` (Sprint 6); v1 prepara claims JWT para `createdBy` |
| Frontend React (login UI, token storage) | Feature separada; backend entrega API de login |
| Rotação de chaves de assinatura | Documentar em design; automação fora do escopo v1 |

---

## User Stories

### P1: Login e emissão de JWT ⭐ MVP

**User Story:** Como operador do backoffice, quero autenticar com usuário e senha para receber um token JWT e acessar a API.

**Why P1:** Sem login não há fluxo autenticado para nenhuma operação financeira.

**Acceptance Criteria**:

1. WHEN `POST /api/v1/auth/login` com credenciais válidas (`username`, `password`) THEN sistema SHALL retornar `200 OK` com envelope `{ data: { accessToken, tokenType: "Bearer", expiresIn }, metadata: {} }`
2. WHEN login bem-sucedido THEN access token SHALL ser JWT assinado com claims `sub` (username), `roles` (lista), `exp` e `iat`
3. WHEN credenciais inválidas THEN sistema SHALL retornar `401 Unauthorized` com Problem Details (`type`, `title`, `detail`, `status`)
4. WHEN body inválido (campos ausentes) THEN sistema SHALL retornar `400 Bad Request`

**Independent Test:** POST login com usuário `operator` / senha configurada retorna 200 e token decodificável; credenciais erradas retornam 401.

---

### P2: Validação de JWT em requisições

**User Story:** Como API stateless, quero validar o JWT em cada requisição protegida para identificar o usuário autenticado sem sessão server-side.

**Why P2:** Base para auditoria (`sub` → `createdBy`) e escala horizontal.

**Acceptance Criteria**:

1. WHEN requisição protegida inclui `Authorization: Bearer <token>` válido THEN filtro JWT SHALL autenticar o `SecurityContext` com principal = `sub` e authorities derivadas de `roles`
2. WHEN token expirado THEN sistema SHALL retornar `401` com Problem Details indicando token expirado
3. WHEN token com assinatura inválida ou malformado THEN sistema SHALL retornar `401` com Problem Details
4. WHEN header `Authorization` ausente em rota protegida THEN sistema SHALL retornar `401`

**Independent Test:** GET `/api/v1/customers` com token válido retorna 200; sem header retorna 401; token expirado retorna 401.

---

### P3: Proteger endpoints da API

**User Story:** Como administrador de segurança, quero que todos os endpoints da API exijam autenticação por padrão, exceto rotas explicitamente públicas.

**Why P3:** Princípio fail-closed; evita exposição acidental de novos endpoints.

**Acceptance Criteria**:

1. WHEN `security.jwt.enabled=true` THEN todas as rotas `/api/v1/**` SHALL exigir autenticação JWT
2. WHEN rota é exceção THEN sistema SHALL permitir acesso sem JWT:
   - `POST /api/v1/auth/login`
   - `POST /api/v1/webhooks/mercadopago` (HMAC futuro — permitAll na v1)
   - `GET /actuator/health`
3. WHEN `security.jwt.enabled=false` (rollout) THEN sistema SHALL manter `permitAll` para não quebrar ITs existentes até migração
4. WHEN proteção ativa THEN CSRF SHALL estar desabilitado (API stateless)

**Independent Test:** Com proteção ativa, POST `/api/v1/customers` sem token retorna 401; POST `/api/v1/auth/login` retorna 200/401 conforme credenciais; GET `/actuator/health` retorna 200 sem token.

---

### P4: Erros 401/403 em Problem Details

**User Story:** Como desenvolvedor frontend, quero respostas de erro de autenticação/autorização no mesmo formato Problem Details das demais APIs para tratamento uniforme.

**Why P4:** ADR-0005 e `.rules/rest.md` exigem interoperabilidade de erros.

**Acceptance Criteria**:

1. WHEN autenticação falha (401) THEN resposta SHALL incluir `Content-Type: application/problem+json` com campos `type`, `title`, `status`, `detail`, `instance`
2. WHEN autorização falha (403) — usuário autenticado sem role suficiente THEN resposta SHALL retornar Problem Details com `status: 403`
3. WHEN erro de segurança THEN `type` SHALL usar URI sob `https://api.financial-platform.lab/problems/` (ex.: `invalid-credentials`, `token-expired`, `access-denied`)
4. WHEN possível THEN resposta SHALL incluir `correlationId` alinhado ao header `X-Correlation-Id`

**Independent Test:** 401 e 403 retornam JSON Problem Details parseável; não retornam envelope `{ data, metadata }`.

---

## Edge Cases

- WHEN login com username inexistente THEN sistema SHALL retornar `401` (mesma mensagem genérica que senha incorreta — evitar user enumeration)
- WHEN token Bearer com prefixo incorreto (`Basic`, token vazio) THEN sistema SHALL retornar `401`
- WHEN token válido mas role insuficiente para endpoint com `@PreAuthorize` futuro THEN sistema SHALL retornar `403`
- WHEN `security.jwt.enabled=false` THEN endpoints permanecem acessíveis sem token (modo transição)
- WHEN múltiplas instâncias do backend THEN JWT SHALL ser validável em qualquer instância (mesma chave de assinatura via config)
- WHEN `POST /api/v1/webhooks/mercadopago` THEN rota SHALL permanecer pública (autenticação HMAC em feature futura)

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| AUTH-01 | P1: Login 200 + envelope com accessToken | Execute | Done |
| AUTH-02 | P1: Login 401 credenciais inválidas | Execute | Done |
| AUTH-03 | P1: JWT claims sub, roles, exp, iat | Execute | Done |
| AUTH-04 | P2: Validação Bearer token em rotas protegidas | Execute | Done |
| AUTH-05 | P2: 401 token expirado / inválido / ausente | Execute | Done |
| AUTH-06 | P3: Proteger `/api/v1/**` por padrão | Execute | Done |
| AUTH-07 | P3: Exceções permitAll (login, webhook, health) | Execute | Done |
| AUTH-08 | P3: Flag `security.jwt.enabled` para rollout | Execute | Done |
| AUTH-09 | P4: 401 Problem Details | Execute | Done |
| AUTH-10 | P4: 403 Problem Details | Execute | Done |
| AUTH-11 | P1/P3: Roles OPERATOR e ADMIN (InMemory v1) | Execute | Done |
| AUTH-12 | P2: Authorities derivadas de claim `roles` no SecurityContext | Execute | Done |

**Coverage:** 12 total, 12 mapped to tasks (T1–T13), 12 done

---

## Success Criteria

- [x] `POST /api/v1/auth/login` funcional com testes unitários (JwtService, LoginUseCase) e integração (controller)
- [x] Com `security.jwt.enabled=true`, ITs existentes passam usando `JwtTestSupport`
- [x] 401/403 retornam Problem Details conforme RFC 9457
- [x] Nenhuma regra de negócio de domínio financeiro em filtros ou SecurityConfig
- [x] Gate `mvn verify -Pintegration` passa para application, customer-module e account-module
- [x] ADR-0005 referenciado e decisões refletidas em design.md
