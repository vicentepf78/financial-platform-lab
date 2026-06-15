# Dashboard de Monitoramento (UI)

**Módulo:** `frontend`  
**Sprint:** 6 — Observabilidade  
**Dependências:** `opentelemetry-instrumentation`, `observability-stack`

## Problem Statement

Operadores e gestores precisam de uma visão consolidada da saúde do sistema e indicadores financeiros no backoffice, sem depender exclusivamente do Grafana. O dashboard interno demonstra integração de métricas de negócio com observabilidade técnica.

## Goals

- [ ] Exibir status de APIs, Kafka e PostgreSQL
- [ ] Exibir indicadores financeiros: saldo total, volume transacionado, PIX hoje, cobranças hoje, clientes ativos
- [ ] Consumir métricas via API interna (agregação backend) e links para Grafana
- [ ] Atualização periódica automática dos indicadores

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Substituição completa do Grafana | Grafana continua para troubleshooting técnico profundo |
| Alertas configuráveis na UI | Alertmanager/Grafana alerting |
| Gráficos históricos interativos complexos | Cards e números na v1; Grafana para séries temporais |
| Métricas de negócio em tempo real (< 1s) | Refresh a cada 30s é suficiente para POC |

---

## User Stories

### P1: Indicadores financeiros no dashboard ⭐ MVP

**User Story**: Como gestor, quero visualizar indicadores financeiros consolidados no dashboard, para ter visão rápida do negócio.

**Why P1**: Principal valor de negócio do dashboard — diferencial do portfólio.

**Acceptance Criteria**:

1. WHEN operador acessa `/dashboard` THEN a UI SHALL exibir cards: saldo total (soma de contas ativas), volume transacionado (hoje), PIX enviados/recebidos (hoje), cobranças criadas/liquidadas (hoje), clientes ativos
2. WHEN dados são carregados THEN a UI SHALL chamar `GET /api/v1/monitoring/financial-summary`
3. WHEN API retorna dados THEN a UI SHALL formatar valores monetários em BRL (R$ X.XXX,XX)
4. WHEN dados estão carregando THEN a UI SHALL exibir skeleton nos cards

**Independent Test**: Acessar dashboard com dados de teste e verificar cards populados.

---

### P2: Status de infraestrutura

**User Story**: Como operador, quero ver o status de APIs, Kafka e banco de dados no dashboard, para identificar problemas rapidamente.

**Why P2**: Visibilidade operacional complementar aos indicadores financeiros.

**Acceptance Criteria**:

1. WHEN operador visualiza seção "Infraestrutura" THEN a UI SHALL exibir status: API (UP/DOWN), PostgreSQL (UP/DOWN), Kafka (UP/DOWN) via `GET /api/v1/monitoring/health`
2. WHEN componente está DOWN THEN a UI SHALL exibir indicador vermelho com timestamp da última verificação
3. WHEN operador clica "Ver no Grafana" THEN a UI SHALL abrir dashboard Grafana em nova aba
4. WHEN health check falha THEN a UI SHALL exibir banner de alerta no topo

**Independent Test**: Parar Kafka e verificar indicador DOWN no dashboard.

---

### P3: Atualização automática e tendências

**User Story**: Como gestor, quero que o dashboard atualize automaticamente e mostre tendência simples (vs ontem), para acompanhar evolução.

**Why P3**: Melhora experiência; refresh manual cobre MVP.

**Acceptance Criteria**:

1. WHEN dashboard está aberto THEN a UI SHALL refazer consultas a cada 30 segundos
2. WHEN indicador possui valor de ontem THEN a UI SHALL exibir variação percentual com seta (↑/↓)
3. WHEN operador clica em card THEN a UI SHALL navegar para tela de detalhe correspondente (ex: PIX → /pix/history)

---

## Edge Cases

- WHEN API de métricas está indisponível THEN a UI SHALL exibir cards com estado de erro e retry
- WHEN não há transações no dia THEN indicadores SHALL exibir zero formatado (não vazio)
- WHEN valores monetários são muito grandes THEN a UI SHALL usar abreviação (ex: R$ 1,2M)

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| OBS-18 | P1: Cards financeiros | Design | Pending |
| OBS-19 | P1: API financial-summary | Design | Pending |
| OBS-20 | P1: Formatação BRL | Design | Pending |
| OBS-21 | P2: Status infraestrutura | Design | Pending |
| OBS-22 | P2: Link Grafana | Design | Pending |
| OBS-23 | P3: Auto-refresh 30s | Design | Pending |
| OBS-24 | P3: Variação vs ontem | Design | Pending |
| OBS-25 | Edge: Estado de erro | Design | Pending |

**Coverage:** 8 total, 0 mapped to tasks, 8 unmapped ⚠️

---

## Success Criteria

- [ ] Dashboard carrega todos os indicadores em < 3 segundos
- [ ] Indicadores financeiros consistentes com dados do ledger e módulos de negócio
- [ ] Status de infraestrutura reflete estado real dos componentes
- [ ] Interface integrada ao layout do backoffice (Material UI)
