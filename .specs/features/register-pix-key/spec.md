# Cadastro de Chaves PIX

**Módulo:** `pix-module`  
**Sprint:** 4 — PIX

## Problem Statement

Para realizar e receber transferências PIX, contas precisam ter chaves PIX cadastradas (CPF, CNPJ, e-mail, telefone ou chave aleatória). Sem gestão de chaves, operações PIX não podem ser iniciadas nem identificadas.

## Goals

- [ ] Cadastrar chaves PIX vinculadas a contas
- [ ] Validar formato e unicidade de chaves por tipo
- [ ] Consultar chaves cadastradas por conta ou por valor da chave
- [ ] Preparar base para integração futura com Banco do Brasil (port abstrato)

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Integração real com DICT do Banco do Brasil | Fase 3 — v1 simula cadastro interno |
| Portabilidade de chave entre instituições | Fora do escopo da POC |
| Reivindicação de chave (claim) | Fluxo regulatório complexo |
| QR Code estático vinculado a chave | Coberto por billing-module |

---

## User Stories

### P1: Cadastrar chave PIX em conta ⭐ MVP

**User Story**: Como operador, quero cadastrar uma chave PIX em uma conta, para habilitar envio e recebimento de PIX.

**Why P1**: Pré-requisito para qualquer operação PIX.

**Acceptance Criteria**:

1. WHEN operador envia `POST /api/v1/pix/keys` com `accountId`, `keyType` e `keyValue` THEN o sistema SHALL criar chave com status ACTIVE
2. WHEN `keyType` é CPF ou CNPJ THEN o sistema SHALL validar dígitos verificadores via value objects do shared-kernel
3. WHEN `keyType` é EMAIL THEN o sistema SHALL validar formato RFC 5322 simplificado
4. WHEN `keyType` é PHONE THEN o sistema SHALL validar formato E.164 brasileiro (+55...)
5. WHEN `keyType` é RANDOM THEN o sistema SHALL gerar UUID v4 como valor da chave
6. WHEN chave já existe para outra conta THEN o sistema SHALL rejeitar com 409 Conflict

**Independent Test**: Cadastrar chave CPF válida e verificar persistência com status ACTIVE.

---

### P2: Consultar chaves por conta

**User Story**: Como operador, quero listar chaves PIX de uma conta, para verificar quais chaves estão ativas.

**Why P2**: Gestão operacional complementar ao cadastro.

**Acceptance Criteria**:

1. WHEN operador solicita `GET /api/v1/pix/keys?accountId={id}` THEN o sistema SHALL retornar lista de chaves com tipo, valor (mascarado parcialmente) e status
2. WHEN operador solicita `GET /api/v1/pix/keys/lookup?keyValue={value}` THEN o sistema SHALL retornar conta associada (para roteamento de PIX recebido)
3. WHEN conta não possui chaves THEN o sistema SHALL retornar lista vazia com 200

**Independent Test**: Cadastrar 2 chaves em conta e verificar listagem.

---

### P3: Inativar chave PIX

**User Story**: Como operador, quero inativar uma chave PIX, para impedir novas operações sem excluir histórico.

**Why P3**: Gestão do ciclo de vida; cadastro cobre MVP.

**Acceptance Criteria**:

1. WHEN operador envia `PATCH /api/v1/pix/keys/{id}` com `status: INACTIVE` THEN o sistema SHALL inativar chave sem permitir novos PIX com ela
2. WHEN chave está INACTIVE THEN envio de PIX para essa chave SHALL ser rejeitado
3. WHEN chave possui transferências históricas THEN inativação SHALL preservar histórico consultável

---

## Edge Cases

- WHEN conta está encerrada THEN o sistema SHALL rejeitar cadastro de nova chave
- WHEN CPF da chave difere do CPF do titular da conta THEN o sistema SHALL rejeitar (regra de titularidade)
- WHEN múltiplas chaves RANDOM são solicitadas para mesma conta THEN o sistema SHALL permitir (sem limite na v1)

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| PIX-01 | P1: Cadastrar chave PIX | Design | Pending |
| PIX-02 | P1: Validação por tipo | Design | Pending |
| PIX-03 | P1: Unicidade de chave | Design | Pending |
| PIX-04 | P2: Listar chaves por conta | Design | Pending |
| PIX-05 | P2: Lookup por valor | Design | Pending |
| PIX-06 | P3: Inativar chave | Design | Pending |
| PIX-07 | Edge: Titularidade CPF/CNPJ | Design | Pending |

**Coverage:** 7 total, 0 mapped to tasks, 7 unmapped ⚠️

---

## Success Criteria

- [ ] Chaves de todos os tipos suportados (CPF, CNPJ, EMAIL, PHONE, RANDOM) cadastráveis via API
- [ ] Validações de formato rejeitam 100% dos inputs inválidos nos testes
- [ ] Nenhuma dependência de Banco do Brasil no domain — apenas port `PixKeyRegistryPort`
- [ ] Cobertura de testes ≥ 80% em domain e use cases
