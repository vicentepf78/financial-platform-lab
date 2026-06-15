# ADR-0004: Stack de Observabilidade

**Status:** Accepted  
**Data:** 2026-06-15  
**Decisores:** Equipe Financial Platform Lab  
**Referências:** [PROJECT.md](../.specs/project/PROJECT.md), [ARCHITECTURE.md](../.specs/codebase/ARCHITECTURE.md)

## Contexto

Uma plataforma financeira de portfólio deve demonstrar operabilidade em produção: capacidade de diagnosticar falhas, medir performance de APIs e integrações, e correlacionar requisições entre componentes. A v1 inclui backoffice com dashboard de monitoramento e ambiente Docker Compose completo.

É necessário instrumentar backend (REST, banco, Kafka), expor health checks para orquestradores e definir métricas mínimas para validar SLIs em ambiente de demonstração.

## Decisão

Adotar a seguinte stack de observabilidade:

### Instrumentação

- **OpenTelemetry** para traces, métricas e logs estruturados no backend Spring Boot.
- Propagação de **correlation-id** em headers HTTP e contexto de spans para rastreabilidade end-to-end.
- Exportação OTLP para coletor/agente no ambiente Docker Compose.

### Coleta e visualização

| Componente | Função |
|------------|--------|
| **Prometheus** | Scraping de métricas (Spring Actuator, exporters) |
| **Grafana** | Dashboards de API, banco, Kafka e negócio |
| **Loki** | Agregação e consulta de logs estruturados |

### Spring Actuator

- Endpoints de **health** (`/actuator/health`) para liveness/readiness em Docker e Kubernetes.
- Endpoint de **métricas** (`/actuator/prometheus`) para scraping pelo Prometheus.
- Health checks de dependências: PostgreSQL, Kafka.

### Métricas obrigatórias (mínimo v1)

| Categoria | Métricas |
|-----------|----------|
| Throughput | Requisições/s por endpoint, mensagens/s por tópico Kafka |
| Latência | p50, p95, p99 de endpoints REST e queries críticas |
| Erros | Taxa de 4xx/5xx, falhas de integração (Mercado Pago, webhooks) |
| Retries | Tentativas de retry em consumers Kafka e chamadas HTTP externas |

### Módulo de monitoramento

- `monitoring-module` consome métricas da infraestrutura de observability.
- Dashboard no backoffice React exibe indicadores agregados para demonstração de portfólio.

## Consequências

### Positivas

- Demonstra competência em operabilidade — critério de sucesso da POC (visualizar métricas).
- Diagnóstico rápido de falhas em integrações (Mercado Pago webhooks, Kafka consumers).
- Base pronta para HPA em Kubernetes (métricas de CPU/latência).
- Correlation-id unifica logs, traces e auditoria de negócio.

### Negativas

- Overhead de recursos no Docker Compose (Prometheus, Grafana, Loki, OTel Collector).
- Curva de configuração de dashboards e alertas.
- Cardinalidade alta de labels pode impactar Prometheus se mal dimensionado.

### Neutras

- Alertmanager e paging ficam fora do escopo da v1 (apenas visualização).
- Frontend pode consumir métricas via API do monitoring-module, não diretamente do Prometheus.
- Stack alinhada com tech stack em PROJECT.md (OpenTelemetry, Prometheus, Grafana, Loki).
