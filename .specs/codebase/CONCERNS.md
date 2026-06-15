# Codebase Concerns

**Analysis Date:** 2026-06-15
**Status:** Repositório em fase pré-implementação — concerns focam em riscos de início e gaps documentais.

## Pre-Implementation Gaps

**Scaffold ausente:**

- Issue: Nenhum diretório `backend/`, `frontend/`, `infra/` existe; impossível executar ou testar qualquer feature
- Files: Raiz do repositório — apenas `PROJECT.md`, `AGENTS.md`, `.specs/`
- Why: Projeto iniciado com documentação de visão antes do código
- Impact: Bloqueia qualquer sprint; brownfield mapping documenta estado planejado, não detectado
- Fix approach: Criar scaffold Maven multi-módulo + React + Docker Compose como primeira tarefa de implementação

**Manifests de dependência ausentes:**

- Issue: Sem `pom.xml`, `package.json` ou `docker-compose.yml` para validar versões reais
- Files: N/A
- Impact: STACK.md e TESTING.md baseiam-se em documentação, não em dependências detectadas
- Fix approach: Gerar manifests no scaffold e atualizar STACK.md com versões reais

**ADRs não criados:**

- Issue: `PROJECT.md` exige ADRs para arquitetura, banco, mensageria, observabilidade e segurança; pasta `adr/` não existe
- Files: `adr/` (ausente)
- Impact: Decisões arquiteturais não formalizadas; risco de inconsistência entre agentes de IA
- Fix approach: Criar ADR-001 a ADR-005 antes da Sprint 1

## Security Considerations

**Domínio financeiro — superfície de ataque elevada:**

- Risk: Implementação incorreta de idempotência em PIX, webhooks e cobranças pode causar duplicação financeira
- Files: A implementar em `billing-module`, `pix-module`
- Current mitigation: Regras documentadas em `AGENTS.md` (idempotency obrigatória)
- Recommendations: Implementar idempotency keys desde a primeira integração; contract tests para webhooks; nunca expor tokens em código

**Autenticação não definida:**

- Risk: Backoffice financeiro sem auth adequada expõe operações sensíveis
- Files: A definir — `backend/application/` security config
- Current mitigation: Spring Security planejado; ADR de segurança pendente
- Recommendations: Definir JWT vs session no ADR; RBAC mínimo (admin, operador, auditor)

**Ledger integrity:**

- Risk: Bypass da regra ledger-first (alteração direta de saldo) corrompe dados financeiros
- Files: `account-module`, `ledger-module`
- Current mitigation: `AGENTS.md` Rule 3 — proibido `account.setBalance(...)`
- Recommendations: Code review automatizado; testes que verificam que toda movimentação gera par débito/crédito

## Architectural Risks

**Acoplamento entre módulos:**

- Risk: Dependências diretas entre módulos de negócio violam boundaries do monólito modular
- Files: Todos os módulos em `backend/`
- Impact: Dificulta evolução para microserviços; aumenta acoplamento
- Fix approach: Comunicação apenas via ports (síncrono) ou Kafka (assíncrono); validar com ArchUnit ou similar

**Lógica de negócio em adapters:**

- Risk: Controllers ou consumers com regras financeiras violam Rule 1
- Files: `*/adapters/`, `*/features/*Controller.java`
- Impact: Dificulta testes; acopla domínio a framework
- Fix approach: Lint/review checklist; testes de use case obrigatórios antes de controller

## Performance Considerations

**Cálculo de saldo por agregação:**

- Risk: Consulta de saldo via SUM de lançamentos pode degradar com volume
- Files: `ledger-module` (a implementar)
- Impact: Latência em get-balance com milhões de lançamentos
- Fix approach: Projeção materializada de saldo (deferred idea em STATE.md); índices em `(account_id, created_at)`

**Kafka consumer lag:**

- Risk: Consumers lentos em picos de PIX/cobranças
- Files: `*/adapters/messaging/`
- Impact: Atraso em auditoria e lançamentos derivados
- Fix approach: Monitorar lag via Prometheus; dead-letter topics; HPA em Sprint 7

## Test Coverage Gaps

**customer-module (referência):**

- Status: create-customer implementado com testes unitários e integração — ver `.specs/codebase/INDEX.md`
- Padrão: testes co-localizados por task + gate Maven (`.rules/testing.md`)

**Demais módulos:**

- Issue: cobertura ainda baixa fora de customer-module; meta de 80% unitários não verificável globalmente
- Fix approach: replicar slice de referência em `create-account` e features seguintes

**Testcontainers dependency:**

- Issue: Integration tests requerem Docker; CI deve ter Docker disponível
- Files: A configurar em `.github/workflows/`
- Impact: CI pode falhar sem Docker-in-Docker
- Fix approach: Configurar GitHub Actions com service containers ou Testcontainers Cloud

## Operational Concerns

**Docker Compose complexity:**

- Risk: 8+ serviços (PostgreSQL, Kafka, Kafka UI, Grafana, Prometheus, Loki, Backend, Frontend) dificultam onboarding
- Files: `infra/docker-compose/` (a criar)
- Impact: Desenvolvedores podem pular observabilidade local
- Fix approach: Profiles no compose (`docker compose --profile observability up`); README com quick start mínimo (DB + Kafka + Backend)

**Kubernetes premature optimization:**

- Risk: Sprint 7 pode atrasar entrega da POC se priorizada cedo
- Files: `infra/kubernetes/` (a criar)
- Impact: POC funcional em Docker Compose é critério de sucesso antes de K8s
- Fix approach: Manter K8s como sprint final; não bloquear sprints 1-6

## Dependency Risks

**Integrações externas em sandbox:**

- Risk: Mercado Pago sandbox pode divergir de produção; confusão entre Orders API e Payments legacy
- Files: `billing-module/adapters/mercadopago/`
- Impact: Contract tests passam mas produção falha; webhook topic errado (`payment` vs `order`) impede liquidação
- Fix approach: Orders API documentada em `docs/integrations/mercadopago/`; contract tests + checklist Sprint 3

**Versões de Spring Boot 3 + Java 21:**

- Risk: Compatibilidade de bibliotecas (OpenTelemetry, Testcontainers) com Java 21
- Files: `pom.xml` (a criar)
- Impact: Build failures ou runtime issues
- Fix approach: Usar BOM do Spring Boot; validar no scaffold inicial

## Documentation Duplication

**PROJECT.md raiz vs .specs/project/PROJECT.md:**

- Issue: Duas fontes de visão do projeto
- Files: `PROJECT.md`, `.specs/project/PROJECT.md`
- Impact: Divergência se apenas um for atualizado
- Fix approach: `PROJECT.md` raiz = visão detalhada; `.specs/project/PROJECT.md` = versão operacional condensada; atualizar ambos em mudanças de escopo
