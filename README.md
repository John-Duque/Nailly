# Nailly

Aplicativo de agendamento para salões de manicure. Clientes agendam serviços; a profissional gerencia agenda, serviços e clientes.

Também é um projeto de estudo de microsserviços: cada serviço é independente e se comunica por API REST e por eventos no RabbitMQ.

## Status

| Parte | Situação |
|---|---|
| `nailly-api` | Em desenvolvimento: endpoints REST de cadastro, login (JWT), catálogo, locais, horários e agendamentos prontos |
| `nailly-notification` | Planejado |
| `app` (Flutter) | Planejado |
| `contracts`, `infra` | Planejado |

## Estrutura do repositório (monorepo)

```
Nailly/
├── nailly-api/               API: clientes, agenda e autenticação (Spring Boot, Maven)
├── nailly-notification/      (planejado) consome o RabbitMQ e envia as notificações
├── app/                      (planejado) aplicativo Flutter
├── contracts/                (planejado) formato dos eventos entre serviços
├── infra/                    (planejado) docker-compose, Prometheus e Grafana
├── docs/                     guia de contribuição e rulesets do GitHub
└── .github/workflows/        CI por serviço, validação de branch e deploy
```

Cada serviço tem o próprio `pom.xml` (sem pom pai), banco, pipeline e projeto no Sonar. Serviços não compartilham código nem tabelas: conversam só por API REST ou pelos eventos descritos em `contracts/`.

## Stack

- **API:** Java 17, Spring Boot 4.1, Maven Wrapper, Spring Data JPA, Bean Validation, Spring Security com JWT (resource server)
- **Banco:** MySQL 8.4 com migrations no Flyway; H2 em memória só nos testes
- **Qualidade:** JaCoCo (cobertura) e SonarQube Cloud (Quality Gate)
- **Fases futuras:** Flutter/Dart, RabbitMQ, Prometheus e Grafana
- **Arquitetura:** Clean Architecture em cada serviço (`domain`, `application`, `infra`)

## Funcionalidades

Já disponíveis na `nailly-api`:

- Cadastro e login de clientes e profissionais, com token de acesso (JWT)
- Cada profissional cadastra os próprios serviços (técnica, mãos/pés, duração, preço, atendimento a domicílio), locais de atendimento e horário de funcionamento
- Clientes cadastram os próprios endereços para atendimento a domicílio
- Agendamento de vários serviços em um único horário (combo), no salão ou a domicílio
- Aprovação automática ou manual do pedido, com expiração automática de pedidos pendentes
- Taxa de deslocamento por faixa de distância para atendimento a domicílio
- Controle de horário de funcionamento e conflitos de agenda
- Agenda da profissional e lista de agendamentos da cliente por período

Previstas: notificações (`nailly-notification` via RabbitMQ), aplicativo Flutter e monitoramento (Prometheus e Grafana).

## Rodando a API localmente

Requisitos: Java 17 ou superior e Docker.

1. Suba um MySQL 8.4 no Docker. Na primeira vez, crie o container:

   ```bash
   docker run -d --name nailly-mysql -p 3306:3306 \
     -e MYSQL_ROOT_PASSWORD=<sua-senha> -e MYSQL_DATABASE=nailly mysql:8.4
   ```

   Nas próximas vezes, basta ligar o container que já existe: `docker start nailly-mysql`.
   Com esse container (porta 3306, banco `nailly`, usuário `root`), a API já sabe onde conectar.

2. Crie o arquivo `nailly-api/.env` a partir do modelo e preencha os valores (o `.env` está no `.gitignore`, nunca versione):

   ```bash
   cd nailly-api
   cp .env.example .env
   openssl rand -hex 32   # cole o resultado em JWT_SECRET=
   ```

   | Variável | O que é |
   |---|---|
   | `DB_URL`, `DB_USERNAME` | Opcionais no local: sem elas, a API usa o MySQL do Docker (`jdbc:mysql://localhost:3306/nailly` e `root`) |
   | `DB_PASSWORD` | Senha do MySQL (a mesma do `MYSQL_ROOT_PASSWORD`) |
   | `JWT_SECRET` | Segredo que assina os tokens de login, com pelo menos 32 caracteres |

   O [`nailly-api/.env.example`](nailly-api/.env.example) também explica como configurar essas variáveis em dev, staging e production, onde não existe `.env`.

3. Rode a aplicação. O Flyway cria as tabelas na primeira subida e a API fica em `http://localhost:8080`:

   ```bash
   cd nailly-api
   ./mvnw spring-boot:run
   ```

## Endpoints da API

Erros seguem o padrão Problem Details: 400 (validação, com a lista `errors`), 401 (sem token ou token inválido), 403 (recurso de outra pessoa), 404, 409 (conflito no banco) e 422 (regra de negócio).

**Autenticação.** Cadastro e login devolvem um token de acesso, válido por 8 horas. Nas rotas protegidas, envie o token no cabeçalho `Authorization: Bearer <token>`. Quem se cadastra é cliente (`CUSTOMER`) ou profissional (`PROFESSIONAL`) e só altera os próprios dados. O papel `ADMIN` já existe no banco, mas ainda não tem cadastro nem rotas.

| Método e rota | Quem pode |
|---|---|
| `POST /auth/register/customer`, `POST /auth/register/professional`, `POST /auth/login` | Público |
| `GET /procedure-types` | Público |
| `GET /professionals/{id}`, `/services`, `/locations`, `/working-hours` | Público |
| `PUT /professionals/{id}/profile`, `/approval-policy`, `/home-visit-policy`; `POST /professionals/{id}/deactivate` | A própria profissional |
| `GET /professionals/{id}/agenda?from=&to=` | A própria profissional |
| `POST`, `PUT /{serviceId}`, `POST /{serviceId}/deactivate` em `/professionals/{id}/services` | A própria profissional |
| `POST`, `PUT /{locationId}`, `POST /{locationId}/deactivate` em `/professionals/{id}/locations` | A própria profissional |
| `PUT /professionals/{id}/working-hours` | A própria profissional |
| `GET`, `PUT /customers/me`; `GET /customers/me/appointments?from=&to=` | A própria cliente |
| `GET`, `POST`, `PUT /{addressId}`, `POST /{addressId}/deactivate` em `/customers/me/addresses` | A própria cliente |
| `POST /appointments` | Cliente |
| `GET /appointments/{id}`, `POST /appointments/{id}/cancel` | Cliente ou profissional do agendamento |
| `POST /appointments/{id}/confirm`, `/reject`, `/complete`, `/no-show` | Profissional do agendamento |

Os parâmetros `from` e `to` são datas e horas em UTC no formato ISO 8601 (ex.: `2026-10-12T00:00:00Z`). Pedidos pendentes não respondidos a tempo passam para `EXPIRED` automaticamente (a rotina roda a cada minuto).

## Build e testes

```bash
cd nailly-api
./mvnw clean verify
```

O relatório de cobertura fica em `nailly-api/target/site/jacoco/index.html`.

Os testes usam H2 em memória com o Flyway desligado, então não validam as migrations. Teste SQL nova no MySQL local.

## Perfis e configuração

| Perfil | Uso |
|---|---|
| `test` | Testes automáticos (H2 em memória), ativado com `@ActiveProfiles("test")` |
| `dev`, `staging`, `prod` | Ambientes do GitHub (dev, homologação e produção) |

O `application.properties` lê `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` de variáveis de ambiente. Só `DB_URL` e `DB_USERNAME` têm valor padrão, apontando para o MySQL do Docker local; os segredos (`DB_PASSWORD` e `JWT_SECRET`) nunca têm. Os arquivos de perfil só ajustam comportamento (logs etc.) e nunca guardam segredos.

As migrations ficam em `nailly-api/src/main/resources/db/migration`. Uma migration já mesclada nunca é editada: toda mudança vira uma nova versão (`V2__...`).

## CI/CD

| Workflow | O que faz |
|---|---|
| `nailly-api-ci.yml` | Build, testes, cobertura e análise no Sonar com Quality Gate |
| `branch-name.yml` | Valida o nome da branch e o fluxo entre as branches de ambiente |
| `deploy.yml` | Deploy simulado para dev, staging e production (production exige aprovação manual) |

Os checks `Build, testes e Sonar` e `Validate Branch Name` são obrigatórios para fazer o merge.

## Ambientes e fluxo de branches

`feat/*` → `develop` (dev, squash) → `staging` (homologação, merge commit) → `main` (produção, merge commit)

## Contribuição

Veja [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) para o padrão de branches, commits (Conventional Commits) e Pull Requests.
