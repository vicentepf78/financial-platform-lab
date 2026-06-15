# PIX (UI)

**Módulo:** `frontend`  
**Sprint:** 4 — PIX  
**Dependências:** `register-pix-key`, `send-pix`, `receive-pix`

## Problem Statement

Operadores precisam gerenciar chaves PIX, enviar transferências e consultar histórico de movimentações PIX pela interface web. Sem UI dedicada, o módulo PIX não é demonstrável no portfólio.

## Goals

- [ ] Tela de gestão de chaves PIX por conta
- [ ] Formulário de envio de PIX com validação
- [ ] Histórico unificado de envios e recebimentos
- [ ] Feedback visual de status de transferência

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Leitura de QR Code via câmera | Complexidade de browser — v1 usa input manual de chave |
| Simulador de PIX recebido na UI | Endpoint interno/API — não exposto ao operador comum |
| Mapa de agências / localização | Irrelevante para POC |
| PIX para contatos salvos | Feature de conveniência futura |

---

## User Stories

### P1: Gerenciar chaves PIX ⭐ MVP

**User Story**: Como operador, quero cadastrar e listar chaves PIX de uma conta, para habilitar operações PIX.

**Why P1**: Pré-requisito visual para envio e recebimento.

**Acceptance Criteria**:

1. WHEN operador acessa `/pix/keys` e seleciona conta THEN a UI SHALL exibir lista de chaves com tipo, valor mascarado e status
2. WHEN operador clica "Nova chave" THEN a UI SHALL exibir formulário com seleção de tipo e campo de valor (ou geração automática para RANDOM)
3. WHEN cadastro é bem-sucedido THEN a UI SHALL atualizar lista e exibir confirmação
4. WHEN chave já existe THEN a UI SHALL exibir erro 409 amigável

**Independent Test**: Cadastrar chave e-mail e verificar na listagem.

---

### P2: Enviar PIX pela interface

**User Story**: Como operador, quero enviar um PIX informando conta de origem, chave de destino e valor, para transferir fundos.

**Why P2**: Operação principal do módulo na UI.

**Acceptance Criteria**:

1. WHEN operador acessa `/pix/send` e preenche formulário THEN a UI SHALL chamar `POST /api/v1/pix/transfers` e exibir comprovante com e2eId
2. WHEN saldo é insuficiente THEN a UI SHALL exibir erro antes ou após submissão com mensagem clara
3. WHEN envio é bem-sucedido THEN a UI SHALL exibir resumo: valor, destino, data/hora, e2eId
4. WHEN operador informa valor inválido THEN a UI SHALL validar formato monetário (R$ X.XXX,XX)

**Independent Test**: Enviar PIX de R$ 25,00 e verificar comprovante exibido.

---

### P3: Histórico de transferências PIX

**User Story**: Como operador, quero visualizar histórico de PIX enviados e recebidos, para acompanhar movimentações.

**Why P3**: Visibilidade completa do módulo.

**Acceptance Criteria**:

1. WHEN operador acessa `/pix/history` THEN a UI SHALL exibir tabela unificada com tipo (ENVIADO/RECEBIDO), valor, contraparte, data e status
2. WHEN operador filtra por conta e período THEN a UI SHALL atualizar tabela
3. WHEN operador clica em linha THEN a UI SHALL exibir detalhes completos da transação
4. WHEN histórico está vazio THEN a UI SHALL exibir estado vazio com CTAs para cadastrar chave ou enviar PIX

---

## Edge Cases

- WHEN API está indisponível THEN a UI SHALL exibir erro com retry
- WHEN chave de destino é inválida THEN a UI SHALL destacar campo com erro de validação
- WHEN lista de histórico é longa THEN a UI SHALL implementar paginação infinita ou por página

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| PIX-25 | P1: Listagem de chaves | Design | Pending |
| PIX-26 | P1: Cadastro de chave | Design | Pending |
| PIX-27 | P2: Formulário envio PIX | Design | Pending |
| PIX-28 | P2: Comprovante de envio | Design | Pending |
| PIX-29 | P3: Histórico unificado | Design | Pending |
| PIX-30 | P3: Filtros e detalhes | Design | Pending |
| PIX-31 | Edge: Validação monetária | Design | Pending |

**Coverage:** 7 total, 0 mapped to tasks, 7 unmapped ⚠️

---

## Success Criteria

- [ ] Operador completa fluxo chave → envio → histórico em < 3 minutos
- [ ] Dados exibidos consistentes com APIs do pix-module
- [ ] Interface segue padrão Material UI do backoffice
- [ ] Formulários com validação client-side antes de submissão
