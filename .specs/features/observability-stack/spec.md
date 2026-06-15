# Stack de Observabilidade (Infra)

**Módulo:** `infra`  
**Sprint:** 6 — Observabilidade  
**Dependência:** `opentelemetry-instrumentation`

## Problem Statement

Telemetria instrumentada no código precisa de infraestrutura para coleta, armazenamento e visualização. Sem Prometheus, Grafana e Loki configurados, métricas, traces e logs não são acessíveis para monitoramento operacional.

## Goals

- [ ] Deployar stack de observabilidade via Docker Compose (e preparar para Kubernetes)
- [ ] Configurar Prometheus para scrape de métricas da aplicação e infra
- [ ] Configurar Grafana com dashboards base e datasources
- [ ] Configurar Loki para agregação de logs estruturados
- [ ] Configurar OpenTelemetry Collector como ponto central de ingestão

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Alertmanager com PagerDuty/Opsgenie | Alertas básicos apenas — sem integração externa |
| Grafana Cloud / SaaS | Self-hosted na POC |
| Long-term storage (> 30 dias) | Retenção curta para demonstração |
| Jaeger como trace backend separado | Grafana Tempo ou OTEL nativo |

---

## User Stories

### P1: Stack observabilidade via Docker Compose ⭐ MVP

**User Story**: Como desenvolvedor, quero subir a stack de observabilidade com um comando, para visualizar métricas e logs da aplicação localmente.

**Why P1**: Sem infra local, instrumentação OTEL não tem destino.

**Acceptance Criteria**:

1. WHEN desenvolvedor executa `docker compose up observability` THEN o sistema SHALL iniciar: OTEL Collector, Prometheus, Grafana e Loki
2. WHEN aplicação exporta métricas para OTEL Collector THEN Prometheus SHALL coletar via scrape ou remote write
3. WHEN Grafana inicia THEN o sistema SHALL provisionar datasources: Prometheus e Loki automaticamente
4. WHEN desenvolvedor acessa `http://localhost:3000` (Grafana) THEN o login padrão de desenvolvimento SHALL funcionar

**Independent Test**: Subir stack, gerar tráfego na API e verificar métricas no Grafana.

---

### P2: Dashboards base de infraestrutura

**User Story**: Como SRE, quero dashboards pré-configurados para APIs, banco e Kafka, para monitorar saúde do sistema.

**Why P2**: Dashboards prontos aceleram demonstração do portfólio.

**Acceptance Criteria**:

1. WHEN Grafana é provisionado THEN o sistema SHALL incluir dashboard "API Overview" com: request rate, error rate, latência p50/p95/p99
2. WHEN dashboard "Database" é acessado THEN o sistema SHALL exibir: query duration, connection pool usage
3. WHEN dashboard "Kafka" é acessado THEN o sistema SHALL exibir: messages in/out, consumer lag
4. WHEN dashboards são importados THEN o sistema SHALL usar provisioning via arquivos YAML/JSON versionados em `infra/observability/`

**Independent Test**: Acessar cada dashboard após gerar tráfego e verificar painéis com dados.

---

### P3: Correlação logs-traces-métricas no Grafana

**User Story**: Como SRE, quero navegar de métrica para trace e de trace para logs no Grafana, para troubleshooting integrado.

**Why P3**: Experiência completa de observabilidade — diferencial do portfólio.

**Acceptance Criteria**:

1. WHEN traceId está presente em log THEN Grafana SHALL permitir navegação log → trace via derived fields
2. WHEN trace está visualizado THEN Grafana SHALL exibir logs correlacionados do mesmo traceId
3. WHEN métrica de erro spike é clicada THEN o sistema SHALL permitir drill-down para traces do período

---

## Edge Cases

- WHEN porta 3000 (Grafana) já está em uso THEN docker-compose SHALL permitir override via variável de ambiente
- WHEN disco enche com logs do Loki THEN o sistema SHALL aplicar retenção configurada (7 dias default)
- WHEN OTEL Collector reinicia THEN a aplicação SHALL retomar export sem perda crítica (buffer em memória)

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| OBS-10 | P1: Docker Compose stack | Design | Pending |
| OBS-11 | P1: OTEL Collector + Prometheus | Design | Pending |
| OBS-12 | P1: Grafana datasources | Design | Pending |
| OBS-13 | P1: Loki para logs | Design | Pending |
| OBS-14 | P2: Dashboard API | Design | Pending |
| OBS-15 | P2: Dashboard DB e Kafka | Design | Pending |
| OBS-16 | P3: Correlação logs-traces | Design | Pending |
| OBS-17 | Edge: Retenção de logs | Design | Pending |

**Coverage:** 8 total, 0 mapped to tasks, 8 unmapped ⚠️

---

## Success Criteria

- [ ] Stack sobe com um comando e todos os serviços ficam healthy em < 2 minutos
- [ ] Métricas da aplicação visíveis no Grafana após primeira requisição
- [ ] Logs estruturados da aplicação pesquisáveis no Loki via Grafana
- [ ] Configuração versionada em `infra/observability/` sem secrets commitados
