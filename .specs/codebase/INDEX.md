# Codebase Index

**Updated:** 2026-06-17  
**Purpose:** Índice vivo do que está implementado. Agentes devem consultar este arquivo em vez de explorar o repositório quando `tasks.md` aponta padrões existentes.

---

## Modules

| Module | Status | Features |
| ------ | ------ | -------- |
| `shared-kernel` | ✅ Implemented | Money, Cpf, Cnpj, Identifier, AggregateRoot, AuditableEntity, DomainEvent |
| `customer-module` | ✅ Implemented | create-customer, query-customers, update-customer |
| `account-module` | ✅ Implemented | create-account, transfer-money |
| `application` | ✅ Implemented | Flyway V1–V6, health, jwt-auth, module wiring smoke tests, integrated transfer flow |

---

## Reference slice: create-customer

Use como padrão para novas vertical slices no `customer-module`.

| Layer | Path |
| ----- | ---- |
| Feature slice | `backend/customer-module/src/main/java/com/financialplatform/customer/features/createcustomer/` |
| Use case | `.../CreateCustomerUseCase.java` |
| Controller | `.../CreateCustomerController.java` |
| Request/Response | `.../CreateCustomerRequest.java`, `CreateCustomerResponse.java` |
| Domain aggregate | `backend/customer-module/src/main/java/.../domain/Customer.java` |
| Repository port | `backend/customer-module/src/main/java/.../ports/CustomerRepositoryPort.java` |
| JPA adapter | `backend/customer-module/src/main/java/.../adapters/persistence/` |
| Module config | `backend/customer-module/src/main/java/.../infrastructure/CustomerModuleConfig.java` |
| Exception handler | `backend/customer-module/src/main/java/.../infrastructure/CustomerExceptionHandler.java` |

### Tests (create-customer)

| Type | Path |
| ---- | ---- |
| Unit (domain) | `backend/customer-module/src/test/java/.../domain/*Test.java` |
| Unit (use case) | `backend/customer-module/src/test/java/.../features/createcustomer/CreateCustomerUseCaseTest.java` |
| Integration (repository) | `backend/customer-module/src/test/java/.../adapters/persistence/JpaCustomerRepositoryIntegrationTest.java` |
| Integration (controller) | `backend/customer-module/src/test/java/.../features/createcustomer/CreateCustomerControllerIntegrationTest.java` |
| IT base class | `backend/customer-module/src/test/java/.../support/AbstractCustomerIntegrationTest.java` |
| Test application | `backend/customer-module/src/test/java/.../support/CustomerModuleTestApplication.java` |

### Migrations (create-customer)

| Version | Path |
| ------- | ---- |
| V2 customers | `backend/application/src/main/resources/db/migration/V2__customers.sql` |
| Test copy | `backend/customer-module/src/test/resources/db/migration/` |

### API (create-customer)

- `POST /api/v1/customers` — envelope `{ data, metadata }`, Problem Details em erros

---

## Reference slice: query-customers

Use como padrão para a vertical slice de consulta no `customer-module`.

| Layer | Path |
| ----- | ---- |
| Feature slice | `backend/customer-module/src/main/java/.../features/querycustomers/` |
| Use cases | `.../QueryCustomersUseCase.java`, `GetCustomerByIdUseCase.java` |
| Controller | `.../QueryCustomersController.java` (GET list + GET by id) |
| Query / result | `.../QueryCustomersQuery.java`, `QueryCustomersResult.java`, `GetCustomerByIdQuery.java`, `CustomerDetailResult.java` |
| Domain exception | `backend/customer-module/src/main/java/.../domain/CustomerNotFoundException.java` |
| Query port | `backend/customer-module/src/main/java/.../ports/CustomerQueryPort.java` |
| Read models | `backend/customer-module/src/main/java/.../application/readmodel/` |
| CustomerSummary | `.../application/readmodel/CustomerSummary.java` |
| CustomerFilter | `.../application/readmodel/CustomerFilter.java` |
| PageResult / PageRequest | `.../application/readmodel/PageResult.java`, `PageRequest.java` |
| Query adapter | `backend/customer-module/src/main/java/.../adapters/persistence/JpaCustomerQueryAdapter.java` |
| Query specifications | `.../adapters/persistence/CustomerQuerySpecifications.java` |
| Summary mapper | `.../adapters/persistence/CustomerSummaryMapper.java` |
| Query response DTOs | `backend/customer-module/src/main/java/.../features/querycustomers/` (`QueryCustomersResponse`, `GetCustomerByIdResponse`, `PaginationMetadata`) |
| Module config | `backend/customer-module/src/main/java/.../infrastructure/CustomerModuleConfig.java` |
| App wiring smoke | `backend/application/src/test/java/com/financialplatform/ApplicationWiringIntegrationTest.java` |

### Tests (query-customers)

| Type | Path |
| ---- | ---- |
| Unit (use case) | `backend/customer-module/src/test/java/.../features/querycustomers/QueryCustomersUseCaseTest.java`, `GetCustomerByIdUseCaseTest.java` |
| Integration (query adapter) | `backend/customer-module/src/test/java/.../adapters/persistence/JpaCustomerQueryAdapterIntegrationTest.java` |
| Integration (controller) | `backend/customer-module/src/test/java/.../features/querycustomers/QueryCustomersControllerIntegrationTest.java` |
| IT base class | `backend/customer-module/src/test/java/.../support/AbstractCustomerIntegrationTest.java` |
| Test application | `backend/customer-module/src/test/java/.../support/CustomerModuleTestApplication.java` |

### API (query-customers)

- `GET /api/v1/customers` — listagem paginada com filtros `page`, `size`, `name`, `type`, `document`; envelope `{ data, metadata }`
- `GET /api/v1/customers/{id}` — detalhe do cliente; envelope `{ data, metadata }`, 404 se não encontrado

**Verify (manual):**

```bash
curl -s http://localhost:8080/api/v1/customers | jq .
curl -s http://localhost:8080/api/v1/customers/{id} | jq .
```

---

## Reference slice: update-customer

Use como padrão para a vertical slice de atualização parcial no `customer-module`.

| Layer | Path |
| ----- | ---- |
| Feature slice | `backend/customer-module/src/main/java/.../features/updatecustomer/` |
| Use case | `.../UpdateCustomerUseCase.java` |
| Controller | `.../UpdateCustomerController.java` |
| Request/Response | `.../UpdateCustomerRequest.java`, `UpdateCustomerResponse.java` |
| Command / result | `.../UpdateCustomerCommand.java`, `UpdateCustomerResult.java` |
| Domain exceptions | `backend/customer-module/src/main/java/.../domain/NoFieldsToUpdateException.java`, `ImmutableFieldException.java` |
| Shared domain exception | `backend/customer-module/src/main/java/.../domain/CustomerNotFoundException.java` |
| Repository port | `backend/customer-module/src/main/java/.../ports/CustomerRepositoryPort.java` |
| Domain aggregate | `backend/customer-module/src/main/java/.../domain/Customer.java` (`updateProfile`) |
| Module config | `backend/customer-module/src/main/java/.../infrastructure/CustomerModuleConfig.java` |
| Exception handler | `backend/customer-module/src/main/java/.../infrastructure/CustomerExceptionHandler.java` |

### Tests (update-customer)

| Type | Path |
| ---- | ---- |
| Unit (domain) | `backend/customer-module/src/test/java/.../domain/CustomerUpdateTest.java` |
| Unit (use case) | `backend/customer-module/src/test/java/.../features/updatecustomer/UpdateCustomerUseCaseTest.java` |
| Integration (controller) | `backend/customer-module/src/test/java/.../features/updatecustomer/UpdateCustomerControllerIntegrationTest.java` |
| IT base class | `backend/customer-module/src/test/java/.../support/AbstractCustomerIntegrationTest.java` |
| Test application | `backend/customer-module/src/test/java/.../support/CustomerModuleTestApplication.java` |

### API (update-customer)

- `PATCH /api/v1/customers/{id}` — atualização parcial de `name` e/ou `email`; envelope `{ data, metadata }`, Problem Details em erros
- 400 quando body vazio (`NoFieldsToUpdateException`), `document`/`type` enviados (`ImmutableFieldException`) ou validação falha
- 404 quando cliente não existe (`CustomerNotFoundException`)
- Requer `Authorization: Bearer <token>` quando `security.jwt.enabled=true` (ver slice `jwt-auth`)

**Verify (manual):**

```bash
# Obter token (quando JWT habilitado)
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"operator","password":"operator"}' | jq -r '.data.accessToken')

curl -s -X PATCH http://localhost:8080/api/v1/customers/{id} \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Maria Santos","email":"maria.santos@example.com"}' | jq .
```

---

## Reference slice: create-account

Use como padrão para novas vertical slices no `account-module` (ex.: `transfer-money`).

| Layer | Path |
| ----- | ---- |
| Feature slice | `backend/account-module/src/main/java/com/financialplatform/account/features/createaccount/` |
| Use case | `.../CreateAccountUseCase.java` |
| Controller | `.../CreateAccountController.java` |
| Request/Response | `.../CreateAccountRequest.java`, `CreateAccountResponse.java` |
| Domain aggregate | `backend/account-module/src/main/java/.../domain/Account.java` |
| Domain event | `backend/account-module/src/main/java/.../domain/AccountCreated.java` |
| Repository port | `backend/account-module/src/main/java/.../ports/AccountRepositoryPort.java` |
| Cross-module ports | `CustomerLookupPort`, `LedgerPort`, `EventPublisherPort` |
| JPA adapter | `backend/account-module/src/main/java/.../adapters/persistence/` |
| Customer adapter | `backend/account-module/src/main/java/.../adapters/customer/InProcessCustomerLookupAdapter.java` |
| Ledger stub | `backend/account-module/src/main/java/.../adapters/ledger/LedgerStubAdapter.java` |
| Kafka adapter | `backend/account-module/src/main/java/.../adapters/messaging/KafkaEventPublisherAdapter.java` |
| Module config | `backend/account-module/src/main/java/.../infrastructure/AccountModuleConfig.java` |
| Exception handler | `backend/account-module/src/main/java/.../infrastructure/AccountExceptionHandler.java` |
| Auto-config | `backend/account-module/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |

### Tests (create-account)

| Type | Path |
| ---- | ---- |
| Unit (domain) | `backend/account-module/src/test/java/.../domain/AccountTest.java` |
| Unit (use case) | `backend/account-module/src/test/java/.../features/createaccount/CreateAccountUseCaseTest.java` |
| Unit (ledger stub) | `backend/account-module/src/test/java/.../adapters/ledger/LedgerStubAdapterTest.java` |
| Unit (customer lookup) | `backend/account-module/src/test/java/.../adapters/customer/InProcessCustomerLookupAdapterTest.java` |
| Integration (repository) | `backend/account-module/src/test/java/.../adapters/persistence/JpaAccountRepositoryIntegrationTest.java` |
| Integration (messaging) | `backend/account-module/src/test/java/.../adapters/messaging/KafkaEventPublisherIntegrationTest.java` |
| Integration (controller) | `backend/account-module/src/test/java/.../features/createaccount/CreateAccountControllerIntegrationTest.java` |
| IT base class | `backend/account-module/src/test/java/.../support/AbstractAccountWebIntegrationTest.java` |
| Test application | `backend/account-module/src/test/java/.../support/AccountModuleTestApplication.java` |
| App wiring smoke | `backend/application/src/test/java/com/financialplatform/ApplicationWiringIntegrationTest.java` |

### Migrations (create-account)

| Version | Path |
| ------- | ---- |
| V3 accounts | `backend/application/src/main/resources/db/migration/V3__accounts.sql` |
| Test copy | `backend/account-module/src/test/resources/db/migration/` |

### API (create-account)

- `POST /api/v1/accounts` — envelope `{ data, metadata }`, Problem Details em erros
- Kafka topic: `account-created`

---

## Reference slice: transfer-money

Use como padrão para transferências internas no `account-module` (ledger-first + evento `TransferExecuted`).

| Layer | Path |
| ----- | ---- |
| Feature slice | `backend/account-module/src/main/java/com/financialplatform/account/features/transfermoney/` |
| Use case | `.../TransferMoneyUseCase.java` |
| Controller | `.../TransferMoneyController.java` |
| Request/Response | `.../TransferMoneyRequest.java`, `TransferMoneyResponse.java` |
| Command / result | `.../TransferMoneyCommand.java`, `TransferMoneyResult.java` |
| Domain entity | `backend/account-module/src/main/java/.../domain/Transfer.java` |
| Domain service | `backend/account-module/src/main/java/.../domain/TransferDomainService.java` |
| Domain event | `backend/account-module/src/main/java/.../domain/TransferExecuted.java` |
| Transfer status | `backend/account-module/src/main/java/.../domain/TransferStatus.java` |
| Repository port | `backend/account-module/src/main/java/.../ports/TransferRepositoryPort.java` |
| Ledger port | `backend/account-module/src/main/java/.../ports/LedgerPort.java` (`recordTransfer`) |
| JPA adapter | `backend/account-module/src/main/java/.../adapters/persistence/JpaTransferRepository.java` |
| Ledger stub | `backend/account-module/src/main/java/.../adapters/ledger/LedgerStubAdapter.java` |
| Kafka adapter | `backend/account-module/src/main/java/.../adapters/messaging/KafkaEventPublisherAdapter.java` |
| Event serializer | `.../adapters/messaging/TransferExecutedJsonSerializer.java` |
| Transactional boundary | `backend/account-module/src/main/java/.../infrastructure/TransferMoneyTransactionalBoundary.java` |
| Module config | `backend/account-module/src/main/java/.../infrastructure/AccountModuleConfig.java` |
| Test helper (credit seed) | `backend/account-module/src/test/java/.../support/LedgerTestSupport.java` |

### Tests (transfer-money)

| Type | Path |
| ---- | ---- |
| Unit (domain) | `backend/account-module/src/test/java/.../domain/TransferTest.java`, `TransferDomainServiceTest.java` |
| Unit (use case) | `backend/account-module/src/test/java/.../features/transfermoney/TransferMoneyUseCaseTest.java` |
| Unit (ledger stub) | `backend/account-module/src/test/java/.../adapters/ledger/LedgerStubAdapterTest.java` |
| Integration (repository) | `backend/account-module/src/test/java/.../adapters/persistence/JpaTransferRepositoryIntegrationTest.java` |
| Integration (ledger) | `backend/account-module/src/test/java/.../adapters/ledger/LedgerStubAdapterIntegrationTest.java` |
| Integration (messaging) | `backend/account-module/src/test/java/.../adapters/messaging/KafkaEventPublisherIntegrationTest.java` (incl. `TransferExecuted`) |
| Integration (controller) | `backend/account-module/src/test/java/.../features/transfermoney/TransferMoneyControllerIntegrationTest.java` |
| Integration (transaction) | `.../TransferMoneyTransactionalIntegrationTest.java`, `TransferMoneyLedgerFailureRollbackIntegrationTest.java` |
| Integrated flow (app) | `backend/application/src/test/java/com/financialplatform/features/transfermoney/TransferMoneyIntegratedFlowIntegrationTest.java` |
| IT base class | `backend/account-module/src/test/java/.../support/AbstractAccountWebIntegrationTest.java` |
| Test application | `backend/account-module/src/test/java/.../support/AccountModuleTestApplication.java` |

### Migrations (transfer-money)

| Version | Path |
| ------- | ---- |
| V5 transfers | `backend/application/src/main/resources/db/migration/V5__transfers.sql` |
| V6 ledger_entries_stub | `backend/application/src/main/resources/db/migration/V6__ledger_entries_stub.sql` |
| Test copy | `backend/account-module/src/test/resources/db/migration/` |

### API (transfer-money)

- `POST /api/v1/transfers` — envelope `{ data, metadata }`, Problem Details em erros (400, 404, 409, 422)
- Kafka topic: `transfer-executed`
- Requer `Authorization: Bearer <token>` quando `security.jwt.enabled=true`

**Verify (manual):**

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"operator","password":"operator"}' | jq -r '.data.accessToken')

curl -s -X POST http://localhost:8080/api/v1/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"originAccountId":"...","destinationAccountId":"...","amount":"100.00","correlationId":"770e8400-e29b-41d4-a716-446655440002"}' | jq .
```

---

## Reference slice: jwt-auth

Use como padrão para autenticação cross-cutting no módulo `application` (login + filtro JWT + Problem Details 401/403).

| Layer | Path |
| ----- | ---- |
| Feature slice (login) | `backend/application/src/main/java/com/financialplatform/features/auth/` |
| Use case | `.../LoginUseCase.java` |
| Controller | `.../LoginController.java` |
| Request/Response | `.../LoginRequest.java`, `LoginResponse.java` |
| Auth exception handler | `.../AuthExceptionHandler.java` |
| Module config | `.../AuthModuleConfig.java` |
| Security config | `backend/application/src/main/java/com/financialplatform/infrastructure/security/SecurityConfig.java` |
| JWT filter | `.../JwtAuthenticationFilter.java` |
| JWT service | `.../JwtService.java` |
| JWT properties | `.../JwtProperties.java` |
| User details (v1 in-memory) | `.../InMemoryUserDetailsService.java` |
| Security error handler | `.../SecurityProblemDetailsHandler.java` |
| App wiring smoke | `backend/application/src/test/java/com/financialplatform/ApplicationWiringIntegrationTest.java` |

### Tests (jwt-auth)

| Type | Path |
| ---- | ---- |
| Unit (JWT service) | `backend/application/src/test/java/.../infrastructure/security/JwtServiceTest.java` |
| Unit (user details) | `backend/application/src/test/java/.../infrastructure/security/InMemoryUserDetailsServiceTest.java` |
| Unit (security config) | `backend/application/src/test/java/.../infrastructure/security/SecurityConfigTest.java` |
| Unit (problem details) | `backend/application/src/test/java/.../infrastructure/security/SecurityProblemDetailsHandlerTest.java` |
| Unit (login use case) | `backend/application/src/test/java/.../features/auth/LoginUseCaseTest.java` |
| Integration (login) | `backend/application/src/test/java/.../features/auth/LoginControllerIntegrationTest.java` |
| Integration (JWT filter) | `backend/application/src/test/java/.../features/auth/JwtAuthenticationFilterIntegrationTest.java` |
| IT helper | `backend/application/src/test/java/com/financialplatform/support/JwtTestSupport.java` |
| Consumer IT bases | `AbstractCustomerIntegrationTest`, `AbstractAccountWebIntegrationTest` (JWT via `@DynamicPropertySource`) |

### Configuration (jwt-auth)

| Property | Env var | Default |
| -------- | ------- | ------- |
| `security.jwt.enabled` | `SECURITY_JWT_ENABLED` | `false` |
| `security.jwt.secret` | `JWT_SECRET` | `change-me-in-production` |
| `security.jwt.expiration-seconds` | `JWT_EXPIRATION_SECONDS` | `3600` |

### API (jwt-auth)

- `POST /api/v1/auth/login` — envelope `{ data: { accessToken, tokenType, expiresIn }, metadata: {} }`; 401 Problem Details em credenciais inválidas
- Rotas protegidas (quando `security.jwt.enabled=true`): `Authorization: Bearer <token>`; exceções `permitAll`: login, `POST /api/v1/webhooks/mercadopago`, `GET /actuator/health`

**Verify (manual):**

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"operator","password":"operator"}' | jq .
```

---

## Shared kernel (quick paths)

| Component | Path |
| --------- | ---- |
| Cpf | `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/Cpf.java` |
| Cnpj | `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/Cnpj.java` |
| Identifier | `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/Identifier.java` |
| AggregateRoot | `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/AggregateRoot.java` |
| AuditableEntity | `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/AuditableEntity.java` |
| DomainEvent | `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/DomainEvent.java` |

---

## Test infrastructure

| Item | Path / command |
| ---- | -------------- |
| Gate commands | `.specs/codebase/TESTING.md` → Gate Check Commands |
| docker-java API 1.44 | `backend/*/src/test/resources/docker-java.properties` |
| Integration profile | `mvn verify -Pintegration` |
| Unit only (customer) | `mvn test -pl customer-module` |
| Unit only (account) | `mvn test -pl account-module` |
| Integration (customer) | `mvn verify -Pintegration -pl customer-module` |
| Integration (account) | `mvn verify -Pintegration -pl account-module` |

---

## Do not re-explore

When implementing a feature whose `tasks.md` lists **Reuses** pointing here:

1. Read only the listed reference files.
2. Do not spawn exploration subagents for patterns already indexed above.
3. For `get-account-balance` / `close-account`: copy structure from `transfer-money` or `create-account`, adapt domain rules from feature `design.md`.
