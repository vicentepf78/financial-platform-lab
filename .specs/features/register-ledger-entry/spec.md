# Registro de Lançamentos Financeiros (Double Entry)

**Módulo:** `ledger-module`  
**Sprint:** 2 — Ledger  
**Evento de domínio:** `LedgerEntryCreated`

## Problem Statement

Operações financeiras na plataforma (transferências, cobranças, PIX) precisam de um registro auditável e consistente. Sem partidas dobradas, não há garantia de que todo débito tenha crédito correspondente, comprometendo a integridade contábil e a rastreabilidade exigida em sistemas financeiros.

## Goals

- [ ] Registrar toda operação financeira como par débito + crédito com valor idêntico
- [ ] Garantir imutabilidade dos lançamentos após persistência (append-only)
- [ ] Publicar evento `LedgerEntryCreated` após transação bem-sucedida
- [ ] Expor API REST para consulta de lançamentos por conta e período

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Estorno/reversão automática de lançamentos | Sprint futura — v1 apenas registra; correções via lançamento compensatório manual |
| Multi-moeda | POC opera exclusivamente em BRL |
| Lançamentos manuais via UI | Sprint 2 foca no registro via use cases de outros módulos |
| Consolidação contábil (DRE, balanço) | Fora do escopo da POC |

---

## User Stories

### P1: Registrar débito e crédito em operação financeira ⭐ MVP

**User Story**: Como sistema de core banking, quero registrar automaticamente um par débito+crédito para cada operação financeira, para que o ledger seja a fonte oficial da verdade.

**Why P1**: Sem double entry, nenhuma outra feature do ledger (saldo, extrato, conciliação) é confiável.

**Acceptance Criteria**:

1. WHEN um use case financeiro solicita registro de lançamento com conta de débito, conta de crédito e valor THEN o sistema SHALL persistir exatamente dois lançamentos (um DEBIT e um CREDIT) com o mesmo valor e mesma referência de operação
2. WHEN o valor do débito difere do valor do crédito THEN o sistema SHALL rejeitar a operação com erro de domínio sem persistir lançamentos
3. WHEN o lançamento é persistido com sucesso THEN o sistema SHALL publicar evento `LedgerEntryCreated` contendo correlationId, accountIds, amount e operationType
4. WHEN uma conta referenciada não existe ou está encerrada THEN o sistema SHALL rejeitar o registro sem efeitos parciais

**Independent Test**: Executar transferência entre duas contas e verificar no banco dois lançamentos (débito na origem, crédito no destino) com mesmo valor e `correlationId`.

---

### P2: Consultar lançamentos por conta

**User Story**: Como operador do backoffice, quero consultar lançamentos de uma conta específica, para auditar movimentações.

**Why P2**: Essencial para suporte e auditoria, mas depende do registro P1 funcionar.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/ledger/entries?accountId={id}` THEN o sistema SHALL retornar lista paginada de lançamentos ordenados por data decrescente
2. WHEN operador filtra por `startDate` e `endDate` THEN o sistema SHALL retornar apenas lançamentos dentro do intervalo informado
3. WHEN `accountId` é inválido THEN o sistema SHALL retornar 404 com Problem Details (RFC 9457)

**Independent Test**: Criar lançamentos para uma conta e consultar via API verificando paginação e ordenação.

---

### P3: Metadados de auditoria em cada lançamento

**User Story**: Como auditor, quero que cada lançamento contenha usuário, timestamp, correlationId e tipo de operação, para rastrear a origem de cada movimentação.

**Why P3**: Complementa auditabilidade; não bloqueia MVP do registro.

**Acceptance Criteria**:

1. WHEN um lançamento é criado THEN o sistema SHALL armazenar `createdAt`, `correlationId`, `operationType` e `performedBy` (usuário ou sistema)
2. WHEN operador consulta um lançamento THEN o sistema SHALL expor todos os metadados de auditoria no response

---

## Edge Cases

- WHEN valor é zero ou negativo THEN o sistema SHALL rejeitar com erro de validação
- WHEN a mesma `idempotencyKey` é enviada duas vezes THEN o sistema SHALL retornar o lançamento existente sem duplicar entradas
- WHEN falha na publicação do evento Kafka após commit no banco THEN o sistema SHALL garantir retry ou outbox pattern para entrega eventual
- WHEN duas operações concorrentes na mesma conta ocorrem THEN o sistema SHALL persistir ambas sem corrupção de dados (isolamento transacional)

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| LED-01 | P1: Registrar débito e crédito | Design | Pending |
| LED-02 | P1: Validar paridade débito/crédito | Design | Pending |
| LED-03 | P1: Publicar LedgerEntryCreated | Design | Pending |
| LED-04 | P1: Validar contas ativas | Design | Pending |
| LED-05 | P2: Consultar lançamentos por conta | Design | Pending |
| LED-06 | P2: Filtrar por período | Design | Pending |
| LED-07 | P3: Metadados de auditoria | Design | Pending |
| LED-08 | Edge: Idempotência | Design | Pending |

**Coverage:** 8 total, 0 mapped to tasks, 8 unmapped ⚠️

---

## Success Criteria

- [ ] Toda transferência gera exatamente 2 lançamentos (débito + crédito) com valores iguais
- [ ] Nenhum lançamento órfão (débito sem crédito ou vice-versa) em testes de integração
- [ ] Evento `LedgerEntryCreated` publicado em 100% das operações bem-sucedidas
- [ ] Cobertura de testes unitários ≥ 80% no domínio e use cases do ledger-module
