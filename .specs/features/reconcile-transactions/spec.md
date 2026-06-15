# Conciliação de Transações

**Módulo:** `reconciliation-module`  
**Sprint:** 5 — Conciliação  
**Evento de domínio:** `ReconciliationExecuted`  
**Dependências:** `import-cnab`, `register-ledger-entry`

## Problem Statement

Após importar movimentações bancárias via CNAB, o sistema precisa compará-las automaticamente com lançamentos do ledger, identificar divergências (valores, datas, registros órfãos) e permitir reprocessamento. Sem conciliação, discrepâncias entre banco e sistema interno passam despercebidas.

## Goals

- [ ] Comparar registros CNAB importados com lançamentos do ledger por valor, data e referência
- [ ] Classificar resultados: MATCHED, UNMATCHED_BANK, UNMATCHED_LEDGER, AMOUNT_MISMATCH
- [ ] Permitir reprocessamento de lote de conciliação
- [ ] Publicar evento `ReconciliationExecuted` ao concluir
- [ ] Gerar relatório de divergências

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Conciliação automática com ajuste de ledger | Ajustes manuais via lançamento compensatório |
| Conciliação em tempo real (streaming) | Batch após importação CNAB |
| Integração com múltiplos bancos simultâneos | Um banco por lote na v1 |
| Machine learning para matching fuzzy | Matching determinístico na v1 |

---

## User Stories

### P1: Executar conciliação automática ⭐ MVP

**User Story**: Como operador, quero executar conciliação de um lote CNAB importado contra o ledger, para identificar divergências automaticamente.

**Why P1**: Objetivo central do Sprint 5.

**Acceptance Criteria**:

1. WHEN operador envia `POST /api/v1/reconciliation/runs` com `importId` THEN o sistema SHALL comparar cada registro CNAB com lançamentos do ledger no período correspondente
2. WHEN valor, data (±1 dia tolerância) e referência coincidem THEN o sistema SHALL classificar como MATCHED
3. WHEN registro CNAB não encontra correspondência no ledger THEN o sistema SHALL classificar como UNMATCHED_BANK
4. WHEN lançamento do ledger não encontra correspondência no CNAB THEN o sistema SHALL classificar como UNMATCHED_LEDGER
5. WHEN valores diferem THEN o sistema SHALL classificar como AMOUNT_MISMATCH
6. WHEN conciliação conclui THEN o sistema SHALL publicar `ReconciliationExecuted` com totais por classificação

**Independent Test**: Importar CNAB com 10 registros (8 matching, 2 divergentes) e verificar classificação.

---

### P2: Consultar divergências e relatório

**User Story**: Como operador, quero consultar divergências de uma conciliação, para investigar e resolver discrepâncias.

**Why P2**: Resultado da conciliação precisa ser acionável.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/reconciliation/runs/{id}` THEN o sistema SHALL retornar resumo: total, matched, unmatched, mismatches
2. WHEN operador solicita `GET /api/v1/reconciliation/runs/{id}/discrepancies` THEN o sistema SHALL retornar lista paginada de divergências com detalhes de ambos os lados
3. WHEN operador solicita exportação THEN o sistema SHALL gerar CSV com todas as divergências

**Independent Test**: Executar conciliação e consultar divergências via API.

---

### P3: Reprocessar conciliação

**User Story**: Como operador, quero reprocessar uma conciliação após corrigir lançamentos no ledger, para atualizar resultados.

**Why P3**: Conciliação é iterativa — correções exigem re-run.

**Acceptance Criteria**:

1. WHEN operador envia `POST /api/v1/reconciliation/runs/{id}/reprocess` THEN o sistema SHALL reexecutar matching mantendo histórico da execução anterior
2. WHEN reprocessamento conclui THEN o sistema SHALL criar nova execução vinculada à anterior com diff de mudanças
3. WHEN nenhuma mudança é detectada THEN o sistema SHALL retornar resultado idêntico com flag `noChanges: true`

---

## Edge Cases

- WHEN período do CNAB não sobrepõe lançamentos do ledger THEN o sistema SHALL classificar todos como UNMATCHED respectivos
- WHEN múltiplos lançamentos correspondem a um registro CNAB THEN o sistema SHALL marcar como AMBIGUOUS para revisão manual
- WHEN conciliação está em andamento e nova é solicitada para mesmo importId THEN o sistema SHALL rejeitar com 409
- WHEN ledger é atualizado durante conciliação THEN o sistema SHALL usar snapshot do momento da execução

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| REC-01 | P1: Executar conciliação | Design | Pending |
| REC-02 | P1: Classificação MATCHED | Design | Pending |
| REC-03 | P1: UNMATCHED_BANK / UNMATCHED_LEDGER | Design | Pending |
| REC-04 | P1: AMOUNT_MISMATCH | Design | Pending |
| REC-05 | P1: Publicar ReconciliationExecuted | Design | Pending |
| REC-06 | P2: Consultar divergências | Design | Pending |
| REC-07 | P2: Exportar relatório CSV | Design | Pending |
| REC-08 | P3: Reprocessamento | Design | Pending |
| REC-09 | Edge: Matching ambíguo | Design | Pending |

**Coverage:** 9 total, 0 mapped to tasks, 9 unmapped ⚠️

---

## Success Criteria

- [ ] Conciliação de lote de teste identifica 100% das divergências conhecidas
- [ ] Zero falsos MATCHED em cenários de valor divergente nos testes
- [ ] Reprocessamento reflete correções no ledger
- [ ] Evento `ReconciliationExecuted` publicado com métricas corretas
