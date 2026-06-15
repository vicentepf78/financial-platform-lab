# Project Structure

**Root:** `/home/vicente/Documentos/financial-platform-lab`
**Status:** Pré-implementação — apenas documentação presente (`PROJECT.md`, `AGENTS.md`, `.specs/`)

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
├── .specs/          ✅ criado
├── AGENTS.md        ✅ existe
└── PROJECT.md       ✅ existe
```

## Module Organization

### shared-kernel

**Purpose:** Objetos compartilhados entre módulos
**Location:** `backend/shared-kernel/`
**Key artifacts:** Money, CPF, CNPJ, DomainEvent, AggregateRoot, AuditableEntity, Identifier

### customer-module

**Purpose:** Cadastro e gestão de clientes
**Location:** `backend/customer-module/`
**Key features:** create-customer, get-customer, update-customer

### account-module

**Purpose:** Contas bancárias e transferências
**Location:** `backend/account-module/`
**Key features:** create-account, close-account, transfer-money, get-balance, get-statement

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

**Purpose:** Bootstrap Spring Boot, configuração global, composição de módulos
**Location:** `backend/application/`

## Where Things Live

**Gestão de Clientes:**

- UI: `frontend/src/pages/customers/`
- Business Logic: `backend/customer-module/features/`
- Data Access: `backend/customer-module/adapters/`
- Configuration: `backend/application/`

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
