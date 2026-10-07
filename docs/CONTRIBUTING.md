# Guia de contribuição

## Branches
Formato: `tipo/descricao-curta-em-minusculas-com-hifen`

- `feat/cadastro-de-clientes`
- `fix/conflito-de-horarios`
- `ci/workflow-flutter`
- `chore/setup-inicial`

Nunca fazer commit direto na `main`.

## Commits (Conventional Commits)
Formato: `tipo(escopo): descrição curta`

Tipos: `feat`, `fix`, `refactor`, `test`, `docs`, `ci`, `chore`.
Escopos (opcionais): `api`, `notification`, `app`, `infra`, `ci`, `docs`.

Regras: tipo em inglês, descrição em português, no imperativo e com até 70 caracteres.

- `feat(api): cria endpoint de agendamento`
- `fix(api): corrige conflito de horários na agenda`
- `ci: adiciona workflow do app Flutter`

## Pull Requests
- O título segue o mesmo padrão dos commits (ele vira a mensagem do commit no squash merge).
- O merge só acontece com o pipeline "Build, testes e Sonar" verde.
- Usar squash merge e apagar a branch depois.

## Fluxo de trabalho
1. `git checkout main` e `git pull`
2. `git checkout -b feat/nome-da-funcionalidade`
3. Fazer os commits seguindo o padrão
4. `git push -u origin feat/nome-da-funcionalidade` e abrir o Pull Request
5. Esperar o pipeline e o Sonar passarem
6. Fazer o squash merge e apagar a branch

## Regra dos serviços
O nome da pasta de cada serviço é igual ao `artifactId` do seu `pom.xml`.
Cada serviço tem pom, pipeline, Dockerfile e projeto no Sonar próprios, e não compartilha código nem banco com os outros.
