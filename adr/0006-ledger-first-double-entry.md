# ADR-0006: Ledger-First com Partidas Dobradas

**Status:** Accepted  
**Data:** 2026-06-15  
**Decisores:** Equipe Financial Platform Lab  
**Referências:** [STATE.md AD-003](../.specs/project/STATE.md), [AGENTS.md Rule 3](../AGENTS.md), [ARCHITECTURE.md](../.specs/codebase/ARCHITECTURE.md)

## Contexto

Em sistemas financeiros, o saldo de uma conta é uma consequência de movimentações históricas, não um valor mutável arbitrariamente. Alterar saldo diretamente impede auditoria, reconciliação e rastreabilidade exigidas por reguladores e por boas práticas de Core Banking.

O Financial Platform Lab deve demonstrar modelagem rigorosa de domínio financeiro. Transferências, recebimentos PIX, liquidação de cobranças e conciliação são operações que movimentam dinheiro e devem seguir contabilidade de partidas dobradas.

Esta decisão formaliza a regra já registrada em STATE.md como **AD-003** e em AGENTS.md como **Rule 3**.

## Decisão

Adotar **Ledger-First** com **partidas dobradas (double-entry accounting)** como regra fundamental e inegociável da plataforma.

### Princípios

1. **Ledger é a fonte da verdade** — `ledger-module` é o repositório autoritativo de movimentações financeiras.
2. **Saldo é projeção** — saldo de conta derivado da agregação de lançamentos (débitos e créditos); nunca armazenado como dado autoritativo mutável.
3. **Toda operação financeira gera débito + crédito** — cada transação produz pelo menos um par de lançamentos balanceados.

### Exemplo: transferência entre contas

| Lançamento | Conta | Tipo | Valor |
|------------|-------|------|-------|
| 1 | Conta A (origem) | Débito | R$ 100,00 |
| 2 | Conta B (destino) | Crédito | R$ 100,00 |

### Proibição explícita

É **terminantemente proibido** alterar saldo diretamente na entidade de conta:

```java
// PROIBIDO — viola ADR-0006 e AGENTS.md Rule 3
account.setBalance(new BigDecimal("1000.00"));
```

Operações válidas passam pelo `LedgerPort` (ou use case equivalente) que registra lançamentos e, opcionalmente, atualiza projeção de saldo derivada.

### Fluxo obrigatório

```text
UseCase → validar regras de domínio → LedgerPort.registrar(débito + crédito) → persistir → publicar evento
```

### Operações cobertas

| Operação | Débito | Crédito |
|----------|--------|---------|
| Transferência | Conta origem | Conta destino |
| Recebimento PIX | Conta transitória / externa | Conta destino |
| Liquidação de cobrança | Conta transitória MP | Conta do cliente |
| Conciliação (ajuste) | Conforme regra de divergência | Conforme regra de divergência |

### Auditoria

Cada lançamento registra: `user`, `timestamp`, `correlationId`, `operation`, `transactionId`.

## Consequências

### Positivas

- Auditabilidade completa: qualquer saldo é explicável por extrato de lançamentos.
- Consistência financeira garantida por partidas dobradas (soma de débitos = soma de créditos por transação).
- Alinhamento com práticas de Core Banking e expectativas de recrutadores do setor financeiro.
- Base sólida para conciliação e relatórios regulatórios.

### Negativas

- Consulta de saldo requer agregação de lançamentos — latência maior que leitura de campo `balance`.
- Implementação mais verbosa que `setBalance()` direto.
- Projeção de saldo materializada pode ser necessária para performance (ideia adiada em STATE.md).

### Neutras

- `account-module` depende de `ledger-module` via port, nunca diretamente no domínio.
- Evento `LedgerEntryCreated` publicado após cada registro no Kafka (ADR-0003).
- Primeira implementação prevista na Sprint 2 (Ledger).
