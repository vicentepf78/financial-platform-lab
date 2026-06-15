# PROJECT.md

# Financial Platform Lab

## 1. Visão Geral

### Objetivo

Construir uma plataforma financeira moderna utilizando práticas de engenharia de software alinhadas ao mercado financeiro, demonstrando conhecimentos em:

* Core Banking
* Ledger Financeiro
* PIX
* Cobranças
* Conciliação Financeira
* Event Driven Architecture
* Observabilidade
* Docker
* Kubernetes
* Desenvolvimento Assistido por IA

O projeto servirá como portfólio técnico para posições em:

* Bancos
* Fintechs
* Cooperativas de Crédito
* Adquirentes
* Empresas de Pagamento

---

# 2. Objetivos Técnicos

O projeto deve demonstrar capacidade de:

* Modelagem de domínio financeiro
* Arquitetura moderna
* Desenvolvimento orientado por especificação
* Testes automatizados
* Event Driven Architecture
* Observabilidade
* Infraestrutura containerizada

---

# 3. Princípios Arquiteturais

## Arquitetura Principal

Monorepo + Modular Monolith + Hexagonal Architecture + Vertical Slice Architecture

### Motivação

Permitir:

* Menor complexidade operacional
* Melhor produtividade com IA
* Menor consumo de tokens
* Facilidade de evolução para microserviços

---

# 4. Stack Tecnológica

## Backend

* Java 21
* Spring Boot 3
* Spring Data JPA
* Spring Security
* Spring Validation
* Spring Actuator
* Maven

## Banco de Dados

* PostgreSQL

## Mensageria

* Apache Kafka

## Frontend

* React
* TypeScript
* Material UI
* React Query
* Axios
* React Router

## Testes

* JUnit 5
* Mockito
* AssertJ
* Testcontainers
* Spring Boot Test
* Playwright

## Observabilidade

* OpenTelemetry
* Prometheus
* Grafana
* Loki

## Infraestrutura

* Docker
* Docker Compose
* Kubernetes
* Helm

---

# 5. Estratégia de Desenvolvimento

## Metodologia

Spec Driven Development

## Ferramentas

* Cursor
* IA Agents
* GitHub

Fluxo obrigatório:

Spec → Testes → Implementação → Refatoração

---

# 6. Estrutura do Repositório

```text
financial-platform/

├── backend/
├── frontend/
├── infra/
├── docs/
├── specs/
├── adr/
├── scripts/
└── README.md
```

---

# 7. Estrutura Backend

```text
backend/

├── shared-kernel/
├── customer-module/
├── account-module/
├── ledger-module/
├── pix-module/
├── billing-module/
├── reconciliation-module/
├── audit-module/
├── monitoring-module/
└── application/
```

---

# 8. Shared Kernel

Objetos compartilhados:

* Money
* CPF
* CNPJ
* DomainEvent
* AggregateRoot
* AuditableEntity
* Identifier

---

# 9. Módulos de Negócio

## Customer Module

Responsável por:

* Cadastro de clientes
* Consulta de clientes
* Atualização cadastral

---

## Account Module

Responsável por:

* Criação de conta
* Encerramento de conta
* Consulta de saldo
* Consulta de movimentações

---

## Ledger Module

Fonte oficial da verdade financeira.

### Regra obrigatória

Nenhum saldo poderá ser alterado diretamente.

Toda movimentação financeira deverá gerar:

* Débito
* Crédito

O saldo será calculado a partir dos lançamentos.

---

## PIX Module

Responsável por:

* Cadastro de chaves
* Transferência PIX
* Consulta PIX
* Recebimento de PIX

Integrações futuras:

* Banco do Brasil

---

## Billing Module

Responsável por:

* Cobranças
* QR Code PIX
* Cobranças pendentes
* Cobranças liquidadas

Integração:

* Mercado Pago

---

## Reconciliation Module

Responsável por:

* Importação CNAB
* Conciliação
* Divergências
* Reprocessamentos

Integração futura:

* Kobana

---

## Audit Module

Responsável por:

* Registro de ações
* Rastreabilidade
* Auditoria

---

## Monitoring Module

Responsável por:

* Métricas
* Indicadores
* Dashboards

---

# 10. Eventos de Domínio

## AccountCreated

Conta criada.

---

## TransferExecuted

Transferência realizada.

---

## LedgerEntryCreated

Lançamento financeiro criado.

---

## PixSent

PIX enviado.

---

## PixReceived

PIX recebido.

---

## ChargeCreated

Cobrança criada.

---

## ChargePaid

Cobrança liquidada.

---

## ReconciliationExecuted

Conciliação realizada.

---

# 11. Arquitetura Hexagonal

Cada módulo deverá seguir:

```text
module/

├── domain/
├── application/
├── ports/
├── adapters/
└── infrastructure/
```

---

# 12. Vertical Slice

Cada funcionalidade deve ser isolada.

Exemplo:

```text
account-module/

features/

├── create-account/
├── close-account/
├── transfer-money/
└── get-balance/
```

---

# 13. Estratégia de Integração

## Fase 1

Mercado Pago

Objetivos:

* Cobranças
* QR Code
* Webhooks

---

## Fase 2

Kobana

Objetivos:

* Boletos
* CNAB
* Retornos

---

## Fase 3

Banco do Brasil

Objetivos:

* PIX
* Cobranças PIX
* Webhooks

---

# 14. Frontend

## Objetivo

Criar um Backoffice Financeiro moderno inspirado em:

* Mercado Pago Business
* Stone Dashboard
* Plataformas de cobrança
* Sistemas bancários corporativos

---

# 15. Telas

## Dashboard Financeiro

Indicadores:

* Saldo Total
* Volume Financeiro
* PIX Hoje
* Cobranças Hoje
* Clientes Ativos

---

## Gestão de Clientes

* Consulta
* Cadastro
* Atualização

---

## Gestão de Contas

* Contas
* Saldos
* Histórico

---

## Transferências

* Conta origem
* Conta destino
* Valor

---

## Extrato Financeiro

* Histórico
* Filtros
* Exportação

---

## Ledger Financeiro

* Débitos
* Créditos
* Razão financeira

---

## PIX

* Chaves
* Transferências
* Histórico

---

## Cobranças

* QR Code
* Status
* Pagamentos

---

## Conciliação

* Upload CNAB
* Divergências
* Reprocessamento

---

## Monitoramento

* APIs
* Kafka
* Banco

---

## Auditoria

* Usuário
* Operação
* Data
* Resultado

---

# 16. Estratégia de Testes

## Pirâmide de Testes

### Unitários

Cobertura mínima:

80%

---

### Integração

Obrigatório uso de:

* PostgreSQL Testcontainers
* Kafka Testcontainers

---

### Contrato

Validar APIs públicas.

---

### E2E

Playwright

Fluxos:

* Cadastro
* Transferência
* PIX
* Cobrança

---

# 17. Docker Compose

Serviços:

* PostgreSQL
* Kafka
* Kafka UI
* Grafana
* Prometheus
* Loki
* Backend
* Frontend

---

# 18. Kubernetes

Fase posterior.

Objetivos:

* Alta disponibilidade
* Escalabilidade
* Rolling Update
* HPA

---

# 19. Observabilidade

Obrigatório instrumentar:

* APIs
* Banco
* Kafka

Métricas:

* Throughput
* Latência
* Erros
* Retries

---

# 20. ADRs

Criar ADR para:

* Arquitetura
* Banco
* Mensageria
* Observabilidade
* Segurança

---

# 21. Regras para IA

## Nunca

* Alterar saldo diretamente
* Ignorar testes
* Criar dependências circulares
* Implementar lógica financeira em controllers

## Sempre

* Escrever testes primeiro
* Respeitar Hexagonal Architecture
* Respeitar Vertical Slice
* Criar documentação
* Atualizar ADRs

---

# 22. Roadmap

## Sprint 1

Core Banking

* Clientes
* Contas
* Transferências

---

## Sprint 2

Ledger

* Débitos
* Créditos
* Saldos

---

## Sprint 3

Cobranças

* Mercado Pago
* Webhooks

---

## Sprint 4

PIX

* Chaves
* Transferências

---

## Sprint 5

Conciliação

* CNAB
* Divergências

---

## Sprint 6

Observabilidade

* Grafana
* Prometheus
* OpenTelemetry

---

## Sprint 7

Kubernetes

* Deploy
* Escalabilidade

---

# 23. Critério de Sucesso

A POC será considerada concluída quando for possível:

1. Criar cliente
2. Criar conta
3. Realizar transferência
4. Gerar lançamentos financeiros
5. Consultar extrato
6. Criar cobrança
7. Receber webhook
8. Realizar conciliação
9. Visualizar métricas
10. Executar ambiente completo via Docker Compose
11. Executar ambiente em Kubernetes
12. Demonstrar todo o fluxo em vídeo para portfólio

```
```
