# Roadmap

**Current Milestone:** Sprint 1 — Core Banking
**Status:** Spec Complete — Ready for Execute

> **Spec-driven:** 30 features em `.specs/features/` · 7 ADRs em `adr/` · Sprint 1 com 54 tarefas atômicas

---

## Sprint 1 — Core Banking

**Goal:** Fluxo básico de cadastro e movimentação entre contas
**Target:** Cliente criado, conta aberta e transferência executada com rastreabilidade

### Features

**Gestão de Clientes** - SPECIFIED → [create-customer](../features/create-customer/spec.md), [query-customers](../features/query-customers/spec.md), [update-customer](../features/update-customer/spec.md)

- Cadastro de clientes (CPF/CNPJ)
- Consulta de clientes
- Atualização cadastral

**Gestão de Contas** - SPECIFIED → [create-account](../features/create-account/spec.md), [close-account](../features/close-account/spec.md), [get-account-balance](../features/get-account-balance/spec.md)

- Criação de conta
- Encerramento de conta
- Consulta de saldo (projeção)
- Consulta de movimentações _(Sprint 2 — financial-statement)_

**Transferências** - SPECIFIED → [transfer-money](../features/transfer-money/spec.md)

- Transferência entre contas (origem, destino, valor)
- Validação de saldo suficiente
- Publicação de evento TransferExecuted

**Shared Kernel** - SPECIFIED (scaffold) → [shared-kernel](../features/shared-kernel/spec.md)

- Money, CPF, CNPJ, Identifier
- AggregateRoot, DomainEvent, AuditableEntity

---

## Sprint 2 — Ledger

**Goal:** Ledger como fonte oficial da verdade financeira
**Target:** Toda movimentação gera débito + crédito; saldo derivado de lançamentos

### Features

**Lançamentos Financeiros** - SPECIFIED → [register-ledger-entry](../features/register-ledger-entry/spec.md), [calculate-balance](../features/calculate-balance/spec.md)

- Registro de débito e crédito (partidas dobradas)
- Cálculo de saldo a partir de lançamentos
- Publicação de evento LedgerEntryCreated

**Extrato Financeiro** - SPECIFIED → [financial-statement](../features/financial-statement/spec.md)

- Histórico de movimentações
- Filtros por período e tipo
- Exportação

**Ledger Financeiro (UI)** - SPECIFIED → [ledger-ui](../features/ledger-ui/spec.md)

- Visualização de débitos e créditos
- Razão financeira

---

## Sprint 3 — Cobranças

**Goal:** Cobranças com integração Mercado Pago
**Target:** Cobrança criada, QR Code gerado, webhook recebido e cobrança liquidada

### Features

**Cobranças** - SPECIFIED → [create-charge](../features/create-charge/spec.md)

- Criação de cobrança
- Geração de QR Code PIX
- Consulta de cobranças pendentes e liquidadas
- Publicação de eventos ChargeCreated e ChargePaid

**Integração Mercado Pago** - SPECIFIED → [mercadopago-integration](../features/mercadopago-integration/spec.md), [process-payment-webhook](../features/process-payment-webhook/spec.md)

- Orders API (`POST /v1/orders`, `type: online`, PIX)
- Webhooks topic `order` (não usar `payment` legacy)
- Idempotência: `X-Idempotency-Key` + webhook events
- Doc: [`docs/integrations/mercadopago/`](../../docs/integrations/mercadopago/README.md)

**Cobranças (UI)** - SPECIFIED → [billing-ui](../features/billing-ui/spec.md)

- Listagem com status
- Visualização de QR Code
- Histórico de pagamentos

---

## Sprint 4 — PIX

**Goal:** Operações PIX internas com base para integração futura com Banco do Brasil
**Target:** Chaves cadastradas, transferência PIX executada e recebimento registrado

### Features

**Chaves PIX** - SPECIFIED → [register-pix-key](../features/register-pix-key/spec.md)

- Cadastro de chaves
- Consulta de chaves

**Transferência PIX** - SPECIFIED → [send-pix](../features/send-pix/spec.md)

- Envio de PIX
- Consulta de status
- Publicação de evento PixSent

**Recebimento PIX** - SPECIFIED → [receive-pix](../features/receive-pix/spec.md)

- Registro de PIX recebido
- Publicação de evento PixReceived

**PIX (UI)** - SPECIFIED → [pix-ui](../features/pix-ui/spec.md)

- Gestão de chaves
- Histórico de transferências

---

## Sprint 5 — Conciliação

**Goal:** Conciliação financeira com importação CNAB
**Target:** Arquivo CNAB importado, divergências identificadas e reprocessamento disponível

### Features

**Importação CNAB** - SPECIFIED → [import-cnab](../features/import-cnab/spec.md)

- Upload e parsing de arquivo CNAB
- Mapeamento para lançamentos do ledger

**Conciliação** - SPECIFIED → [reconcile-transactions](../features/reconcile-transactions/spec.md)

- Comparação automática
- Identificação de divergências
- Reprocessamento
- Publicação de evento ReconciliationExecuted

**Conciliação (UI)** - SPECIFIED → [reconciliation-ui](../features/reconciliation-ui/spec.md)

- Upload de arquivo
- Listagem de divergências
- Ação de reprocessamento

---

## Sprint 6 — Observabilidade

**Goal:** Visibilidade operacional completa do sistema
**Target:** Métricas de throughput, latência, erros e retries instrumentadas e visualizáveis

### Features

**Instrumentação** - SPECIFIED → [opentelemetry-instrumentation](../features/opentelemetry-instrumentation/spec.md)

- OpenTelemetry em APIs, banco e Kafka
- Métricas: throughput, latência, erros, retries

**Stack de Observabilidade** - SPECIFIED → [observability-stack](../features/observability-stack/spec.md)

- Prometheus para coleta
- Grafana para dashboards
- Loki para logs

**Monitoramento (UI)** - SPECIFIED → [monitoring-dashboard](../features/monitoring-dashboard/spec.md)

- Status de APIs, Kafka e banco
- Dashboard financeiro com indicadores (saldo total, volume, PIX hoje, cobranças hoje, clientes ativos)

**Auditoria** - SPECIFIED → [audit-trail](../features/audit-trail/spec.md)

- Registro de ações com usuário, operação, data e resultado
- Tela de auditoria no backoffice

---

## Sprint 7 — Kubernetes

**Goal:** Deploy cloud-native com escalabilidade
**Target:** Aplicação rodando em Kubernetes com rolling update e HPA

### Features

**Deploy Kubernetes** - SPECIFIED → [kubernetes-deploy](../features/kubernetes-deploy/spec.md)

- Manifests/Helm charts para backend, frontend e dependências
- Rolling update
- Horizontal Pod Autoscaler (HPA)

**Alta Disponibilidade** - SPECIFIED → [high-availability](../features/high-availability/spec.md)

- Configuração de réplicas
- Health checks e readiness probes

---

## Future Considerations

- Integração Kobana (boletos, CNAB, retornos) — Fase 2
- Integração Banco do Brasil (PIX, cobranças PIX, webhooks) — Fase 3
- Evolução para microserviços a partir do monólito modular
- Contract tests para integrações externas (Mercado Pago, Kobana, Banco do Brasil)
- Testes E2E Playwright: cadastro, transferência, PIX, cobrança
