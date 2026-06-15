# Create Customer — Especificação

**Módulo:** `customer-module`
**Endpoint:** `POST /api/v1/customers`
**Sprint:** 1 — Core Banking
**Status:** Done

---

## Problem Statement

A plataforma financeira precisa de um cadastro confiável de clientes (pessoa física ou jurídica) antes de abrir contas ou executar operações. Sem validação rigorosa de CPF/CNPJ e unicidade de documento, o sistema permitiria cadastros inválidos ou duplicados, comprometendo conformidade cadastral e rastreabilidade.

## Goals

- [x] Permitir cadastro de cliente PF (CPF) ou PJ (CNPJ) via API REST
- [x] Validar documento usando value objects do shared-kernel (`Cpf`, `Cnpj`)
- [x] Garantir unicidade de documento no banco (rejeitar duplicatas)
- [x] Persistir cliente com identificador UUID e metadados de auditoria
- [x] Retornar envelope `{ data, metadata }` conforme convenção REST do projeto

## Out of Scope

| Feature | Reason |
| ------- | ------ |
| Atualização cadastral | Feature separada: `update-customer` |
| Consulta/listagem | Feature separada: `query-customers` |
| Autenticação/autorização | Sprint posterior; actor fixo `system` no S1 |
| Validação de endereço via Correios | Fora do escopo da POC |
| KYC / análise de risco | Fora do escopo v1 |
| Exclusão de cliente | Não previsto no Sprint 1 |

---

## User Stories

### P1: Cadastrar cliente com dados válidos ⭐ MVP

**User Story:** Como operador do backoffice, quero cadastrar um cliente com CPF ou CNPJ válido para vincular contas bancárias posteriormente.

**Why P1:** Sem cliente cadastrado não há fluxo de Core Banking.

**Acceptance Criteria**:

1. WHEN `POST /api/v1/customers` com body válido (nome, tipo PF/PJ, documento, e-mail) THEN sistema SHALL persistir cliente e retornar `201 Created` com `{ data: { id, name, type, document, email, createdAt }, metadata: {} }`
2. WHEN documento é CPF THEN sistema SHALL validar via `Cpf.of()` no domínio
3. WHEN documento é CNPJ THEN sistema SHALL validar via `Cnpj.of()` no domínio
4. WHEN cadastro é bem-sucedido THEN sistema SHALL gerar `Identifier` UUID para o cliente
5. WHEN cadastro é bem-sucedido THEN sistema SHALL registrar `createdAt` e `createdBy`

**Independent Test:** POST com CPF válido retorna 201; registro existe no PostgreSQL.

---

### P2: Rejeitar documento duplicado

**User Story:** Como operador, quero que o sistema impeça cadastro com CPF/CNPJ já existente para manter integridade cadastral.

**Why P2:** Duplicata quebra vínculo conta-cliente e auditoria.

**Acceptance Criteria**:

1. WHEN `POST /api/v1/customers` com documento já cadastrado THEN sistema SHALL retornar `409 Conflict` com Problem Details (`type`, `title`, `detail`)
2. WHEN documento duplicado THEN sistema SHALL NÃO persistir novo registro
3. WHEN conflito THEN `detail` SHALL indicar documento duplicado

**Independent Test:** Dois POSTs com mesmo CPF — segundo retorna 409.

---

### P3: Preparar evolução cadastral (nota de dependência)

**User Story:** Como product owner, quero que o cadastro inicial suporte campos extensíveis para atualização futura sem breaking change.

**Why P3:** Atualização cadastral virá em feature `update-customer`; modelo deve antecipar `updatedAt`/`updatedBy`.

**Acceptance Criteria**:

1. WHEN cliente é criado THEN entidade SHALL estender padrão auditável (`AuditableEntity` ou equivalente)
2. WHEN campos opcionais (telefone) forem omitidos THEN sistema SHALL aceitar cadastro mínimo
3. WHEN `update-customer` for implementado THEN SHALL reutilizar mesma tabela e agregado

**Independent Test:** Schema inclui colunas de auditoria; PATCH futuro não exige migration destrutiva.

---

## Edge Cases

- WHEN CPF/CNPJ com máscara (pontos, traços) THEN sistema SHALL normalizar antes de validar
- WHEN CPF/CNPJ inválido (checksum) THEN sistema SHALL retornar `400 Bad Request` sem persistir
- WHEN `type=INDIVIDUAL` com CNPJ THEN sistema SHALL retornar `400` (inconsistência tipo/documento)
- WHEN `type=COMPANY` com CPF THEN sistema SHALL retornar `400`
- WHEN nome vazio ou e-mail malformado THEN sistema SHALL retornar `400` com detalhes de validação
- WHEN body JSON malformado THEN sistema SHALL retornar `400`

---

## Requirement Traceability

| Requirement ID | Story | Phase | Status |
| -------------- | ----- | ----- | ------ |
| CUST-01 | P1: POST cadastro válido | Execute | Done |
| CUST-02 | P1: Validação Cpf/Cnpj VO | Execute | Done |
| CUST-03 | P1: Resposta 201 envelope | Execute | Done |
| CUST-04 | P1: Auditoria createdAt/By | Execute | Done |
| CUST-05 | P2: Unicidade documento | Execute | Done |
| CUST-06 | P2: Resposta 409 Conflict | Execute | Done |
| CUST-07 | P3: Modelo extensível | Execute | Done |
| CUST-08 | Edge: consistência tipo/doc | Execute | Done |

**Coverage:** 8 total, 8 mapped to tasks (T1–T11), 8 done

---

## Success Criteria

- [x] `POST /api/v1/customers` funcional com testes unitários (use case) e integração (controller + DB)
- [x] Documento inválido nunca persiste
- [x] Documento duplicado retorna 409 determinístico
- [x] Nenhuma regra de negócio em controller ou repository
- [x] Gate `mvn verify -Pintegration` passa para feature
