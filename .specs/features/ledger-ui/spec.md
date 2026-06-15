# Ledger Financeiro (UI)

**Módulo:** `frontend`  
**Sprint:** 2 — Ledger  
**Dependências:** `register-ledger-entry`, `calculate-balance`, `financial-statement`

## Problem Statement

Operadores do backoffice precisam visualizar débitos, créditos e a razão financeira de forma clara na interface web. APIs de ledger sem UI dificultam demonstração do portfólio e operação diária do sistema.

## Goals

- [ ] Exibir razão financeira (débitos e créditos) por conta selecionada
- [ ] Integrar visualização de extrato com filtros e paginação
- [ ] Exibir saldo atualizado derivado do ledger
- [ ] Seguir padrões do frontend: React, Material UI, React Query

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Lançamentos manuais via UI | Registro apenas via operações de negócio (transferência, cobrança, PIX) |
| Gráficos de evolução de saldo | Sprint 6 (monitoring-dashboard) |
| Modo escuro customizado | Usar tema padrão MUI |
| App mobile | Backoffice web apenas |

---

## User Stories

### P1: Visualizar razão financeira por conta ⭐ MVP

**User Story**: Como operador do backoffice, quero visualizar a razão financeira de uma conta com débitos e créditos, para auditar movimentações visualmente.

**Why P1**: Principal entrega de UI do Sprint 2; demonstra ledger-first no portfólio.

**Acceptance Criteria**:

1. WHEN operador acessa `/ledger` e seleciona uma conta THEN a UI SHALL exibir tabela com colunas: data, descrição, tipo (Débito/Crédito), valor e saldo acumulado
2. WHEN dados estão carregando THEN a UI SHALL exibir skeleton/loading state
3. WHEN API retorna erro THEN a UI SHALL exibir mensagem de erro amigável com opção de retry
4. WHEN operador clica em uma linha THEN a UI SHALL exibir detalhes: correlationId, operationType, performedBy

**Independent Test**: Selecionar conta com lançamentos e verificar que tabela exibe dados idênticos à API de extrato.

---

### P2: Filtrar razão por período e tipo

**User Story**: Como operador, quero filtrar a razão financeira por período e tipo de lançamento, para investigar movimentações específicas.

**Why P2**: Paridade com filtros da API de extrato; melhora usabilidade.

**Acceptance Criteria**:

1. WHEN operador seleciona intervalo de datas THEN a UI SHALL refazer consulta com `startDate` e `endDate`
2. WHEN operador seleciona filtro de tipo (Todos/Débito/Crédito) THEN a UI SHALL atualizar tabela
3. WHEN filtros não retornam resultados THEN a UI SHALL exibir estado vazio com mensagem informativa
4. WHEN operador limpa filtros THEN a UI SHALL restaurar visualização completa

**Independent Test**: Aplicar filtro de débito e verificar que apenas linhas de débito aparecem.

---

### P3: Exportar extrato via UI

**User Story**: Como operador, quero exportar o extrato filtrado em CSV diretamente da interface, para análise externa.

**Why P3**: Conveniência; exportação via API cobre necessidade funcional.

**Acceptance Criteria**:

1. WHEN operador clica em "Exportar CSV" THEN a UI SHALL chamar endpoint de exportação e iniciar download do arquivo
2. WHEN exportação está em andamento THEN a UI SHALL desabilitar botão e exibir indicador de progresso
3. WHEN exportação falha THEN a UI SHALL exibir toast de erro

---

## Edge Cases

- WHEN conta selecionada não possui lançamentos THEN a UI SHALL exibir estado vazio com saldo R$ 0,00
- WHEN sessão expira durante navegação THEN a UI SHALL redirecionar para login
- WHEN lista possui muitas páginas THEN a UI SHALL implementar paginação com controles de página anterior/próxima

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| LED-16 | P1: Tabela razão financeira | Design | Pending |
| LED-17 | P1: Loading e error states | Design | Pending |
| LED-18 | P1: Detalhes do lançamento | Design | Pending |
| LED-19 | P2: Filtros período e tipo | Design | Pending |
| LED-20 | P2: Estado vazio | Design | Pending |
| LED-21 | P3: Exportação CSV | Design | Pending |
| LED-22 | Edge: Paginação | Design | Pending |

**Coverage:** 7 total, 0 mapped to tasks, 7 unmapped ⚠️

---

## Success Criteria

- [ ] Operador consegue visualizar razão financeira completa em < 3 cliques a partir do dashboard
- [ ] Dados exibidos na UI são consistentes com respostas da API (sem transformação incorreta)
- [ ] Interface responsiva funcional em viewport ≥ 1024px
- [ ] Componentes seguem padrão visual do backoffice (Material UI)
