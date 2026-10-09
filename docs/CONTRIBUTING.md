# Guia de contribuição

## Ambientes e branches

| Branch | Ambiente | Recebe de | Merge |
|---|---|---|---|
| `develop` | dev (desenvolvimento) | branches de trabalho (`feat/*`, `fix/*`...) | squash |
| `staging` | homologação | somente `develop` | merge commit |
| `main` | produção | somente `staging` (ou `fix/*` em correção urgente) | merge commit |

Promoções entre branches de ambiente usam **merge commit**, nunca squash: o squash cria um commit novo no destino e as históricas passam a divergir, gerando conflitos que não deveriam existir.

## Branches de trabalho
Saem sempre da `develop`. Formato: `tipo/descricao-em-minusculas-com-hifen`

- `feat/cadastro-de-clientes`
- `fix/conflito-de-horarios`
- `ci/workflow-flutter`

Tipos: `feat`, `fix`, `refactor`, `test`, `docs`, `ci`, `chore`. Nunca commitar direto em `develop`, `staging` ou `main`.

## Commits (Conventional Commits)
Formato: `tipo(escopo): descrição curta`

Escopos (opcionais): `api`, `notification`, `app`, `infra`, `ci`, `docs`.
Regras: tipo em inglês, descrição em português, no imperativo e com até 70 caracteres.

- `feat(api): cria endpoint de agendamento`
- `fix(api): corrige conflito de horários na agenda`
- `ci: adiciona workflow do app Flutter`

## Pull Requests
- Título no mesmo padrão dos commits (vira a mensagem do commit no squash)
- Merge só com os checks `Build, testes e Sonar` e `Validate Branch Name` verdes
- Branches de trabalho: squash merge e apagar a branch (`gh pr merge --squash --delete-branch`)
- Promoções: merge commit, sem apagar a branch (`gh pr merge --merge`). Título: `chore: promove develop para staging`

## Fluxo do dia a dia
1. `git checkout develop` e `git pull`
2. `git checkout -b feat/nome-da-funcionalidade`
3. Commits no padrão e `git push -u origin feat/nome-da-funcionalidade`
4. `gh pr create --base develop --fill`, esperar os checks e fazer o squash merge
5. Promover: PR `develop` para `staging` e depois `staging` para `main`
6. O deploy para `production` espera aprovação manual em Actions > Review deployments

## Correção urgente em produção
1. Sai uma `fix/...` da `main`, entra na `main` por PR (merge commit)
2. Depois a correção é trazida de volta para `staging` e `develop`

## Regras no GitHub
Os rulesets (um por branch de ambiente) ficam documentados em `docs/rulesets/`. Eles não fazem parte dos arquivos do repositório: vivem nas configurações do GitHub.
