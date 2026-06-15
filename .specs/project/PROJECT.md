# Financial Platform Lab

**Vision:** Plataforma financeira moderna demonstrando Core Banking, Ledger, PIX, Cobranças e Conciliação com arquitetura event-driven, observabilidade e infraestrutura cloud-native — servindo como portfólio técnico para o mercado financeiro brasileiro.

**For:** Recrutadores e times técnicos de bancos, fintechs, cooperativas de crédito, adquirentes e empresas de pagamento que avaliam competência em domínio financeiro e engenharia de software.

**Solves:** Falta de um projeto de portfólio que demonstre, de ponta a ponta, modelagem de domínio financeiro rigorosa (ledger-first, partidas dobradas), integrações reais (Mercado Pago, Kobana, Banco do Brasil), event-driven architecture e operabilidade em produção (Docker, Kubernetes, observabilidade).

## Goals

- Demonstrar fluxo financeiro completo: cliente → conta → transferência → ledger → cobrança → webhook → conciliação → métricas
- Atingir cobertura mínima de 80% em testes unitários e testes de integração com PostgreSQL e Kafka via Testcontainers
- Executar ambiente completo via Docker Compose e, em fase posterior, deploy em Kubernetes com HPA
- Produzir vídeo de demonstração do fluxo end-to-end para portfólio

## Tech Stack

**Core:**

- Framework: Spring Boot 3 (Java 21)
- Language: Java 21 (backend), TypeScript (frontend)
- Database: PostgreSQL
- Messaging: Apache Kafka

**Key dependencies:**

- Spring Data JPA, Spring Security, Spring Validation, Spring Actuator
- React, Material UI, React Query, Axios, React Router
- OpenTelemetry, Prometheus, Grafana, Loki
- JUnit 5, Mockito, AssertJ, Testcontainers, Playwright

## Scope

**v1 includes (POC completa):**

- Core Banking: clientes, contas, transferências
- Ledger financeiro: débitos, créditos, saldos derivados de lançamentos
- Cobranças com Mercado Pago (QR Code, webhooks)
- PIX: chaves, transferências, recebimento
- Conciliação: importação CNAB, divergências, reprocessamento
- Backoffice financeiro com dashboard, extrato, auditoria e monitoramento
- Observabilidade: métricas de API, banco e Kafka
- Ambiente Docker Compose completo

**Explicitly out of scope (v1):**

- Microserviços (monólito modular é a abordagem inicial)
- Integração real com Banco do Brasil PIX (fase 3 — planejada)
- Integração real com Kobana (fase 2 — planejada)
- Alta disponibilidade multi-região em produção

## Constraints

- Timeline: 7 sprints planejadas (Core Banking → Ledger → Cobranças → PIX → Conciliação → Observabilidade → Kubernetes)
- Technical: Ledger é fonte da verdade — saldo nunca alterado diretamente; toda operação financeira gera débito + crédito; regras de negócio apenas em Domain e Application Layer
- Resources: Desenvolvimento assistido por IA (Cursor); Spec Driven Development com fluxo Spec → Testes → Implementação → Refatoração

## Architecture

**Pattern:** Monorepo + Modular Monolith + Hexagonal Architecture + Vertical Slice Architecture

**Modules:** shared-kernel, customer, account, ledger, pix, billing, reconciliation, audit, monitoring

**Per-module structure:**

```text
module/
├── domain/
├── application/
├── ports/
├── adapters/
└── infrastructure/
```

**Per-feature structure:**

```text
features/
├── create-account/
│   ├── CreateAccountController
│   ├── CreateAccountUseCase
│   ├── CreateAccountRequest
│   ├── CreateAccountResponse
│   └── CreateAccountTest
```

## Domain Events

AccountCreated, TransferExecuted, LedgerEntryCreated, PixSent, PixReceived, ChargeCreated, ChargePaid, ReconciliationExecuted

## Success Criteria

A POC estará concluída quando for possível:

1. Criar cliente e conta
2. Realizar transferência com lançamentos financeiros
3. Consultar extrato
4. Criar cobrança e receber webhook
5. Realizar conciliação
6. Visualizar métricas
7. Executar ambiente completo via Docker Compose
8. Executar ambiente em Kubernetes
9. Demonstrar todo o fluxo em vídeo para portfólio
