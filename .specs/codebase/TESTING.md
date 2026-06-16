# Testing Infrastructure

**Status:** Parcialmente implementado — `customer-module` e `account-module` com testes unitários e integração (referência em `INDEX.md`). Demais módulos conforme roadmap.

## Test Frameworks

**Unit/Integration:** JUnit 5, Mockito, AssertJ, Spring Boot Test, Testcontainers
**E2E:** Playwright
**Coverage:** Meta mínima de 80% em testes unitários

## Test Organization

**Location (planejada):**

- Unit/Integration: co-localizados com a feature — `backend/{module}/features/{feature}/{Feature}Test.java`
- Integration (infra): `backend/{module}/src/test/java/` ou pasta `integration/` por módulo
- E2E: `frontend/e2e/` ou `e2e/` na raiz do monorepo

**Naming:** `{ClassUnderTest}Test.java` com métodos `should{Behavior}When{Condition}()`

**Structure:** Testes por vertical slice — cada feature traz seu próprio teste junto ao use case

## Testing Patterns

### Unit Tests

**Approach:** Testar domain rules e use cases isolados com mocks de ports
**Location:** Ao lado do use case na pasta da feature
**Pattern:**

```java
@ExtendWith(MockitoExtension.class)
class CreateAccountUseCaseTest {
    @Mock private AccountRepositoryPort accountRepository;
    @Mock private EventPublisherPort eventPublisher;
    @InjectMocks private CreateAccountUseCase useCase;

    @Test
    void shouldCreateAccountWhenCustomerExists() { ... }
}
```

### Integration Tests

**Approach:** Testcontainers para PostgreSQL e Kafka — containers obrigatórios
**Location:** `backend/{module}/src/test/java/.../integration/` ou sufixo `IntegrationTest`
**Pattern:** `@SpringBootTest` + `@Testcontainers` com containers reais

**Containers obrigatórios:**

| Container | Uso |
|-----------|-----|
| PostgreSQL | Repositórios JPA, migrations Flyway |
| Kafka | Producers/consumers de domain events |

### Contract Tests

**Approach:** Validar contratos de APIs públicas e integrações externas
**Location:** `backend/{module}/src/test/java/.../contract/`
**Targets:** Mercado Pago, Kobana (futuro), Banco do Brasil (futuro)

### E2E Tests

**Approach:** Playwright para fluxos completos no backoffice
**Location:** `e2e/` na raiz
**Flows planejados:**

| Fluxo | Sprint |
|-------|--------|
| Cadastro de cliente | Sprint 1 | ✅ `create-customer` |
| Abertura de conta | Sprint 1 | ✅ `create-account` |
| Transferência | Sprint 1-2 | Planejado |
| PIX | Sprint 4 |
| Cobrança | Sprint 3 |

## Test Execution

**Commands (planejados — a confirmar no scaffold Maven/npm):**

| Comando | Escopo |
|---------|--------|
| `mvn test` | Unit tests backend |
| `mvn verify -Pintegration` | Unit + integration (Testcontainers) |
| `npx playwright test` | E2E frontend |

**Configuration:** Perfil Maven `integration` para Testcontainers; Docker required para integration e E2E

## Coverage Targets

**Current:** `customer-module` e `account-module` com cobertura por vertical slice
**Goals:** 80% mínimo em testes unitários (domain + use cases)
**Enforcement:** A configurar no CI (GitHub Actions)

## Test Coverage Matrix

| Code Layer | Required Test Type | Location Pattern | Run Command |
| ---------- | ------------------ | ---------------- | ----------- |
| Domain (entities, VOs) | Unit | `backend/*/domain/**/*Test.java` | `mvn test` |
| Use Cases (application) | Unit | `backend/*/features/**/*UseCaseTest.java` | `mvn test` |
| Controllers (adapters) | Integration | `backend/*/features/**/*Controller*Test.java` | `mvn verify -Pintegration` |
| Repositories (adapters) | Integration | `backend/*/integration/**/*Repository*Test.java` | `mvn verify -Pintegration` |
| Kafka consumers/producers | Integration | `backend/*/integration/**/*Kafka*Test.java` | `mvn verify -Pintegration` |
| Flyway migrations | Integration | `backend/application/integration/*Migration*Test.java` | `mvn verify -Pintegration` |
| External APIs (Mercado Pago) | Contract | `backend/billing-module/contract/**/*Test.java` | `mvn verify -Pcontract` |
| Frontend components | Unit (opcional v1) | `frontend/src/**/*.test.tsx` | `npm test` |
| E2E flows | E2E | `e2e/**/*.spec.ts` | `npx playwright test` |

## Parallelism Assessment

| Test Type | Parallel-Safe? | Isolation Model | Evidence |
| --------- | -------------- | --------------- | -------- |
| Unit (domain/use case) | Yes | Mocks isolados, sem estado compartilhado | Padrão Mockito |
| Integration (Testcontainers) | Yes* | Container por JVM/test class via Testcontainers | Padrão Spring Boot Testcontainers |
| E2E (Playwright) | No | Browser compartilhado, dados de teste no DB | Executar sequencialmente ou com workers=1 |

\* Requer que cada test class use seu próprio schema/database ou limpeza por transação rollback.

## Gate Check Commands

| Gate Level | When to Use | Command |
| ---------- | ----------- | ------- |
| Quick | Após tasks com unit tests only | `mvn test -pl {module}` |
| Full | Após tasks com integration tests | `mvn verify -Pintegration -pl {module}` |
| Build | Após conclusão de sprint/fase | `mvn verify -Pintegration` |

## Test Co-location Workflow

Alinhado ao `tlc-spec-driven`. Por task em `tasks.md`:

1. **Implementar código + testes na mesma task** (camada definida na Coverage Matrix abaixo).
2. **Gate check** — comando Maven da task; exit code não-zero = parar e corrigir.
3. **Commit atômico** por task.

Não há ritual TDD obrigatório (red/green). O gate é o veredito. Detalhes: `.rules/testing.md`.

Nenhuma feature é considerada completa sem testes nas camadas exigidas pela matriz.
