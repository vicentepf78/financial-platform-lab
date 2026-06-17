# Financial Platform Lab

Plataforma financeira modular (monólito hexagonal) para portfólio técnico — Core Banking, Ledger, PIX, Cobranças e Conciliação.

## Estrutura

```text
financial-platform-lab/
├── backend/          # Java 21 + Spring Boot 3 (Maven multi-módulo)
├── frontend/         # React + TypeScript + MUI
├── infra/            # Docker Compose, Kubernetes, Helm
├── docs/             # Documentação
├── adr/              # Architecture Decision Records
├── .specs/           # Spec-driven development (tlc-spec-driven)
├── scripts/          # Scripts de desenvolvimento
├── AGENTS.md         # Regras para agentes de IA
└── PROJECT.md        # Visão detalhada do projeto
```

## Pré-requisitos

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker e Docker Compose

## Quick Start

### 1. Infraestrutura local

```bash
chmod +x scripts/*.sh
./scripts/dev-up.sh
```

Serviços: PostgreSQL (`localhost:5432`), Kafka (`localhost:9092`), Kafka UI (`http://localhost:8081`).

Observabilidade opcional (Prometheus, Grafana, Loki):

```bash
./scripts/compose.sh --profile observability up -d
```

### 2. Backend

```bash
cd backend
mvn test
mvn spring-boot:run -pl application
```

API: `http://localhost:8080`  
Health: `http://localhost:8080/api/v1/health`  
Actuator: `http://localhost:8080/actuator/health`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

UI: `http://localhost:5173`

## Módulos Backend

| Módulo | Responsabilidade |
|--------|------------------|
| `shared-kernel` | Money, CPF, CNPJ, DomainEvent, AggregateRoot |
| `customer-module` | Clientes |
| `account-module` | Contas e transferências |
| `ledger-module` | Lançamentos financeiros (fonte da verdade) |
| `pix-module` | PIX |
| `billing-module` | Cobranças |
| `reconciliation-module` | Conciliação CNAB |
| `audit-module` | Auditoria |
| `monitoring-module` | Métricas |
| `application` | Bootstrap Spring Boot |

Cada módulo segue hexagonal architecture: `domain`, `application`, `ports`, `adapters`, `infrastructure`, `features`.

## Testes

```bash
# Unit tests
cd backend && mvn test

# Integration tests (requer Docker)
cd backend && mvn verify -Pintegration
```

## Fluxo de Pull Request

O desenvolvimento deve seguir branch por feature: crie uma branch a partir da branch alvo do trabalho, faça commits atômicos por task e abra um PR/MR para a branch de integração combinada pelo time.

PRs disparam a revisão consultiva por IA via GitHub Actions. Configuração e detalhes: [`docs/ai-pr-review.md`](docs/ai-pr-review.md).

## Regras Financeiras

- **Ledger-first:** saldo nunca é alterado diretamente
- Toda operação financeira gera **débito + crédito**
- Regras de negócio apenas em **Domain** e **Application**

Ver [`AGENTS.md`](AGENTS.md) para regras completas.

## Roadmap

Sprint 1 (atual): Core Banking — ver [`.specs/project/ROADMAP.md`](.specs/project/ROADMAP.md).
