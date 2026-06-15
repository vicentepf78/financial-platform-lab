# Shared Kernel — Especificação

**Módulo:** `backend/shared-kernel`
**Status:** Mostly Implemented (scaffold)
**Sprint:** 1 — Core Banking (fundação transversal)

---

## Problem Statement

Módulos de negócio (customer, account, ledger, pix, billing) precisam compartilhar tipos de domínio imutáveis e contratos base sem acoplar uns aos outros. Sem um shared kernel, validações de CPF/CNPJ, representação monetária e publicação de eventos seriam duplicadas ou inconsistentes — violando DDD e as regras financeiras do projeto (ledger-first, auditabilidade).

## Goals

- [ ] Centralizar value objects financeiros e cadastrais reutilizáveis (`Money`, `Cpf`, `Cnpj`, `Identifier`)
- [ ] Fornecer abstrações base para agregados, eventos e auditoria sem dependência de framework
- [ ] Garantir que nenhum módulo de negócio dependa de outro módulo de negócio — apenas do shared-kernel
- [ ] Manter cobertura de testes unitários para regras de validação críticas

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Regras de negócio de clientes/contas | Pertencem aos módulos de negócio |
| Persistência JPA / mapeamento ORM | Fica nos adapters de cada módulo |
| Publicação Kafka concreta | Implementada via ports nos módulos |
| Tipos específicos de eventos (`AccountCreated`, etc.) | Definidos no módulo de origem |
| Internacionalização de moedas além de BRL | Fora do escopo da POC v1 |

---

## Componentes Documentados

### Money

**Localização:** `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/Money.java`

**Responsabilidade:** Representar valores monetários imutáveis em BRL com escala fixa (2 casas decimais).

**Comportamento implementado:**

- Factory methods: `Money.brl(String)`, `Money.brl(BigDecimal)`, `Money.zero()`
- Operações: `add`, `subtract` (rejeita resultado negativo), `isGreaterThanOrEqual`
- Validação: amount ≥ 0; mesma moeda obrigatória em operações
- Imutabilidade: `final class`, campos `private final`

**Uso previsto:** Transferências, saldos, cobranças, ledger entries.

---

### Cpf

**Localização:** `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/Cpf.java`

**Responsabilidade:** Value object para CPF brasileiro com validação de formato e dígitos verificadores.

**Comportamento implementado:**

- `Cpf.of(String raw)` — normaliza removendo não-dígitos
- Rejeita: menos/mais de 11 dígitos, sequências repetidas (111.111.111-11), checksum inválido
- `value()` retorna 11 dígitos; `formatted()` retorna máscara XXX.XXX.XXX-XX

---

### Cnpj

**Localização:** `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/Cnpj.java`

**Responsabilidade:** Value object para CNPJ brasileiro com validação de formato e dígitos verificadores.

**Comportamento implementado:**

- `Cnpj.of(String raw)` — normaliza removendo não-dígitos
- Rejeita: menos/mais de 14 dígitos, sequências repetidas, checksum inválido
- `value()` retorna 14 dígitos; `formatted()` retorna máscara XX.XXX.XXX/XXXX-XX

---

### Identifier

**Localização:** `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/Identifier.java`

**Responsabilidade:** Identificador imutável baseado em UUID para entidades e agregados.

**Comportamento implementado:**

- `Identifier.generate()`, `Identifier.of(UUID)`, `Identifier.of(String)`
- Igualdade por valor UUID

---

### AggregateRoot

**Localização:** `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/AggregateRoot.java`

**Responsabilidade:** Classe base para agregados que acumulam domain events antes da publicação.

**Comportamento implementado:**

- `registerEvent(DomainEvent)` — protegido, chamado pelo agregado
- `pullDomainEvents()` — retorna cópia imutável e limpa fila interna (padrão outbox in-memory)

**Uso previsto:** `Customer`, `Account` estendem `AggregateRoot`.

---

### DomainEvent

**Localização:** `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/DomainEvent.java`

**Responsabilidade:** Contrato mínimo para todos os eventos de domínio.

**Campos obrigatórios:**

| Campo | Tipo | Descrição |
| ----- | ---- | --------- |
| `eventId` | UUID | Identificador único do evento |
| `eventType` | String | Nome do tipo (ex: `AccountCreated`) |
| `occurredAt` | Instant | Timestamp UTC |
| `aggregateId` | Identifier | ID do agregado de origem |

---

### AuditableEntity

**Localização:** `backend/shared-kernel/src/main/java/com/financialplatform/sharedkernel/domain/AuditableEntity.java`

**Responsabilidade:** Metadados de auditoria para entidades persistidas.

**Comportamento implementado:**

- Campos: `id`, `createdAt`, `createdBy`, `updatedAt`, `updatedBy`
- `touch(actor, at)` — atualiza campos de modificação (protegido)

**Uso previsto:** Entidades JPA mapeiam colunas equivalentes; domínio usa `Identifier` + timestamps.

---

## User Stories

### P1: Reutilizar VOs em cadastro de clientes ⭐ MVP

**User Story:** Como desenvolvedor do customer-module, quero usar `Cpf` e `Cnpj` do shared-kernel para validar documentos na camada de domínio sem duplicar lógica.

**Why P1:** Cadastro de clientes é a primeira vertical slice; documento inválido deve falhar no domínio.

**Acceptance Criteria:**

1. WHEN `Cpf.of("123.456.789-09")` com dígitos válidos THEN sistema SHALL retornar instância imutável
2. WHEN `Cpf.of("000.000.000-00")` THEN sistema SHALL lançar `IllegalArgumentException`
3. WHEN customer-module instancia documento THEN SHALL usar apenas factory methods do shared-kernel

**Independent Test:** Teste unitário de `Customer` com CPF/CNPJ inválido falha antes de persistência.

---

### P2: Representar valores financeiros com Money ⭐ MVP

**User Story:** Como desenvolvedor do account-module, quero usar `Money` para amounts de transferência e saldo sem `BigDecimal` solto no domínio.

**Acceptance Criteria:**

1. WHEN `Money.brl("100.50").subtract(Money.brl("50.25")` THEN sistema SHALL retornar `Money.brl("50.25")`
2. WHEN subtração resultaria valor negativo THEN sistema SHALL lançar exceção
3. WHEN comparar saldo para transferência THEN SHALL usar `isGreaterThanOrEqual`

**Independent Test:** `MoneyTest` existente + testes de use case com mocks.

---

### P3: Publicar eventos via AggregateRoot

**User Story:** Como desenvolvedor, quero que agregados registrem eventos via `AggregateRoot` para publicação após commit transacional.

**Acceptance Criteria:**

1. WHEN agregado chama `registerEvent` THEN evento SHALL permanecer até `pullDomainEvents`
2. WHEN `pullDomainEvents` é chamado THEN fila interna SHALL ser esvaziada
3. WHEN evento é publicado THEN SHALL implementar interface `DomainEvent`

---

## Edge Cases

- WHEN `Money` de moedas diferentes é somado/subtraído THEN sistema SHALL lançar `IllegalArgumentException` (currency mismatch)
- WHEN `Identifier.of("not-a-uuid")` THEN sistema SHALL lançar exceção de parsing
- WHEN CPF/CNPJ contém caracteres especiais THEN sistema SHALL normalizar antes de validar
- WHEN agregado registra múltiplos eventos THEN `pullDomainEvents` SHALL retornar todos em ordem de registro

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| SK-01 | P1: Cpf/Cnpj | Implementado | Verified |
| SK-02 | P2: Money | Implementado | Verified |
| SK-03 | P2: Money zero/ops | Implementado | Verified |
| SK-04 | P3: AggregateRoot | Implementado | Verified |
| SK-05 | P3: DomainEvent | Implementado | Verified |
| SK-06 | Auditoria base | Implementado | Verified |
| SK-07 | Identifier UUID | Implementado | Verified |
| SK-08 | Testes Cpf/Cnpj | Pendente | Pending |

**Coverage:** 8 total, 6 verified, 2 pending (testes Cpf/Cnpj ausentes)

---

## Success Criteria

- [ ] Todos os VOs compilam sem dependência de Spring/JPA/Kafka
- [ ] `mvn test -pl shared-kernel` passa (MoneyTest + futuros CpfTest/CnpjTest)
- [ ] Módulos de negócio importam shared-kernel sem dependência circular
- [ ] Nenhuma regra de negócio específica de módulo reside no shared-kernel

---

## Gaps Conhecidos (pós-scaffold)

| Gap | Ação sugerida |
| --- | ------------- |
| Ausência de `CpfTest` e `CnpjTest` | Adicionar na fase Execute se necessário |
| Apenas BRL suportado em `Money` | Suficiente para Sprint 1 |
| `AuditableEntity` não integrada a Spring Data auditing | Configurar nos adapters por módulo |
