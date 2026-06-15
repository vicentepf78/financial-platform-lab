# Cobranças (UI)

**Módulo:** `frontend`  
**Sprint:** 3 — Cobranças  
**Dependências:** `create-charge`, `process-payment-webhook`

## Problem Statement

Operadores precisam criar cobranças, visualizar QR Codes PIX e acompanhar status de pagamento pela interface web. Sem UI de cobranças, o fluxo de billing não é demonstrável no portfólio nem operável no dia a dia.

## Goals

- [ ] Tela de criação de cobrança com seleção de conta e valor
- [ ] Listagem de cobranças com filtros por status (PENDING, PAID, CANCELED, EXPIRED)
- [ ] Visualização de QR Code PIX (imagem e copia-e-cola)
- [ ] Atualização automática de status após pagamento (polling ou refresh)

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Checkout público para pagador externo | Backoffice interno — pagador usa QR externamente |
| Notificações push de pagamento | Polling/refresh manual na v1 |
| Cancelamento de cobrança via UI | Sprint futura |
| Impressão de boleto | PIX apenas |

---

## User Stories

### P1: Criar cobrança e exibir QR Code ⭐ MVP

**User Story**: Como operador, quero criar uma cobrança PIX e visualizar o QR Code na tela, para compartilhar com o pagador.

**Why P1**: Fluxo principal do Sprint 3 na interface.

**Acceptance Criteria**:

1. WHEN operador acessa `/billing/new` e preenche conta, valor e descrição THEN a UI SHALL chamar `POST /api/v1/charges` e exibir QR Code retornado
2. WHEN cobrança é criada THEN a UI SHALL exibir imagem do QR Code (`qrCodeBase64`) e campo copia-e-cola com botão "Copiar"
3. WHEN criação falha THEN a UI SHALL exibir erros de validação por campo
4. WHEN operador clica "Copiar" THEN a UI SHALL copiar `qrCode` para clipboard e exibir confirmação

**Independent Test**: Criar cobrança de R$ 10,00 e verificar QR Code renderizado na tela.

---

### P2: Listar cobranças com status

**User Story**: Como operador, quero listar cobranças filtradas por status, para acompanhar recebimentos pendentes e liquidados.

**Why P2**: Gestão operacional — complementa criação.

**Acceptance Criteria**:

1. WHEN operador acessa `/billing` THEN a UI SHALL exibir tabela com: ID, conta, valor, status, data criação
2. WHEN operador seleciona filtro de status THEN a UI SHALL refazer consulta com parâmetro `status`
3. WHEN operador clica em uma cobrança THEN a UI SHALL navegar para detalhe com QR Code (se PENDING) ou comprovante (se PAID)
4. WHEN lista está vazia THEN a UI SHALL exibir estado vazio com CTA "Criar cobrança"

**Independent Test**: Criar cobranças em status distintos e verificar filtros.

---

### P3: Atualização automática de status

**User Story**: Como operador, quero que o status da cobrança atualize automaticamente após pagamento, sem precisar recarregar a página manualmente.

**Why P3**: Melhora UX; refresh manual cobre necessidade funcional.

**Acceptance Criteria**:

1. WHEN operador visualiza detalhe de cobrança PENDING THEN a UI SHALL fazer polling a cada 5 segundos via `GET /api/v1/charges/{id}`
2. WHEN status muda para PAID THEN a UI SHALL exibir notificação de sucesso e parar polling
3. WHEN polling excede 30 minutos sem mudança THEN a UI SHALL parar polling e sugerir refresh manual

---

## Edge Cases

- WHEN QR Code não carrega (imagem corrompida) THEN a UI SHALL exibir apenas copia-e-cola como fallback
- WHEN sessão expira durante criação THEN a UI SHALL redirecionar para login preservando draft se possível
- WHEN valor informado é inválido (zero, negativo) THEN a UI SHALL validar antes de submeter

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| CHG-10 | P1: Formulário criar cobrança | Design | Pending |
| CHG-11 | P1: Exibir QR Code | Design | Pending |
| CHG-12 | P1: Copiar copia-e-cola | Design | Pending |
| CHG-13 | P2: Listagem com filtros | Design | Pending |
| CHG-14 | P2: Detalhe da cobrança | Design | Pending |
| CHG-15 | P3: Polling de status | Design | Pending |
| CHG-16 | Edge: Validação de valor | Design | Pending |

**Coverage:** 7 total, 0 mapped to tasks, 7 unmapped ⚠️

---

## Success Criteria

- [ ] Operador cria cobrança e visualiza QR Code em < 1 minuto
- [ ] Status PAID refletido na UI após pagamento no sandbox (com polling)
- [ ] Interface consistente com padrão Material UI do backoffice
- [ ] Sem lógica de negócio no frontend — apenas apresentação e chamadas API
