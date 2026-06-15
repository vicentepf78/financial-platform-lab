# Importação de Arquivo CNAB

**Módulo:** `reconciliation-module`  
**Sprint:** 5 — Conciliação

## Problem Statement

Instituições financeiras recebem arquivos de retorno bancário (CNAB 240/400) com movimentações que precisam ser comparadas com os lançamentos internos do ledger. Sem importação e parsing de CNAB, a conciliação financeira não é possível.

## Goals

- [ ] Aceitar upload de arquivo CNAB (240 e 400) via API
- [ ] Fazer parsing dos registros de movimentação financeira
- [ ] Mapear registros CNAB para entidades de conciliação internas
- [ ] Persistir lote de importação com status e metadados
- [ ] Preparar base para integração futura com Kobana

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Integração automática com Kobana (API) | Fase 2 — v1 usa upload manual |
| Geração de arquivo CNAB de remessa | Apenas retorno na v1 |
| CNAB de boletos (cobrança tradicional) | Foco em movimentações de conta corrente |
| Validação de assinatura digital do arquivo | Fora do escopo da POC |

---

## User Stories

### P1: Upload e parsing de arquivo CNAB ⭐ MVP

**User Story**: Como operador, quero fazer upload de um arquivo CNAB de retorno, para que o sistema importe as movimentações bancárias.

**Why P1**: Sem importação, não há dados externos para conciliar.

**Acceptance Criteria**:

1. WHEN operador envia `POST /api/v1/reconciliation/imports` com arquivo CNAB THEN o sistema SHALL validar formato (extensão .ret, .txt ou .cnab) e iniciar parsing
2. WHEN parsing é bem-sucedido THEN o sistema SHALL persistir lote com `importId`, quantidade de registros, data de importação e status PARSED
3. WHEN cada registro é parseado THEN o sistema SHALL extrair: data, valor, tipo (crédito/débito), identificador bancário e descrição
4. WHEN arquivo é inválido ou corrompido THEN o sistema SHALL rejeitar com 422 e detalhar linha/erro de parsing
5. WHEN mesmo arquivo (hash SHA-256) já foi importado THEN o sistema SHALL rejeitar com 409

**Independent Test**: Upload de arquivo CNAB de exemplo e verificar registros persistidos.

---

### P2: Consultar lotes e registros importados

**User Story**: Como operador, quero consultar lotes importados e seus registros, para verificar o que foi processado.

**Why P2**: Visibilidade do processo de importação.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/reconciliation/imports` THEN o sistema SHALL retornar lista paginada de lotes com status
2. WHEN operador solicita `GET /api/v1/reconciliation/imports/{id}/records` THEN o sistema SHALL retornar registros do lote
3. WHEN lote não existe THEN o sistema SHALL retornar 404

**Independent Test**: Importar arquivo e consultar registros via API.

---

### P3: Mapeamento configurável de códigos CNAB

**User Story**: Como operador, quero que códigos de lançamento CNAB sejam mapeados para tipos de operação internos, para conciliação precisa.

**Why P3**: Flexibilidade entre bancos; parsing básico cobre MVP.

**Acceptance Criteria**:

1. WHEN registro CNAB possui código de ocorrência THEN o sistema SHALL aplicar mapeamento configurado em `cnab_code_mappings`
2. WHEN código não possui mapeamento THEN o sistema SHALL classificar como UNKNOWN para revisão manual
3. WHEN operador atualiza mapeamento THEN novas importações SHALL usar regras atualizadas

---

## Edge Cases

- WHEN arquivo excede 50MB THEN o sistema SHALL rejeitar com 413
- WHEN arquivo contém registros com valor zero THEN o sistema SHALL importar mas marcar para revisão
- WHEN encoding do arquivo não é UTF-8/ISO-8859-1 THEN o sistema SHALL tentar detecção automática ou rejeitar com erro claro
- WHEN parsing parcial falha (algumas linhas inválidas) THEN o sistema SHALL importar linhas válidas e reportar erros das inválidas

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| CNAB-01 | P1: Upload arquivo CNAB | Design | Pending |
| CNAB-02 | P1: Parsing registros | Design | Pending |
| CNAB-03 | P1: Persistir lote | Design | Pending |
| CNAB-04 | P1: Rejeitar arquivo inválido | Design | Pending |
| CNAB-05 | P1: Deduplicação por hash | Design | Pending |
| CNAB-06 | P2: Consultar lotes | Design | Pending |
| CNAB-07 | P2: Consultar registros | Design | Pending |
| CNAB-08 | P3: Mapeamento de códigos | Design | Pending |
| CNAB-09 | Edge: Arquivo grande | Design | Pending |

**Coverage:** 9 total, 0 mapped to tasks, 9 unmapped ⚠️

---

## Success Criteria

- [ ] Arquivo CNAB de exemplo (240 e 400) parseado com 100% dos registros válidos
- [ ] Registros importados contêm campos necessários para conciliação (data, valor, identificador)
- [ ] Upload duplicado rejeitado sem registros duplicados
- [ ] Testes unitários cobrem parser com fixtures de arquivos reais anonimizados
