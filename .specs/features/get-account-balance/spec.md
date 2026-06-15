# Get Account Balance — Especificação

**Módulo:** `account-module`
**Endpoint:** `GET /api/v1/accounts/{id}/balance`
**Sprint:** 1 — Core Banking

---

## Problem Statement

Operadores e fluxos de transferência precisam consultar saldo atual de uma conta. O saldo é projeção derivada do ledger — nunca um campo autoritativo na entidade Account. Esta feature expõe a projeção via API, antecipando integração completa com ledger-module no Sprint 2.

## Goals

- [ ] Expor saldo via GET REST para conta existente
- [ ] Saldo sempre obtido via `LedgerPort.getBalanceProjection`
- [ ] Retornar valor como `Money` (amount + currency BRL)
- [ ] Incluir `accountId` e `asOf` (timestamp da projeção)

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Extrato / histórico movimentações | Sprint 2 |
| Saldo bloqueado / disponível | Fora escopo S1 |
| Cache de saldo | YAGNI |
| Multi-moeda | Apenas BRL |
| Coluna balance em accounts | Proibido Rule 3 |

---

## User Stories

### P1: Consultar saldo de conta existente ⭐ MVP

**User Story:** Como operador, quero ver o saldo atual de uma conta para validar operações.

**Acceptance Criteria**:

1. WHEN `GET /api/v1/accounts/{id}/balance` e conta existe THEN sistema SHALL retornar `200 OK` com `{ data: { accountId, amount: "0.00", currency: "BRL", asOf }, metadata: {} }`
2. WHEN conta inexistente THEN 404
3. WHEN saldo THEN SHALL ser calculado exclusivamente via LedgerPort

**Independent Test:** Conta recém-criada retorna 0.00 BRL.

---

### P2: Saldo após movimentação (integração ledger stub)

**User Story:** Como tester, quero que saldo reflita transferências registradas no ledger stub.

**Acceptance Criteria**:

1. WHEN transferência credita conta THEN balance SHALL aumentar conforme projeção ledger
2. WHEN transferência debita conta THEN balance SHALL diminuir
3. WHEN ledger indisponível THEN 503 Service Unavailable

---

### P3: Metadados e formatação

**User Story:** Como consumidor API, quero amount como string decimal com 2 casas para evitar erro de ponto flutuante.

**Acceptance Criteria**:

1. WHEN resposta THEN amount SHALL ser string `"1234.56"` scale 2
2. WHEN currency THEN SHALL ser `"BRL"`

---

## Edge Cases

- WHEN UUID malformado THEN 400
- WHEN conta CLOSED THEN ainda permite consulta saldo (200) — saldo pode ser zero
- WHEN saldo zero THEN amount `"0.00"`

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| ACCT-13 | P1: GET balance 200 | Design | Pending |
| ACCT-14 | P1: LedgerPort projection | Design | Pending |
| ACCT-15 | P1: 404 | Design | Pending |
| ACCT-16 | P2: Saldo pós-movimento | Design | Pending |
| ACCT-17 | P3: Formato amount | Design | Pending |

**Coverage:** 5 total, 0 mapped, 5 pending

---

## Success Criteria

- [ ] Endpoint read-only sem regra de negócio no controller
- [ ] Zero referências a account.balance column
- [ ] Design documenta evolução Sprint 2 ledger real
