# Extrato Financeiro

**Módulo:** `ledger-module`  
**Sprint:** 2 — Ledger  
**Dependências:** `register-ledger-entry`, `calculate-balance`

## Problem Statement

Operadores e clientes precisam visualizar o histórico completo de movimentações de uma conta (extrato), com filtros por período e tipo, e capacidade de exportar para análise externa. Sem extrato estruturado, a auditoria e o suporte ao cliente ficam comprometidos.

## Goals

- [ ] Expor extrato financeiro com lançamentos, saldo acumulado e metadados de operação
- [ ] Permitir filtros por período, tipo (débito/crédito) e tipo de operação
- [ ] Suportar exportação em CSV
- [ ] Paginar resultados para contas com alto volume de movimentações

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Exportação PDF com layout bancário oficial | Complexidade visual — v1 exporta CSV |
| Extrato consolidado multi-conta | Feature de relatório gerencial — futuro |
| Envio de extrato por e-mail | Fora do escopo da POC |
| Extrato de conta encerrada com reabertura | Encerramento é definitivo na v1 |

---

## User Stories

### P1: Consultar extrato com saldo acumulado ⭐ MVP

**User Story**: Como operador do backoffice, quero consultar o extrato de uma conta com saldo acumulado linha a linha, para entender a evolução do saldo ao longo do tempo.

**Why P1**: Extrato é a principal ferramenta de auditoria e suporte; sem ele, operadores não conseguem investigar movimentações.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/accounts/{id}/statement` THEN o sistema SHALL retornar lista de lançamentos com data, descrição, tipo (DEBIT/CREDIT), valor e saldo acumulado após cada lançamento
2. WHEN extrato é solicitado THEN o sistema SHALL ordenar lançamentos por `createdAt` ascendente para cálculo correto do saldo acumulado
3. WHEN operador informa `page` e `size` THEN o sistema SHALL retornar resultado paginado com `metadata.totalElements` e `metadata.totalPages`
4. WHEN conta não existe THEN o sistema SHALL retornar 404

**Independent Test**: Criar 5 lançamentos em sequência e verificar que saldo acumulado de cada linha corresponde à soma progressiva.

---

### P2: Filtrar extrato por período e tipo

**User Story**: Como operador, quero filtrar o extrato por período e tipo de lançamento, para focar em movimentações específicas.

**Why P2**: Melhora usabilidade significativamente; extrato completo sem filtros é utilizável mas limitado.

**Acceptance Criteria**:

1. WHEN operador informa `startDate` e `endDate` THEN o sistema SHALL retornar apenas lançamentos dentro do intervalo
2. WHEN operador informa `entryType=DEBIT` ou `entryType=CREDIT` THEN o sistema SHALL filtrar por tipo
3. WHEN operador informa `operationType` (ex: TRANSFER, CHARGE, PIX) THEN o sistema SHALL filtrar por tipo de operação
4. WHEN filtros resultam em lista vazia THEN o sistema SHALL retornar `data: []` com status 200

**Independent Test**: Criar lançamentos de tipos distintos e verificar que cada filtro retorna subconjunto correto.

---

### P3: Exportar extrato em CSV

**User Story**: Como operador, quero exportar o extrato filtrado em CSV, para análise em planilha ou envio a auditoria externa.

**Why P3**: Conveniência operacional; consulta on-line cobre MVP.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/accounts/{id}/statement/export?format=csv` THEN o sistema SHALL retornar arquivo CSV com cabeçalhos: data, descrição, tipo, valor, saldoAcumulado, correlationId
2. WHEN filtros de período/tipo estão aplicados THEN o sistema SHALL exportar apenas lançamentos filtrados
3. WHEN exportação excede 50.000 linhas THEN o sistema SHALL retornar 413 com sugestão de reduzir período

---

## Edge Cases

- WHEN período informado tem `startDate` posterior a `endDate` THEN o sistema SHALL retornar 400
- WHEN conta possui lançamentos no mesmo milissegundo THEN o sistema SHALL desempatar por `id` para ordenação determinística
- WHEN exportação é solicitada para conta sem lançamentos THEN o sistema SHALL retornar CSV apenas com cabeçalho

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| STMT-01 | P1: Extrato com saldo acumulado | Design | Pending |
| STMT-02 | P1: Ordenação ascendente | Design | Pending |
| STMT-03 | P1: Paginação | Design | Pending |
| STMT-04 | P2: Filtro por período | Design | Pending |
| STMT-05 | P2: Filtro por tipo | Design | Pending |
| STMT-06 | P2: Filtro por operationType | Design | Pending |
| STMT-07 | P3: Exportação CSV | Design | Pending |
| STMT-08 | P3: Limite de exportação | Design | Pending |
| STMT-09 | Edge: Validação de período | Design | Pending |

**Coverage:** 9 total, 0 mapped to tasks, 9 unmapped ⚠️

---

## Success Criteria

- [ ] Saldo acumulado da última linha do extrato é igual ao saldo retornado por `calculate-balance`
- [ ] Filtros retornam subconjuntos corretos em 100% dos cenários de teste
- [ ] CSV exportado é válido e abre corretamente em LibreOffice/Google Sheets
- [ ] Extrato paginado responde em < 500ms para contas com até 10.000 lançamentos
