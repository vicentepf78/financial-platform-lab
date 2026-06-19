# Project Structure

**Root:** `/home/vicente/Documentos/financial-platform-lab`
**Status:** Sprint 1 em execução — backend com `shared-kernel`, `customer-module`, `account-module` e `application` (jwt-auth, transfer-money) implementados

## Directory Tree (planejada)

```text
financial-platform-lab/
├── .specs/                    # tlc-spec-driven (specs, roadmap, brownfield)
│   ├── project/
│   ├── codebase/
│   └── features/
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
│   └── src/
│       ├── pages/
│       ├── components/
│       ├── hooks/
│       └── api/
├── infra/
│   ├── docker-compose/
│   ├── kubernetes/
│   └── helm/
├── docs/
├── adr/
├── scripts/
├── specs/                     # specs legadas (se aplicável)
├── AGENTS.md
├── PROJECT.md
└── README.md
```

## Estado Atual

```text
financial-platform-lab/
├── .specs/          ✅ specs, roadmap, brownfield index
├── backend/         ✅ shared-kernel, customer-module, account-module, application
├── adr/             ✅ ADRs 0001–0007
├── docs/            ✅ integrações Mercado Pago
├── frontend/        ⏳ scaffold
├── infra/           ⏳ Docker Compose pendente
├── AGENTS.md        ✅
└── PROJECT.md       ✅
```

## Module Organization

### shared-kernel

**Purpose:** Objetos compartilhados entre módulos
**Location:** `backend/shared-kernel/`
**Key artifacts:** Money, CPF, CNPJ, DomainEvent, AggregateRoot, AuditableEntity, Identifier

### customer-module

**Purpose:** Cadastro e gestão de clientes
**Location:** `backend/customer-module/`
**Key features:** create-customer ✅, query-customers ✅, update-customer ✅

### account-module

**Purpose:** Contas bancárias e transferências
**Location:** `backend/account-module/`
**Key features:** create-account ✅, transfer-money ✅, close-account, get-balance, get-statement

### ledger-module

**Purpose:** Fonte da verdade financeira — lançamentos débito/crédito
**Location:** `backend/ledger-module/`
**Key features:** create-ledger-entry, get-ledger-entries, get-account-balance-projection

### pix-module

**Purpose:** Operações PIX
**Location:** `backend/pix-module/`
**Key features:** register-pix-key, send-pix, receive-pix, get-pix-history

### billing-module

**Purpose:** Cobranças e integração Mercado Pago
**Location:** `backend/billing-module/`
**Key features:** create-charge, get-charges, process-webhook

### reconciliation-module

**Purpose:** Conciliação financeira e CNAB
**Location:** `backend/reconciliation-module/`
**Key features:** import-cnab, reconcile, handle-divergence, reprocess

### audit-module

**Purpose:** Rastreabilidade e auditoria
**Location:** `backend/audit-module/`

### monitoring-module

**Purpose:** Métricas e indicadores
**Location:** `backend/monitoring-module/`

### application

**Purpose:** Bootstrap Spring Boot, configuração global, composição de módulos, autenticação JWT cross-cutting
**Location:** `backend/application/`
**Key features:** jwt-auth ✅ (`features/auth/`, `infrastructure/security/`)

## Where Things Live

**Gestão de Clientes:**

- UI: `frontend/src/pages/customers/`
- Business Logic: `backend/customer-module/features/createcustomer/`, `backend/customer-module/features/querycustomers/`, `backend/customer-module/features/updatecustomer/`
- Data Access: `backend/customer-module/adapters/`
- Configuration: `backend/application/`

**Gestão de Contas:**

- UI: `frontend/src/pages/accounts/` _(planejado)_
- Business Logic: `backend/account-module/features/createaccount/`, `backend/account-module/features/transfermoney/`
- Cross-module ports: `CustomerLookupPort`, `LedgerPort`, `EventPublisherPort`
- Data Access: `backend/account-module/adapters/persistence/`
- Events: Kafka topic `account-created`

**Transferências:**

- UI: `frontend/src/pages/transfers/`
- Business Logic: `backend/account-module/features/transfer-money/`
- Ledger: `backend/ledger-module/` (via port)
- Events: Kafka topic `transfer-executed`

**Cobranças:**

- UI: `frontend/src/pages/billing/`
- Business Logic: `backend/billing-module/features/`
- External API: `backend/billing-module/adapters/mercadopago/`
- Webhooks: `backend/billing-module/adapters/webhook/`

**Observabilidade:**

- Instrumentação: `backend/application/` + módulos
- Dashboards: `infra/docker-compose/grafana/`
- Config: `infra/docker-compose/prometheus/`, `loki/`

## Special Directories

**adr/:**

**Purpose:** Architecture Decision Records
**Examples:** ADR-001-arquitetura-modular, ADR-002-postgresql, ADR-003-kafka

**.specs/features/:**

**Purpose:** Especificações por feature com IDs rastreáveis (tlc-spec-driven)
**Examples:** `.specs/features/create-account/spec.md`

**infra/docker-compose/:**

**Purpose:** Ambiente local completo (PostgreSQL, Kafka, Grafana, Prometheus, Loki, Backend, Frontend)
