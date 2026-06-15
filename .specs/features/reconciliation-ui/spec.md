# Conciliação (UI)

**Módulo:** `frontend`  
**Sprint:** 5 — Conciliação  
**Dependências:** `import-cnab`, `reconcile-transactions`

## Problem Statement

Operadores precisam importar arquivos CNAB, executar conciliação e visualizar divergências pela interface web. Sem UI de conciliação, o processo fica restrito a chamadas API e não é demonstrável no portfólio.

## Goals

- [ ] Tela de upload de arquivo CNAB com feedback de progresso
- [ ] Listagem de lotes importados com status
- [ ] Visualização de divergências com filtros por tipo
- [ ] Ação de reprocessamento de conciliação

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Editor de lançamentos compensatórios na UI | Correções via módulo ledger em sprint futura |
| Drag-and-drop matching manual | Matching automático na v1 |
| Integração direta com Kobana na UI | Upload manual na v1 |
| Dashboard gerencial de conciliação | Sprint 6 (monitoring-dashboard) |

---

## User Stories

### P1: Upload de arquivo CNAB ⭐ MVP

**User Story**: Como operador, quero fazer upload de arquivo CNAB pela interface, para iniciar o processo de conciliação.

**Why P1**: Ponto de entrada do fluxo de conciliação na UI.

**Acceptance Criteria**:

1. WHEN operador acessa `/reconciliation/import` e seleciona arquivo THEN a UI SHALL enviar via `POST /api/v1/reconciliation/imports` com indicador de progresso
2. WHEN upload é bem-sucedido THEN a UI SHALL exibir resumo: importId, quantidade de registros, data
3. WHEN arquivo é inválido THEN a UI SHALL exibir erros de parsing retornados pela API
4. WHEN arquivo duplicado THEN a UI SHALL exibir aviso 409 com referência ao lote existente

**Independent Test**: Upload de arquivo CNAB de teste e verificar resumo exibido.

---

### P2: Visualizar divergências de conciliação

**User Story**: Como operador, quero visualizar divergências após conciliação, para investigar discrepâncias entre banco e sistema.

**Why P2**: Resultado principal do processo — sem visualização, conciliação não é acionável.

**Acceptance Criteria**:

1. WHEN operador acessa `/reconciliation/runs/{id}` THEN a UI SHALL exibir cards com totais: matched, unmatched bank, unmatched ledger, amount mismatch
2. WHEN operador visualiza tabela de divergências THEN a UI SHALL exibir colunas: tipo, valor CNAB, valor ledger, data, descrição
3. WHEN operador filtra por tipo de divergência THEN a UI SHALL atualizar tabela
4. WHEN operador clica "Exportar CSV" THEN a UI SHALL iniciar download do relatório

**Independent Test**: Executar conciliação e verificar cards e tabela de divergências.

---

### P3: Reprocessar conciliação

**User Story**: Como operador, quero reprocessar uma conciliação pela interface, após corrigir lançamentos no sistema.

**Why P3**: Fluxo iterativo de conciliação.

**Acceptance Criteria**:

1. WHEN operador clica "Reprocessar" THEN a UI SHALL chamar `POST /api/v1/reconciliation/runs/{id}/reprocess` com confirmação
2. WHEN reprocessamento está em andamento THEN a UI SHALL exibir loading e desabilitar ações
3. WHEN reprocessamento conclui THEN a UI SHALL atualizar totais e destacar mudanças em relação à execução anterior
4. WHEN nenhuma mudança THEN a UI SHALL exibir mensagem informativa

---

## Edge Cases

- WHEN upload é cancelado pelo usuário THEN a UI SHALL abortar requisição e limpar estado
- WHEN lista de divergências é muito grande THEN a UI SHALL paginar resultados
- WHEN conciliação ainda não foi executada para um lote THEN a UI SHALL exibir CTA "Executar conciliação"

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| REC-10 | P1: Upload CNAB | Design | Pending |
| REC-11 | P1: Feedback de importação | Design | Pending |
| REC-12 | P2: Cards de resumo | Design | Pending |
| REC-13 | P2: Tabela divergências | Design | Pending |
| REC-14 | P2: Exportação CSV | Design | Pending |
| REC-15 | P3: Reprocessamento | Design | Pending |
| REC-16 | Edge: Paginação | Design | Pending |

**Coverage:** 7 total, 0 mapped to tasks, 7 unmapped ⚠️

---

## Success Criteria

- [ ] Operador completa fluxo upload → conciliação → visualização de divergências em < 5 minutos
- [ ] Dados exibidos consistentes com APIs do reconciliation-module
- [ ] Interface segue padrão Material UI do backoffice
- [ ] Upload suporta arquivos até 50MB com feedback de progresso
