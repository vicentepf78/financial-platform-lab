# Code Conventions

**Source:** `AGENTS.md` (regras mandatórias para IA) + `PROJECT.md`
**Status:** Pré-implementação — convenções definidas; exemplos de código serão adicionados após primeira vertical slice.

## Naming Conventions

**Files:**

- Use cases: `{Verb}{Noun}UseCase.java` — ex: `CreateAccountUseCase.java`
- Controllers: `{Verb}{Noun}Controller.java` — ex: `CreateAccountController.java`
- Tests: `{ClassUnderTest}Test.java` — ex: `CreateAccountUseCaseTest.java`
- Features: kebab-case em diretórios — ex: `features/create-account/`
- Migrations Flyway: `V{version}__{description}.sql`

**Functions/Methods:**

- Testes: `should{ExpectedBehavior}When{Condition}()` — ex: `shouldTransferMoneyWhenOriginAccountHasEnoughBalance()`
- Evitar: `testTransfer()`, `testCreateAccount()`

**Variables:**

- Nomes explícitos em inglês para código — ex: `originAccountId`, `transferAmount`
- Domínio financeiro: `Money`, `LedgerEntry`, `DebitEntry`, `CreditEntry`

**Constants:**

- UPPER_SNAKE_CASE para constantes de domínio e configuração

## Code Organization

**Import/Dependency Declaration:**

- Domain: zero imports de Spring, JPA, Kafka
- Application: imports de domain + ports apenas
- Adapters: imports de ports + frameworks

**File Structure (por feature):**

```text
features/create-account/
├── CreateAccountController.java
├── CreateAccountUseCase.java
├── CreateAccountRequest.java
├── CreateAccountResponse.java
└── CreateAccountUseCaseTest.java
```

**Class size limits:**

- Máximo 200 linhas por classe
- Máximo 30 linhas por método

## Type Safety/Documentation

**Approach:** Java records para DTOs imutáveis; value objects no domain (Money, CPF, CNPJ)
**Documentation:** Cada feature deve conter descrição, critérios de aceite e regras de negócio na spec

## Error Handling

**Pattern:** Problem Details (RFC 9457) para erros REST
**API Response envelope:**

```json
{
  "data": {},
  "metadata": {}
}
```

**Domain:** Exceções de domínio específicas; sem lógica de negócio em exception handlers de framework

## Comments/Documentation

**Style:** Código autoexplicativo; comentários apenas para regras de negócio não óbvias
**ADRs:** Obrigatório para decisões de arquitetura (banco, mensageria, observabilidade, segurança)

## Architectural Rules (Golden Rules)

| Regra | Descrição |
|-------|-----------|
| Rule 1 | Regras de negócio apenas em Domain e Application — nunca em Controllers, Consumers ou Repositories |
| Rule 2 | Toda feature deve ter testes |
| Rule 3 | Nunca alterar saldo diretamente — sempre via ledger entries |
| Rule 4 | Domain sem acoplamento a Spring, JPA, Kafka, HTTP |
| Rule 5 | Composição sobre herança |
| Rule 6 | Classes pequenas (≤200 linhas, métodos ≤30 linhas) |
| Rule 7 | Favor imutabilidade |

## Development Workflow

```text
Spec → Testes → Implementação → Refatoração
```

**Git:** Commits atômicos por tarefa; mensagens focadas no "porquê"

## REST Conventions

- Verbos HTTP: GET, POST, PUT, PATCH, DELETE
- Idempotência obrigatória em: PIX, webhooks, cobranças
- Correlation ID em toda operação financeira para auditabilidade
