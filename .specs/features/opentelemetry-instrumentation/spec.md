# Instrumentação OpenTelemetry

**Módulo:** `monitoring-module`  
**Sprint:** 6 — Observabilidade

## Problem Statement

Sem instrumentação padronizada, é impossível medir throughput, latência, erros e retries das APIs, operações de banco e consumidores Kafka. A plataforma precisa de observabilidade desde o código para demonstrar maturidade operacional no portfólio.

## Goals

- [ ] Instrumentar APIs REST com traces, métricas e logs estruturados via OpenTelemetry
- [ ] Instrumentar operações PostgreSQL (latência de queries)
- [ ] Instrumentar produtores e consumidores Kafka (throughput, lag, erros)
- [ ] Exportar telemetria para collector OTLP
- [ ] Correlacionar traces com `correlationId` de operações financeiras

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Instrumentação de frontend (RUM) | Foco em backend na v1 |
| Custom metrics de negócio no código | Dashboard financeiro — feature separada |
| Sampling adaptativo avançado | Configuração fixa na v1 |
| Service mesh (Istio/Linkerd) | Sprint 7 — infra Kubernetes |

---

## User Stories

### P1: Traces e métricas em APIs REST ⭐ MVP

**User Story**: Como SRE, quero traces e métricas automáticos em todas as APIs REST, para diagnosticar latência e erros.

**Why P1**: APIs são o ponto de entrada — sem instrumentação aqui, observabilidade é incompleta.

**Acceptance Criteria**:

1. WHEN qualquer endpoint REST é invocado THEN o sistema SHALL criar span OpenTelemetry com atributos: http.method, http.route, http.status_code
2. WHEN endpoint pertence a operação financeira THEN o span SHALL incluir `correlationId` e `operationType` como atributos
3. WHEN requisição completa THEN o sistema SHALL registrar métricas: `http.server.duration` (histogram), `http.server.requests` (counter) por rota e status
4. WHEN erro 5xx ocorre THEN o span SHALL ser marcado como ERROR com stack trace

**Independent Test**: Chamar endpoint e verificar span exportado no collector OTLP.

---

### P2: Instrumentação de PostgreSQL e Kafka

**User Story**: Como SRE, quero métricas de banco de dados e Kafka, para identificar gargalos de infraestrutura.

**Why P2**: Operações financeiras dependem criticamente de DB e mensageria.

**Acceptance Criteria**:

1. WHEN query JPA é executada THEN o sistema SHALL criar span filho com `db.system=postgresql`, `db.statement` (sanitizado) e duração
2. WHEN mensagem Kafka é publicada THEN o sistema SHALL criar span com `messaging.system=kafka`, topic e partition
3. WHEN consumer Kafka processa mensagem THEN o sistema SHALL criar span com duração de processamento e resultado (success/error)
4. WHEN métricas são coletadas THEN o sistema SHALL expor: `db.client.duration`, `messaging.publish.duration`, `messaging.process.duration`

**Independent Test**: Executar transferência (DB + Kafka) e verificar spans de todas as camadas.

---

### P3: Logs estruturados correlacionados

**User Story**: Como SRE, quero logs estruturados com traceId e correlationId, para correlacionar logs com traces no Grafana/Loki.

**Why P3**: Correlação logs↔traces é essencial para troubleshooting.

**Acceptance Criteria**:

1. WHEN log é emitido durante requisição THEN o sistema SHALL incluir `traceId`, `spanId` e `correlationId` no MDC
2. WHEN log é de operação financeira THEN o sistema SHALL incluir `accountId` e `operationType` (sem dados sensíveis)
3. WHEN formato de log é JSON THEN campos SHALL ser parseáveis pelo Loki

---

## Edge Cases

- WHEN collector OTLP está indisponível THEN a aplicação SHALL continuar operando (export assíncrono com buffer e drop graceful)
- WHEN operação gera span muito longo (> 100 spans filhos) THEN o sistema SHALL respeitar limites de atributos OTEL
- WHEN dados sensíveis (CPF, token) aparecem em atributos THEN o sistema SHALL sanitizar antes de exportar

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| OBS-01 | P1: Spans em APIs REST | Design | Pending |
| OBS-02 | P1: Métricas HTTP | Design | Pending |
| OBS-03 | P1: CorrelationId em spans | Design | Pending |
| OBS-04 | P2: Spans PostgreSQL | Design | Pending |
| OBS-05 | P2: Spans Kafka | Design | Pending |
| OBS-06 | P2: Métricas DB e messaging | Design | Pending |
| OBS-07 | P3: Logs estruturados | Design | Pending |
| OBS-08 | Edge: Sanitização de dados | Design | Pending |
| OBS-09 | Edge: Collector indisponível | Design | Pending |

**Coverage:** 9 total, 0 mapped to tasks, 9 unmapped ⚠️

---

## Success Criteria

- [ ] 100% dos endpoints REST geram spans visíveis no Grafana Tempo/Jaeger
- [ ] Transferência financeira completa gera trace com spans de API → UseCase → DB → Kafka
- [ ] Métricas de latência p50/p95/p99 disponíveis no Prometheus
- [ ] Nenhum dado sensível (CPF completo, tokens) em traces ou logs
