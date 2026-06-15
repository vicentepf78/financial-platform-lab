# Close Account — Especificação

**Módulo:** `account-module`
**Endpoint:** `POST /api/v1/accounts/{id}/close`
**Sprint:** 1 — Core Banking

---

## Problem Statement

Contas inativas ou encerradas por solicitação do cliente precisam ser fechadas de forma segura. Encerrar conta com saldo residual causaria perda financeira ou inconsistência contábil. O sistema deve validar saldo zero (via projeção ledger) e status ACTIVE antes de transicionar para CLOSED.

## Goals

- [ ] Encerrar conta via API dedicada (ação, não DELETE)
- [ ] Validar saldo zero via `LedgerPort` antes do encerramento
- [ ] Validar conta em status ACTIVE
- [ ] Transicionar status para CLOSED com auditoria
- [ ] Impedir novas movimentações em conta CLOSED

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Estorno automático de saldo residual | Processo manual / transferência prévia |
| Reabertura de conta | Não previsto S1 |
| Evento AccountClosed | Opcional P3; não bloqueia MVP |
| Exclusão física de registro | Soft close via status |

---

## User Stories

### P1: Encerrar conta com saldo zero ⭐ MVP

**User Story:** Como operador, quero encerrar uma conta sem saldo para finalizar relacionamento bancário.

**Why P1:** Ciclo de vida completo da conta no Core Banking.

**Acceptance Criteria**:

1. WHEN `POST /api/v1/accounts/{id}/close` e conta ACTIVE com saldo zero THEN sistema SHALL retornar `200 OK` com `{ data: { id, status: "CLOSED", closedAt }, metadata: {} }`
2. WHEN encerramento THEN status SHALL ser `CLOSED` persistido
3. WHEN conta inexistente THEN 404
4. WHEN conta já CLOSED THEN 409 Conflict

**Independent Test:** Conta nova (saldo zero) → close → GET status CLOSED.

---

### P2: Rejeitar encerramento com saldo positivo

**User Story:** Como compliance, quero impedir encerramento de conta com saldo para evitar abandono de recursos.

**Acceptance Criteria**:

1. WHEN close em conta com saldo > 0 THEN sistema SHALL retornar `422 Unprocessable Entity` (ou 409) com Problem Details indicando saldo residual
2. WHEN rejeitado THEN status permanece ACTIVE
3. WHEN saldo consultado THEN valor SHALL vir de projeção ledger (não campo balance)

**Independent Test:** Após transferência inbound simulada, close retorna erro.

---

### P3: Bloquear operações em conta encerrada

**User Story:** Como sistema, quero que transferências envolvendo conta CLOSED sejam rejeitadas.

**Acceptance Criteria**:

1. WHEN conta CLOSED THEN transfer-money origin/destination SHALL falhar (integração com XFER)
2. WHEN close bem-sucedido THEN updatedAt/updatedBy atualizados

---

## Edge Cases

- WHEN UUID inválido THEN 400
- WHEN saldo negativo (não permitido pelo domínio Money) THEN tratar como erro de integridade 500
- WHEN close concorrente duplicado THEN idempotência retorna 200 se já CLOSED ou 409

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| ACCT-08 | P1: Close saldo zero | Design | Pending |
| ACCT-09 | P1: Status CLOSED | Design | Pending |
| ACCT-10 | P1: 404/409 | Design | Pending |
| ACCT-11 | P2: Rejeitar saldo > 0 | Design | Pending |
| ACCT-12 | P3: Conta closed inoperante | Design | Pending |

**Coverage:** 5 total, 0 mapped, 5 pending

---

## Success Criteria

- [ ] Close funcional com validação ledger
- [ ] Testes unitários domain + use case + integração controller
- [ ] Regra de saldo zero no application/domain, não no controller
