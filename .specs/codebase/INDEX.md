# Codebase Index

**Updated:** 2026-06-15  
**Purpose:** Índice vivo do que está implementado. Agentes devem consultar este arquivo em vez de explorar o repositório quando `tasks.md` aponta padrões existentes.

---

## Modules

| Module | Status | Features |
| ------ | ------ | -------- |
| `shared-kernel` | ✅ Implemented | Money, Cpf, Cnpj, Identifier, AggregateRoot, AuditableEntity, DomainEvent |
| `customer-module` | ✅ Implemented | create-customer, query-customers (T1–T4, T5) |
| `account-module` | ✅ Implemented | create-account |
| `application` | ✅ Partial | Flyway V1–V3, health, module wiring smoke tests |

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

## Reference slice: query-customers (foundation)

Use como padrão para a vertical slice de consulta no `customer-module`.

| Layer | Path |
| ----- | ---- |
| Feature slice | `backend/customer-module/src/main/java/.../features/querycustomers/` |
| Use cases | `.../QueryCustomersUseCase.java`, `GetCustomerByIdUseCase.java` |
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
| Integration (query adapter) | `backend/customer-module/src/test/java/.../adapters/persistence/JpaCustomerQueryAdapterIntegrationTest.java` |
| Unit (use case) | `backend/customer-module/src/test/java/.../features/querycustomers/QueryCustomersUseCaseTest.java`, `GetCustomerByIdUseCaseTest.java` |
| Query response DTOs | `backend/customer-module/src/main/java/.../features/querycustomers/` (`QueryCustomersResponse`, `GetCustomerByIdResponse`, `PaginationMetadata`) |

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
| Integration (account) | `mvn verify -Pintegration -pl account-module` |

---

## Do not re-explore

When implementing a feature whose `tasks.md` lists **Reuses** pointing here:

1. Read only the listed reference files.
2. Do not spawn exploration subagents for patterns already indexed above.
3. For `transfer-money`: copy structure from `create-account`, adapt domain rules from `transfer-money/design.md`.
