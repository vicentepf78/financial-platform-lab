# ADR-0002: PostgreSQL como Banco de Dados Relacional Principal

**Status:** Accepted  
**Data:** 2026-06-15  
**Decisores:** Equipe Financial Platform Lab  
**Referências:** [PROJECT.md](../.specs/project/PROJECT.md), [AGENTS.md](../AGENTS.md)

## Contexto

A plataforma financeira precisa de um banco relacional robusto para persistir clientes, contas, lançamentos de ledger, cobranças, conciliação e trilhas de auditoria. Os dados financeiros exigem integridade referencial, transações ACID e suporte a consultas analíticas.

O domínio deve permanecer desacoplado de frameworks de persistência (AGENTS.md Rule 4). O schema do banco deve ser versionado explicitamente e reproduzível em todos os ambientes, incluindo testes de integração com Testcontainers.

## Decisão

Adotar **PostgreSQL** como único banco de dados relacional da plataforma, com as seguintes diretrizes:

### Persistência

- PostgreSQL é o **único** store relacional; não haverá bancos secundários na v1.
- Identificadores primários em formato **UUID**.
- **Chaves estrangeiras explícitas** entre entidades relacionadas.
- Índices planejados para consultas frequentes (extrato, saldo, conciliação).

### Migrações

- Versionamento de schema via **Flyway** com migrations explícitas em `backend/`.
- **Proibido** em produção:
  ```yaml
  ddl-auto: create
  ddl-auto: create-drop
  ```
- Schema gerado automaticamente pelo Hibernate é aceitável apenas em testes locais isolados, nunca em produção.

### Camada de acesso

- **Spring Data JPA** utilizado exclusivamente na camada de **adapters** (implementações de ports de saída).
- Entidades JPA e mapeamentos ORM **não** entram na camada `domain/`.
- O domínio interage com persistência apenas via interfaces em `ports/` (ex.: `AccountRepository`, `LedgerEntryRepository`).
- Repositórios convertem entre entidades de domínio e entidades JPA nos adapters.

### Testes

- Testes de integração obrigatórios com **Testcontainers PostgreSQL** para validar migrations e queries reais.

## Consequências

### Positivas

- ACID garante consistência em operações financeiras (transferências, ledger).
- UUIDs evitam colisão de IDs em integrações e facilitam rastreabilidade.
- Flyway garante schema reproduzível entre dev, CI e produção.
- Domínio permanece testável sem container de banco (mocks nos ports).

### Negativas

- Overhead de mapeamento domínio ↔ entidade JPA nos adapters.
- Agregações de saldo a partir do ledger podem exigir otimização (projeções/materialized views) em volume alto.
- Migrations manuais exigem disciplina em cada alteração de schema.

### Neutras

- PostgreSQL já está no stack definido em PROJECT.md e no Docker Compose planejado.
- Testcontainers PostgreSQL é mandatório conforme AGENTS.md.
- Não há adoção de banco NoSQL ou cache na v1.
