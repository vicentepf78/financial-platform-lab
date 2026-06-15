# ADR-0001: Monólito Modular com Arquitetura Hexagonal e Vertical Slice

**Status:** Accepted  
**Data:** 2026-06-15  
**Decisores:** Equipe Financial Platform Lab  
**Referências:** [STATE.md AD-002](../.specs/project/STATE.md), [ARCHITECTURE.md](../.specs/codebase/ARCHITECTURE.md), [AGENTS.md](../AGENTS.md)

## Contexto

O Financial Platform Lab é um projeto de portfólio que deve demonstrar competência em domínio financeiro e engenharia de software para o mercado brasileiro. A v1 precisa cobrir Core Banking, Ledger, PIX, Cobranças e Conciliação em um único deploy operável via Docker Compose, com evolução futura para Kubernetes.

Microserviços na v1 aumentariam complexidade operacional (rede, consistência distribuída, observabilidade cross-service) sem benefício proporcional para um time pequeno e desenvolvimento assistido por IA. É necessário, porém, manter limites claros entre capacidades de negócio para permitir extração gradual no futuro.

A arquitetura deve isolar regras de domínio de frameworks (Spring, JPA, Kafka, HTTP), organizar funcionalidades de ponta a ponta e seguir DDD Light com módulos de negócio bem definidos.

## Decisão

Adotar **Monorepo + Modular Monolith + Arquitetura Hexagonal + Vertical Slice Architecture + DDD Light** como padrão arquitetural da v1.

### Estrutura do monorepo

```text
financial-platform-lab/
├── backend/
│   ├── shared-kernel/
│   ├── customer-module/
│   ├── account-module/
│   ├── ledger-module/
│   ├── pix-module/
│   ├── billing-module/
│   ├── reconciliation-module/
│   ├── audit-module/
│   ├── monitoring-module/
│   └── application/
├── frontend/
├── infra/
├── docs/
├── adr/
└── .specs/
```

### Estrutura por módulo

Todo módulo de negócio segue:

```text
module/
├── domain/          # Entidades, VOs, regras — sem dependência de framework
├── application/     # Use cases, orquestração
├── ports/           # Interfaces inbound/outbound
├── adapters/        # Controllers, consumers, repositórios
└── infrastructure/  # Configuração Spring, beans
```

### Estrutura por feature (vertical slice)

Dentro de cada módulo, cada capacidade de negócio fica isolada em `features/`:

```text
features/create-account/
├── CreateAccountController
├── CreateAccountUseCase
├── CreateAccountRequest
├── CreateAccountResponse
└── CreateAccountTest
```

### Dependências permitidas

```text
Controller → UseCase
UseCase → Domain | Ports
Adapters → Ports
Infrastructure → Adapters
```

### Dependências proibidas

```text
Domain → Controller | Repository Impl | Spring | Kafka | REST
Controller → Domain (direto, sem UseCase)
```

Regras de negócio residem exclusivamente em **Domain** e **Application Layer** (AGENTS.md Rule 1).

## Consequências

### Positivas

- Menor complexidade operacional na v1; deploy único via Docker Compose.
- Limites lógicos explícitos entre módulos facilitam evolução para microserviços.
- Hexagonal Architecture protege o domínio financeiro de acoplamento com Spring/JPA/Kafka.
- Vertical Slice melhora coesão: cada feature contém controller, use case e testes juntos.
- Produtividade elevada com desenvolvimento assistido por IA em codebase coesa.

### Negativas

- Escala horizontal limitada por módulo até eventual extração.
- Disciplina rigorosa necessária para não violar limites entre módulos.
- Monorepo pode crescer em tamanho; exige convenções claras de organização.

### Neutras

- Microserviços ficam explicitamente fora do escopo da v1 (ver PROJECT.md).
- Primeira vertical slice planejada em `account-module/features/create-account/`.
- Módulos de negócio: `shared-kernel`, `customer`, `account`, `ledger`, `pix`, `billing`, `reconciliation`, `audit`, `monitoring`.
