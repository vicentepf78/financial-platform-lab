# ADR-0003: Arquitetura Event-Driven com Apache Kafka

**Status:** Accepted  
**Data:** 2026-06-15  
**Decisores:** Equipe Financial Platform Lab  
**Referências:** [ARCHITECTURE.md](../.specs/codebase/ARCHITECTURE.md), [AGENTS.md](../AGENTS.md)

## Contexto

A plataforma financeira precisa desacoplar módulos de negócio, garantir auditabilidade de operações e permitir processamento assíncrono (conciliação, auditoria, monitoramento). Eventos de domínio devem ser publicados somente após transações bem-sucedidas, preservando consistência entre persistência e mensageria.

O domínio não pode depender de Kafka (AGENTS.md Rule 4). Consumidores externos e integrações podem reenviar mensagens, exigindo idempotência e capacidade de retry.

## Decisão

Adotar **Apache Kafka** como barramento de eventos de domínio, com as seguintes diretrizes:

### Publicação de eventos

- Eventos de domínio publicados **após** commit bem-sucedido da transação de banco.
- Publicação via **ports de saída** (`EventPublisher`, `DomainEventPort`); implementação Kafka fica em `adapters/`.
- **Spring Kafka** restrito à camada de adapters e infrastructure; nunca em `domain/` ou `application/`.

### Tópicos por capacidade de negócio

Um tópico por evento/capacidade, em kebab-case:

| Tópico | Evento | Módulo origem |
|--------|--------|---------------|
| `account-created` | AccountCreated | account |
| `transfer-executed` | TransferExecuted | account |
| `ledger-entry-created` | LedgerEntryCreated | ledger |
| `pix-sent` | PixSent | pix |
| `pix-received` | PixReceived | pix |
| `charge-created` | ChargeCreated | billing |
| `charge-paid` | ChargePaid | billing |
| `reconciliation-executed` | ReconciliationExecuted | reconciliation |

### Serialização e contrato

- Serialização em **JSON**; proibida serialização Java nativa.
- Payloads versionados com campos estáveis (`eventId`, `occurredAt`, `correlationId`, `payload`).

### Consumidores

- Consumers implementados em `adapters/` (Kafka listeners).
- Todo consumer deve ser:
  - **Idempotente** — chave de deduplicação por `eventId` ou combinação semântica (`order_id + action`).
  - **Retryable** — retry com backoff; dead-letter topic para falhas persistentes.
- Regras de negócio em consumers delegam para **Use Cases**, nunca implementadas inline no listener.

### Testes

- Testes de integração com **Testcontainers Kafka** obrigatórios para publicação e consumo.

## Consequências

### Positivas

- Desacoplamento entre módulos (audit, monitoring, reconciliation consomem eventos).
- Auditabilidade: trilha imutável de operações financeiras.
- Base para evolução futura (sagas, CQRS, extração de microserviços).
- JSON facilita inspeção, debugging e contratos com ferramentas externas.

### Negativas

- Complexidade operacional adicional (Kafka + Zookeeper/KRaft no Docker Compose).
- Consistência eventual entre módulos que reagem a eventos.
- Necessidade de estratégia de outbox ou transactional messaging para evitar eventos órfãos.

### Neutras

- Kafka UI planejado no ambiente de desenvolvimento para inspeção de tópicos.
- Padrão outbox pode ser adotado em sprint futura se necessário.
- Eventos listados alinhados com PROJECT.md e ARCHITECTURE.md.
