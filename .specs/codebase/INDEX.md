# Codebase Index

**Updated:** 2026-06-15  
**Purpose:** Índice vivo do que está implementado. Agentes devem consultar este arquivo em vez de explorar o repositório quando `tasks.md` aponta padrões existentes.

---

## Modules

| Module | Status | Features |
| ------ | ------ | -------- |
| `shared-kernel` | ✅ Implemented | Money, Cpf, Cnpj, Identifier, AggregateRoot, AuditableEntity, DomainEvent |
| `customer-module` | ✅ Implemented | create-customer |
| `account-module` | ⏳ Scaffold only | create-account (spec ready) |
| `application` | ✅ Partial | Flyway V1 init, V2 customers, health, smoke tests |

---

## Reference slice: create-customer

Use como padrão para novas vertical slices (ex.: create-account).

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

### Tests (reference)

| Type | Path |
| ---- | ---- |
| Unit (domain) | `backend/customer-module/src/test/java/.../domain/*Test.java` |
| Unit (use case) | `backend/customer-module/src/test/java/.../features/createcustomer/CreateCustomerUseCaseTest.java` |
| Integration (repository) | `backend/customer-module/src/test/java/.../adapters/persistence/JpaCustomerRepositoryIntegrationTest.java` |
| Integration (controller) | `backend/customer-module/src/test/java/.../features/createcustomer/CreateCustomerControllerIntegrationTest.java` |
| IT base class | `backend/customer-module/src/test/java/.../support/AbstractCustomerIntegrationTest.java` |
| Test application | `backend/customer-module/src/test/java/.../support/CustomerModuleTestApplication.java` |

### Migrations

| Version | Path |
| ------- | ---- |
| V2 customers | `backend/application/src/main/resources/db/migration/V2__customers.sql` |
| Test copy | `backend/customer-module/src/test/resources/db/migration/` |

### API

- `POST /api/v1/customers` — envelope `{ data, metadata }`, Problem Details em erros

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
| docker-java API 1.44 | `backend/customer-module/src/test/resources/docker-java.properties` |
| Integration profile | `mvn verify -Pintegration` |
| Unit only | `mvn test -pl {module}` |

---

## Do not re-explore

When implementing a feature whose `tasks.md` lists **Reuses** pointing here:

1. Read only the listed reference files.
2. Do not spawn exploration subagents for patterns already indexed above.
3. For create-account: copy structure from create-customer, adapt domain rules from `create-account/design.md`.
