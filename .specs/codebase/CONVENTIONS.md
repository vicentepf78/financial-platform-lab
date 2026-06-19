# Code Conventions

**Source:** `AGENTS.md` (regras mandatórias para IA) e `PROJECT.md`
**Status:** Sprint 1 em execução — convenções definidas; exemplos implementados estão indexados em `INDEX.md`.

## Naming Conventions

**Files:**

- Use cases: `{Verb}{Noun}UseCase.java` — ex: `CreateAccountUseCase.java`
- Controllers: `{Verb}{Noun}Controller.java` — ex: `CreateAccountController.java`
- Tests: `{ClassUnderTest}Test.java` — ex: `CreateAccountUseCaseTest.java`
- Features: lowercase em package Java — ex: `features/createaccount/`; kebab-case apenas nos diretórios de planejamento `.specs/features/create-account/`
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

**Dependency boundaries:** ver `AGENTS.md`.

**File Structure (por feature):**

```text
features/createaccount/
├── CreateAccountController.java
├── CreateAccountUseCase.java
├── CreateAccountRequest.java
├── CreateAccountResponse.java
└── CreateAccountUseCaseTest.java
```

**Class size limits:** ver `AGENTS.md`.

## Type Safety/Documentation

**Approach:** Java records para DTOs imutáveis; value objects no domain (Money, CPF, CNPJ)
**Documentation:** Cada feature deve conter descrição, critérios de aceite e regras de negócio na spec

## Error Handling

**REST:** envelope e Problem Details ficam em `AGENTS.md`.
**Domain:** Exceções de domínio específicas; sem lógica de negócio em exception handlers de framework

## Comments/Documentation

**Style:** Código autoexplicativo; comentários apenas para regras de negócio não óbvias
**ADRs:** Obrigatório para decisões de arquitetura (banco, mensageria, observabilidade, segurança)

## Canonical Rule References

Este arquivo registra convenções de nomenclatura e organização. As regras mandatórias não devem ser duplicadas aqui:

- Golden Rules, arquitetura, financeiro, REST, banco e eventos: `AGENTS.md`
- Testes e gates detalhados: `AGENTS.md` e `.specs/codebase/TESTING.md`

## Development Workflow

Fluxo canônico: `AGENTS.md` → Spec-Driven Development.

**Git:** Branch por feature a partir da branch alvo; commits atômicos por tarefa; mensagens focadas no "porquê"; merge via PR/MR para a branch de integração acordada.

**AI review:** PRs disparam revisão consultiva por IA no GitHub Actions. Detalhes: `docs/ai-pr-review.md`.

## REST Conventions

Ver `AGENTS.md` para contrato REST, idempotência e auditabilidade em operações financeiras.
