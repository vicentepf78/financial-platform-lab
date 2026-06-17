# AI PR Review

Este diretório contém um template reutilizável de GitHub Actions para revisar pull requests com IA. Ele não assume que a branch alvo seja `main`: o workflow roda para PRs abertos contra qualquer branch do repositório.

- Workflow: [`.github/workflows/ai-pr-review.yml`](../.github/workflows/ai-pr-review.yml)
- Script: [`.github/scripts/ai-pr-review.mjs`](../.github/scripts/ai-pr-review.mjs)
- Prompt genérico: [`.github/prompts/pr-review.md`](../.github/prompts/pr-review.md)
- Overlay opcional do projeto: [`.github/prompts/pr-review.overlay.md`](../.github/prompts/pr-review.overlay.md)

## Quando roda

O workflow executa em pull requests do GitHub nos eventos:

- `opened`
- `synchronize`
- `reopened`
- `ready_for_review`

Também pode ser executado manualmente em **Actions → AI PR Review → Run workflow**, informando o número do PR.

> GitHub Actions trabalha com pull requests. Merge requests do GitLab exigem um `.gitlab-ci.yml` equivalente usando as variáveis de MR do GitLab, como `CI_MERGE_REQUEST_IID` e `CI_MERGE_REQUEST_TARGET_BRANCH_NAME`.

## O que a IA revisa

O prompt segue a skill `pr-review` e roda seis revisões independentes em paralelo:

- Segurança
- Requisitos / spec
- Testes
- Arquitetura
- Regressões / alucinações
- Performance

Os comentários são consultivos. O workflow não aprova, não solicita mudanças, não faz merge e não altera arquivos.

## Branch alvo

O workflow usa a branch base real do PR:

- PR para `develop`: revisa o diff contra `develop`.
- PR para `release/1.0`: revisa o diff contra `release/1.0`.
- PR para `main`: revisa o diff contra `main`.

Use a estratégia de branches do projeto. O template não força PR para `main`.

## Guardrails

O script aplica filtros antes de publicar qualquer comentário:

- Só comenta em linhas adicionadas pelo PR.
- Ignora achados com confiança menor que `0.8`.
- Evita duplicar comentários próximos já publicados por reviews anteriores.
- Executa a versão do script do `base.sha`, não a versão alterada pelo PR.
- Lê o diff via API do GitHub; não executa código do PR.

No primeiro PR que instala este workflow, os arquivos de review ainda não existem na branch base. Nesse caso, o job é encerrado sem executar a revisão IA. Após o merge desse PR, os próximos PRs passam a executar normalmente.

## Como reutilizar em qualquer projeto

Copie estes arquivos para o repositório que receberá a automação:

```text
.github/
├── workflows/ai-pr-review.yml
├── scripts/ai-pr-review.mjs
└── prompts/
    ├── pr-review.md
    └── pr-review.overlay.md   # opcional
```

O arquivo `pr-review.md` é genérico. Use `pr-review.overlay.md` quando o projeto tiver regras próprias, como padrões de arquitetura, regras financeiras, estilo de testes ou convenções de API.

## Configuração no GitHub

Por padrão, o workflow usa GitHub Models:

- `AI_BASE_URL`: `https://models.github.ai/inference`
- `AI_MODEL`: `openai/gpt-4o-mini`
- Credencial: `GITHUB_TOKEN`

Habilite GitHub Models no repositório ou organização e mantenha as permissões do workflow:

```yaml
permissions:
  contents: read
  pull-requests: write
  issues: write
  models: read
```

### Variáveis opcionais

Configure em **Settings → Secrets and variables → Actions → Variables**:

| Variável | Padrão | Uso |
| -------- | ------ | --- |
| `AI_REVIEW_MODEL` | `openai/gpt-4o-mini` | Modelo usado na revisão |
| `AI_REVIEW_BASE_URL` | `https://models.github.ai/inference` | Endpoint compatível com OpenAI chat completions |
| `AI_REVIEW_PROMPT_PATH` | `.github/prompts/pr-review.md` | Prompt base |
| `AI_REVIEW_OVERLAY_PATH` | `.github/prompts/pr-review.overlay.md` | Regras específicas do projeto |
| `AI_REVIEW_CONTEXT_PATHS` | contexto padrão do repo | Lista separada por vírgula de arquivos/globs de contexto |
| `AI_REVIEW_DIMENSIONS` | `security,requirements,tests,architecture,regression,performance` | Dimensões de review |
| `AI_REVIEW_COMMENT_MARKER` | `ai-pr-review` | Marcador invisível para evitar duplicações |
| `AI_REVIEW_MAX_CONTEXT_CHARS` | `12000` | Limite de contexto do repositório enviado ao modelo |
| `AI_REVIEW_MAX_PATCH_CHARS` | `20000` | Limite de diff enviado ao modelo |
| `AI_REVIEW_MAX_REQUEST_TOKENS` | `6000` | Orçamento total estimado por chamada ao modelo (ajuste para limites do provider) |
| `AI_REVIEW_MAX_COMMENTS` | `40` | Limite de comentários inline por execução |

Quando `AI_REVIEW_CONTEXT_PATHS` não é informado, o script carrega apenas os arquivos essenciais do projeto (`AGENTS.md`, overlay de review, `.rules/*` principais e `.specs/codebase/*.md`), evitando enviar centenas de specs ao modelo.

Para este projeto, uma configuração explícita recomendada é:

```text
AGENTS.md,.github/prompts/pr-review.overlay.md,.rules/**/*.md,.specs/codebase/*.md
```

> **Limite do GitHub Models (`gpt-4o-mini`):** o corpo da requisição é limitado a ~8000 tokens. O script aplica truncamento por orçamento (com base no JSON serializado), faz shrink automático antes da chamada e retry em erro 413. O workflow faz checkout da **ponta atual** da branch base do PR (via API), não do snapshot congelado do evento — assim um *Re-run* após merge de correções em `main` usa o script atualizado. Para forçar nova execução após merge na base, também pode fazer push na branch do PR ou usar *workflow_dispatch*.

### Provider externo

Para usar outro endpoint compatível com OpenAI:

1. Configure a variável `AI_REVIEW_BASE_URL`.
2. Configure a variável `AI_REVIEW_MODEL`.
3. Configure o secret `AI_REVIEW_API_KEY`.

## Proteção de branches

Configure regras no GitHub para as branches que recebem PRs, não apenas `main`.

Exemplos de padrões:

- `develop`
- `release/*`
- `hotfix/*`
- `main`

Em **Settings → Rules → Rulesets** ou **Settings → Branches**, exija PR antes de merge e, se fizer sentido, exija os checks de CI. A review IA é consultiva; para bloquear merge por achado da IA, seria necessário transformar o resultado em check obrigatório, não apenas comentário.

## Fluxo recomendado

1. Atualize a branch alvo do trabalho, como `develop`, `release/*`, `hotfix/*` ou `main`.
2. Crie uma branch por feature a partir dessa branch alvo: `feature/nome-da-feature`.
3. Faça commits atômicos por task.
4. Abra um PR/MR para a branch de integração combinada pelo time.
5. Aguarde a review IA e os checks.
6. Corrija achados relevantes na mesma branch.
7. Faça merge somente após revisão humana.
