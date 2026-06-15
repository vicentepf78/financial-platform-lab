# Trilha de Auditoria

**Módulo:** `audit-module`  
**Sprint:** 6 — Observabilidade

## Problem Statement

Operações financeiras e ações administrativas precisam ser rastreáveis com registro de quem executou, quando, qual operação e qual foi o resultado. Sem trilha de auditoria, a plataforma não atende requisitos regulatórios básicos nem permite investigação de incidentes.

## Goals

- [ ] Registrar automaticamente ações de usuários e operações do sistema
- [ ] Capturar: usuário, operação, timestamp, correlationId, resultado (success/failure), detalhes
- [ ] Consumir eventos de domínio (Kafka) para auditoria assíncrona
- [ ] Expor API de consulta de registros de auditoria com filtros
- [ ] Garantir imutabilidade dos registros (append-only)

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Auditoria de leitura (GET requests) | Apenas operações de escrita e login na v1 |
| Retenção legal de 5+ anos | Retenção de 90 dias na POC |
| Assinatura digital de registros | Complexidade fora do escopo |
| Exportação para SIEM externo | Self-contained na v1 |

---

## User Stories

### P1: Registrar ações de auditoria automaticamente ⭐ MVP

**User Story**: Como auditor, quero que toda operação financeira e ação administrativa seja registrada automaticamente, para rastreabilidade completa.

**Why P1**: Sem registro automático, auditoria depende de logs não estruturados.

**Acceptance Criteria**:

1. WHEN operação financeira é executada (transferência, cobrança, PIX, conciliação) THEN o sistema SHALL registrar audit entry com: userId, operation, timestamp, correlationId, result, entityType, entityId
2. WHEN domain event é publicado (AccountCreated, TransferExecuted, etc.) THEN consumer de auditoria SHALL persistir registro correspondente
3. WHEN operação falha THEN o sistema SHALL registrar audit entry com result FAILURE e motivo do erro
4. WHEN registro é persistido THEN o sistema SHALL impedir alteração ou exclusão (append-only)

**Independent Test**: Executar transferência e verificar registro de auditoria com todos os campos.

---

### P2: Consultar registros de auditoria

**User Story**: Como auditor, quero consultar registros de auditoria com filtros, para investigar ações específicas.

**Why P2**: Registro sem consulta não é utilizável.

**Acceptance Criteria**:

1. WHEN auditor solicita `GET /api/v1/audit/entries` THEN o sistema SHALL retornar lista paginada ordenada por timestamp decrescente
2. WHEN auditor filtra por `userId`, `operation`, `startDate`, `endDate` ou `correlationId` THEN o sistema SHALL retornar subconjunto correspondente
3. WHEN auditor solicita `GET /api/v1/audit/entries/{id}` THEN o sistema SHALL retornar detalhe completo incluindo payload sanitizado
4. WHEN usuário não possui permissão de auditor THEN o sistema SHALL retornar 403

**Independent Test**: Executar 5 operações distintas e verificar filtros.

---

### P3: Tela de auditoria no backoffice

**User Story**: Como auditor, quero visualizar registros de auditoria na interface web, para investigação sem usar API diretamente.

**Why P3**: UI de auditoria — complementa API; pode ser feature separada mas está no escopo do roadmap.

**Acceptance Criteria**:

1. WHEN auditor acessa `/audit` THEN a UI SHALL exibir tabela: data/hora, usuário, operação, entidade, resultado
2. WHEN auditor aplica filtros THEN a UI SHALL refazer consulta com parâmetros
3. WHEN auditor clica em registro THEN a UI SHALL exibir detalhe com correlationId e link para operação relacionada
4. WHEN exportação é solicitada THEN a UI SHALL gerar CSV dos registros filtrados

---

## Edge Cases

- WHEN consumer Kafka de auditoria falha THEN o sistema SHALL retry com backoff e dead-letter após N tentativas
- WHEN payload contém dados sensíveis (CPF completo, senha) THEN o sistema SHALL mascarar antes de persistir
- WHEN volume de auditoria é alto THEN consultas SHALL manter performance com índices em timestamp, userId e correlationId
- WHEN operação é executada por sistema (webhook, job) THEN userId SHALL ser `SYSTEM`

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| AUD-01 | P1: Registro automático | Design | Pending |
| AUD-02 | P1: Consumer de domain events | Design | Pending |
| AUD-03 | P1: Registro de falhas | Design | Pending |
| AUD-04 | P1: Imutabilidade append-only | Design | Pending |
| AUD-05 | P2: Consulta com filtros | Design | Pending |
| AUD-06 | P2: Controle de acesso | Design | Pending |
| AUD-07 | P3: Tela de auditoria | Design | Pending |
| AUD-08 | P3: Exportação CSV | Design | Pending |
| AUD-09 | Edge: Mascaramento de dados | Design | Pending |

**Coverage:** 9 total, 0 mapped to tasks, 9 unmapped ⚠️

---

## Success Criteria

- [ ] 100% das operações financeiras de escrita geram registro de auditoria
- [ ] Registros correlacionáveis via correlationId com traces e lançamentos do ledger
- [ ] Consulta de auditoria responde em < 500ms para 100.000 registros
- [ ] Nenhum dado sensível em texto claro nos registros de auditoria
