# JWT Auth — Tasks

**Design:** `.specs/features/jwt-auth/design.md`
**Reference:** `.specs/codebase/INDEX.md` (controller + IT patterns)
**ADR:** [ADR-0005](../../../adr/0005-spring-security-authentication.md)
**Status:** Draft

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

- [ ] Dependências resolvem sem conflito
- [ ] `mvn compile -pl application` passa

**Tests:** none | **Gate:** build

---

### T2: SecurityConfig skeleton + JwtProperties

**What:** `SecurityFilterChain` com `security.jwt.enabled=false` (permitAll), `JwtProperties` e `application.yml`
**Where:** `backend/application/src/main/java/com/financialplatform/infrastructure/security/`
**Depends on:** T1
**Reuses:** Padrão config em `application.yml`
**Requirement:** AUTH-06, AUTH-07, AUTH-08

**Done when**:

- [ ] Bean `SecurityFilterChain` carrega com flag desligada
- [ ] Propriedades `security.jwt.*` bindam corretamente
- [ ] Gate: `mvn test -pl application`

**Tests:** unit (context smoke) | **Gate:** quick

---

### T3: SecurityProblemDetailsHandler (401/403)

**What:** `AuthenticationEntryPoint` e `AccessDeniedHandler` retornando Problem Details
**Where:** `backend/application/src/main/java/com/financialplatform/infrastructure/security/SecurityProblemDetailsHandler.java`
**Depends on:** T2
**Reuses:** URI base e formato de `CustomerExceptionHandler`
**Requirement:** AUTH-09, AUTH-10

**Done when**:

- [ ] 401 e 403 retornam `application/problem+json`
- [ ] `type`, `title`, `status`, `detail`, `instance` presentes
- [ ] Gate: `mvn test -pl application`
- [ ] Test count: ≥2 passam

**Tests:** unit | **Gate:** quick

---

### T4: JwtService + InMemoryUserDetailsService

**What:** Geração/validação JWT; usuários operator/admin em memória
**Where:** `backend/application/src/main/java/com/financialplatform/infrastructure/security/`
**Depends on:** T3
**Requirement:** AUTH-03, AUTH-11, AUTH-12

**Done when**:

- [ ] Token contém `sub`, `roles`, `iat`, `exp`
- [ ] `operator` e `admin` carregam com roles corretas
- [ ] Token expirado rejeitado
- [ ] Gate: `mvn test -pl application`
- [ ] Test count: ≥4 passam

**Tests:** unit | **Gate:** quick

---

### T5: LoginUseCase + testes unitários

**What:** Orquestra autenticação e emissão de token
**Where:** `backend/application/src/main/java/com/financialplatform/features/auth/LoginUseCase.java`
**Depends on:** T4
**Reuses:** Padrão `CreateCustomerUseCase` (delegação, sem Spring no domínio)
**Requirement:** AUTH-01, AUTH-02

**Done when**:

- [ ] Happy path retorna `LoginResult` com token
- [ ] Credenciais inválidas lançam exceção mapeável a 401
- [ ] Gate: `mvn test -pl application`
- [ ] Test count: ≥3 passam

**Tests:** unit | **Gate:** quick

---

### T6: LoginController + DTOs + IT login

**What:** `POST /api/v1/auth/login` com envelope de sucesso
**Where:** `backend/application/src/main/java/com/financialplatform/features/auth/`
**Depends on:** T5
**Reuses:** Padrão controller em INDEX.md → `CreateCustomerController`
**Requirement:** AUTH-01, AUTH-02

**Done when**:

- [ ] 200 com `{ data: { accessToken, tokenType, expiresIn }, metadata: {} }`
- [ ] 401 credenciais inválidas; 400 body inválido
- [ ] Gate: `mvn verify -Pintegration -pl application`
- [ ] Test count: ≥3 passam

**Tests:** integration | **Gate:** full

---

### T7: JwtAuthenticationFilter + proteção filter chain [P]

**What:** Filtro Bearer + regras ADR com `security.jwt.enabled=true`
**Where:** `backend/application/src/main/java/com/financialplatform/infrastructure/security/JwtAuthenticationFilter.java`, atualizar `SecurityConfig`
**Depends on:** T6
**Requirement:** AUTH-04, AUTH-05, AUTH-06, AUTH-07

**Done when**:

- [ ] Rotas protegidas exigem JWT quando flag=true
- [ ] Exceções permitAll funcionam (login, health, webhook path)
- [ ] Gate: `mvn verify -Pintegration -pl application`
- [ ] Test count: ≥4 passam

**Tests:** integration | **Gate:** full

---

### T8: JwtTestSupport [P]

**What:** Helper de teste para obter token e adicionar Bearer header
**Where:** `backend/application/src/test/java/com/financialplatform/support/JwtTestSupport.java`
**Depends on:** T6
**Reuses:** Login endpoint de T6
**Requirement:** AUTH-08

**Done when**:

- [ ] `obtainOperatorToken(MockMvc)` funcional
- [ ] `bearerToken(String)` RequestPostProcessor disponível
- [ ] Documentado em comentário Javadoc para módulos consumidores

**Tests:** none (usado por T9–T12) | **Gate:** build

---

### T9: Atualizar IT query-customers

**What:** Habilitar JWT nos testes; requests autenticados
**Where:** `backend/customer-module/src/test/java/.../QueryCustomersControllerIntegrationTest.java`, `AbstractCustomerIntegrationTest`
**Depends on:** T7, T8
**Requirement:** AUTH-04, AUTH-05

**Done when**:

- [ ] `security.jwt.enabled=true` via `@DynamicPropertySource`
- [ ] Todos os requests HTTP incluem Bearer token
- [ ] Gate: `mvn verify -Pintegration -pl customer-module`
- [ ] Test count: suite existente verde

**Tests:** integration | **Gate:** full

---

### T10: Atualizar IT create-customer

**What:** Migrar `CreateCustomerControllerIntegrationTest` para auth
**Where:** `backend/customer-module/src/test/java/.../createcustomer/`
**Depends on:** T8
**Requirement:** AUTH-04, AUTH-05

**Done when**:

- [ ] POST `/api/v1/customers` com token retorna 201
- [ ] POST sem token retorna 401 (quando flag=true)
- [ ] Gate: `mvn verify -Pintegration -pl customer-module`

**Tests:** integration | **Gate:** full

---

### T11: Atualizar IT create-account

**What:** Migrar `CreateAccountControllerIntegrationTest` e `AbstractAccountWebIntegrationTest`
**Where:** `backend/account-module/src/test/java/.../`
**Depends on:** T8
**Requirement:** AUTH-04, AUTH-05

**Done when**:

- [ ] POST `/api/v1/accounts` autenticado
- [ ] Gate: `mvn verify -Pintegration -pl account-module`

**Tests:** integration | **Gate:** full

---

### T12: Atualizar ApplicationWiringIntegrationTest

**What:** Smoke end-to-end: login + customer query + account create com JWT
**Where:** `backend/application/src/test/java/com/financialplatform/ApplicationWiringIntegrationTest.java`
**Depends on:** T9, T10, T11
**Requirement:** AUTH-01, AUTH-04, AUTH-06

**Done when**:

- [ ] Fluxo login → GET customers → POST account passa
- [ ] Gate: `mvn verify -Pintegration`

**Tests:** integration | **Gate:** full

---

### T13: Documentação + feature close

**What:** Atualizar `.env.example`, `INDEX.md`, `INTEGRATIONS.md`, `STATE.md`, `ROADMAP.md`, `CONCERNS.md`; marcar spec/design/tasks Done
**Where:** `.specs/`, `.env.example`, `docs/` se aplicável
**Depends on:** T12
**Requirement:** AUTH-01 a AUTH-12

**Done when**:

- [ ] Variáveis `JWT_*` e `SECURITY_JWT_ENABLED` documentadas
- [ ] Feature Close Checklist (`.rules/workflow.md`) completo
- [ ] spec.md Success Criteria marcados
- [ ] Gate: `mvn verify -Pintegration`

**Tests:** none | **Gate:** full
