# JWT Auth — Tasks

**Design:** `.specs/features/jwt-auth/design.md`
**Reference:** `.specs/codebase/INDEX.md` (controller + IT patterns)
**ADR:** [ADR-0005](../../../adr/0005-spring-security-authentication.md)
**Status:** Done

**Total tasks:** 13

---

## Execution Plan

### Phase 1: Foundation (Sequential)

```text
T1 → T2 → T3
```

### Phase 2: Auth core (Sequential)

```text
T3 → T4 → T5 → T6
```

### Phase 3: Protection (Parallel OK)

```text
T6 → T7
  → T8 [P]
```

### Phase 4: IT migration (Sequential)

```text
T7, T8 → T9 → T10 → T11 → T12
```

### Phase 5: Close

```text
T12 → T13
```

---

## Task Breakdown

### T1: Dependências Maven (Spring Security + JWT)

**What:** Adicionar `spring-boot-starter-security` e biblioteca JWT ao `application/pom.xml`
**Where:** `backend/application/pom.xml`
**Depends on:** None
**Reuses:** BOM Spring Boot parent (`backend/pom.xml`)
**Requirement:** AUTH-03, AUTH-04

**Done when**:

- [x] Dependências resolvem sem conflito
- [x] `mvn compile -pl application` passa

**Tests:** none | **Gate:** build

---

### T2: SecurityConfig skeleton + JwtProperties

**What:** `SecurityFilterChain` com `security.jwt.enabled=false` (permitAll), `JwtProperties` e `application.yml`
**Where:** `backend/application/src/main/java/com/financialplatform/infrastructure/security/`
**Depends on:** T1
**Reuses:** Padrão config em `application.yml`
**Requirement:** AUTH-06, AUTH-07, AUTH-08

**Done when**:

- [x] Bean `SecurityFilterChain` carrega com flag desligada
- [x] Propriedades `security.jwt.*` bindam corretamente
- [x] Gate: `mvn test -pl application`

**Tests:** unit (context smoke) | **Gate:** quick

---

### T3: SecurityProblemDetailsHandler (401/403)

**What:** `AuthenticationEntryPoint` e `AccessDeniedHandler` retornando Problem Details
**Where:** `backend/application/src/main/java/com/financialplatform/infrastructure/security/SecurityProblemDetailsHandler.java`
**Depends on:** T2
**Reuses:** URI base e formato de `CustomerExceptionHandler`
**Requirement:** AUTH-09, AUTH-10

**Done when**:

- [x] 401 e 403 retornam `application/problem+json`
- [x] `type`, `title`, `status`, `detail`, `instance` presentes
- [x] Gate: `mvn test -pl application`
- [x] Test count: ≥2 passam

**Tests:** unit | **Gate:** quick

---

### T4: JwtService + InMemoryUserDetailsService

**What:** Geração/validação JWT; usuários operator/admin em memória
**Where:** `backend/application/src/main/java/com/financialplatform/infrastructure/security/`
**Depends on:** T3
**Requirement:** AUTH-03, AUTH-11, AUTH-12

**Done when**:

- [x] Token contém `sub`, `roles`, `iat`, `exp`
- [x] `operator` e `admin` carregam com roles corretas
- [x] Token expirado rejeitado
- [x] Gate: `mvn test -pl application`
- [x] Test count: ≥4 passam

**Tests:** unit | **Gate:** quick

---

### T5: LoginUseCase + testes unitários

**What:** Orquestra autenticação e emissão de token
**Where:** `backend/application/src/main/java/com/financialplatform/features/auth/LoginUseCase.java`
**Depends on:** T4
**Reuses:** Padrão `CreateCustomerUseCase` (delegação, sem Spring no domínio)
**Requirement:** AUTH-01, AUTH-02

**Done when**:

- [x] Happy path retorna `LoginResult` com token
- [x] Credenciais inválidas lançam exceção mapeável a 401
- [x] Gate: `mvn test -pl application`
- [x] Test count: ≥3 passam

**Tests:** unit | **Gate:** quick

---

### T6: LoginController + DTOs + IT login

**What:** `POST /api/v1/auth/login` com envelope de sucesso
**Where:** `backend/application/src/main/java/com/financialplatform/features/auth/`
**Depends on:** T5
**Reuses:** Padrão controller em INDEX.md → `CreateCustomerController`
**Requirement:** AUTH-01, AUTH-02

**Done when**:

- [x] 200 com `{ data: { accessToken, tokenType, expiresIn }, metadata: {} }`
- [x] 401 credenciais inválidas; 400 body inválido
- [x] Gate: `mvn verify -Pintegration -pl application`
- [x] Test count: ≥3 passam

**Tests:** integration | **Gate:** full

---

### T7: JwtAuthenticationFilter + proteção filter chain [P]

**What:** Filtro Bearer + regras ADR com `security.jwt.enabled=true`
**Where:** `backend/application/src/main/java/com/financialplatform/infrastructure/security/JwtAuthenticationFilter.java`, atualizar `SecurityConfig`
**Depends on:** T6
**Requirement:** AUTH-04, AUTH-05, AUTH-06, AUTH-07

**Done when**:

- [x] Rotas protegidas exigem JWT quando flag=true
- [x] Exceções permitAll funcionam (login, health, webhook path)
- [x] Gate: `mvn verify -Pintegration -pl application`
- [x] Test count: ≥4 passam

**Tests:** integration | **Gate:** full

---

### T8: JwtTestSupport [P]

**What:** Helper de teste para obter token e adicionar Bearer header
**Where:** `backend/application/src/test/java/com/financialplatform/support/JwtTestSupport.java`
**Depends on:** T6
**Reuses:** Login endpoint de T6
**Requirement:** AUTH-08

**Done when**:

- [x] `obtainOperatorToken(MockMvc)` funcional
- [x] `bearerToken(String)` RequestPostProcessor disponível
- [x] Documentado em comentário Javadoc para módulos consumidores

**Tests:** none (usado por T9–T12) | **Gate:** build

---

### T9: Atualizar IT query-customers

**What:** Habilitar JWT nos testes; requests autenticados
**Where:** `backend/customer-module/src/test/java/.../QueryCustomersControllerIntegrationTest.java`, `AbstractCustomerIntegrationTest`
**Depends on:** T7, T8
**Requirement:** AUTH-04, AUTH-05

**Done when**:

- [x] `security.jwt.enabled=true` via `@DynamicPropertySource`
- [x] Todos os requests HTTP incluem Bearer token
- [x] Gate: `mvn verify -Pintegration -pl customer-module`
- [x] Test count: suite existente verde

**Tests:** integration | **Gate:** full

---

### T10: Atualizar IT create-customer

**What:** Migrar `CreateCustomerControllerIntegrationTest` para auth
**Where:** `backend/customer-module/src/test/java/.../createcustomer/`
**Depends on:** T8
**Requirement:** AUTH-04, AUTH-05

**Done when**:

- [x] POST `/api/v1/customers` com token retorna 201
- [x] POST sem token retorna 401 (quando flag=true)
- [x] Gate: `mvn verify -Pintegration -pl customer-module`

**Tests:** integration | **Gate:** full

---

### T11: Atualizar IT create-account

**What:** Migrar `CreateAccountControllerIntegrationTest` e `AbstractAccountWebIntegrationTest`
**Where:** `backend/account-module/src/test/java/.../`
**Depends on:** T8
**Requirement:** AUTH-04, AUTH-05

**Done when**:

- [x] POST `/api/v1/accounts` autenticado
- [x] Gate: `mvn verify -Pintegration -pl account-module`

**Tests:** integration | **Gate:** full

---

### T12: Atualizar ApplicationWiringIntegrationTest

**What:** Smoke end-to-end: login + customer query + account create com JWT
**Where:** `backend/application/src/test/java/com/financialplatform/ApplicationWiringIntegrationTest.java`
**Depends on:** T9, T10, T11
**Requirement:** AUTH-01, AUTH-04, AUTH-06

**Done when**:

- [x] Fluxo login → GET customers → POST account passa
- [x] Gate: `mvn verify -Pintegration`

**Tests:** integration | **Gate:** full

---

### T13: Documentação + feature close

**What:** Atualizar `.env.example`, `INDEX.md`, `INTEGRATIONS.md`, `STATE.md`, `ROADMAP.md`, `CONCERNS.md`; marcar spec/design/tasks Done
**Where:** `.specs/`, `.env.example`, `docs/` se aplicável
**Depends on:** T12
**Requirement:** AUTH-01 a AUTH-12

**Done when**:

- [x] Variáveis `JWT_*` e `SECURITY_JWT_ENABLED` documentadas
- [x] Feature Close Checklist (`AGENTS.md`) completo
- [x] spec.md Success Criteria marcados
- [x] Gate: `mvn verify -Pintegration`

**Tests:** none | **Gate:** full
