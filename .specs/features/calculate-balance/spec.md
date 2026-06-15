# Cálculo de Saldo a partir de Lançamentos

**Módulo:** `ledger-module`  
**Sprint:** 2 — Ledger  
**Dependência:** `register-ledger-entry`

## Problem Statement

Contas precisam exibir saldo atualizado, mas o saldo não pode ser armazenado como dado autoritativo (`account.setBalance` é proibido). O saldo deve ser uma projeção derivada exclusivamente dos lançamentos do ledger, garantindo consistência com a fonte da verdade financeira.

## Goals

- [ ] Calcular saldo de conta somando créditos e subtraindo débitos dos lançamentos
- [ ] Expor saldo via API REST com envelope `{ data, metadata }`
- [ ] Garantir que validação de saldo suficiente em transferências use projeção do ledger
- [ ] Suportar consulta de saldo em data específica (saldo histórico)

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Cache distribuído de saldo (Redis) | Otimização futura — v1 calcula on-demand ou via projeção simples |
| Saldo bloqueado / limite de crédito | Fora do escopo da POC |
| Atualização direta de saldo na entidade Account | Violaria regra ledger-first (AGENTS.md Rule 3) |
| Saldos consolidados multi-conta | Feature de dashboard — Sprint 6 |

---

## User Stories

### P1: Consultar saldo atual de conta ⭐ MVP

**User Story**: Como operador do backoffice, quero consultar o saldo atual de uma conta, para verificar disponibilidade antes de operações.

**Why P1**: Saldo é informação crítica para qualquer operação financeira; sem ele, transferências não podem validar fundos.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/accounts/{id}/balance` THEN o sistema SHALL retornar saldo calculado como `SUM(credits) - SUM(debits)` dos lançamentos da conta
2. WHEN a conta não possui lançamentos THEN o sistema SHALL retornar saldo `0.00` BRL
3. WHEN a conta não existe THEN o sistema SHALL retornar 404 com Problem Details
4. WHEN transferência é solicitada com valor superior ao saldo projetado THEN o sistema SHALL rejeitar com erro `InsufficientBalance`

**Independent Test**: Registrar lançamentos de crédito e débito em uma conta e verificar que o saldo retornado pela API corresponde à soma manual.

---

### P2: Saldo em data específica (ponto no tempo)

**User Story**: Como auditor, quero consultar o saldo de uma conta em uma data passada, para reconciliar extratos históricos.

**Why P2**: Importante para auditoria, mas não bloqueia operações do dia a dia.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/accounts/{id}/balance?asOf={date}` THEN o sistema SHALL calcular saldo considerando apenas lançamentos com `createdAt <= asOf`
2. WHEN `asOf` é futuro THEN o sistema SHALL retornar 400 com mensagem de validação
3. WHEN `asOf` não é informado THEN o sistema SHALL usar timestamp atual (saldo corrente)

**Independent Test**: Criar lançamentos em datas distintas e verificar saldo `asOf` intermediário.

---

### P3: Projeção de saldo otimizada

**User Story**: Como sistema, quero uma estratégia de projeção de saldo eficiente, para evitar recálculo completo a cada consulta em contas com muitos lançamentos.

**Why P3**: Performance — nice-to-have para POC com volume baixo.

**Acceptance Criteria**:

1. WHEN conta possui mais de 1000 lançamentos THEN o sistema SHALL utilizar snapshot incremental ou materialized view para cálculo
2. WHEN novo lançamento é registrado THEN o sistema SHALL atualizar projeção de saldo de forma consistente com cálculo full-scan

---

## Edge Cases

- WHEN lançamentos possuem valores com centavos THEN o sistema SHALL usar aritmética de `Money` sem erros de ponto flutuante
- WHEN consulta de saldo ocorre durante transação em andamento THEN o sistema SHALL respeitar nível de isolamento READ COMMITTED (saldo pode variar entre leituras)
- WHEN conta está encerrada THEN o sistema SHALL permitir consulta de saldo mas bloquear novas operações de débito

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| LED-09 | P1: Consultar saldo atual | Design | Pending |
| LED-10 | P1: Saldo zero sem lançamentos | Design | Pending |
| LED-11 | P1: Validação saldo insuficiente | Design | Pending |
| LED-12 | P2: Saldo em data específica | Design | Pending |
| LED-13 | P2: Validação asOf | Design | Pending |
| LED-14 | P3: Projeção otimizada | Design | Pending |
| LED-15 | Edge: Precisão Money | Design | Pending |

**Coverage:** 7 total, 0 mapped to tasks, 7 unmapped ⚠️

---

## Success Criteria

- [ ] Saldo retornado pela API é idêntico à soma manual de lançamentos em 100% dos cenários de teste
- [ ] Nenhum código no projeto chama `account.setBalance(...)` — verificável por análise estática
- [ ] Transferência com saldo insuficiente é rejeitada antes de persistir lançamentos
- [ ] Tempo de resposta de consulta de saldo < 200ms para contas com até 10.000 lançamentos
