# Nailly

Aplicativo de agendamento para salões de manicure. Clientes agendam serviços; a profissional gerencia agenda, serviços e clientes.

Também é um projeto de estudo de microsserviços: cada serviço é independente e se comunica por API REST e por eventos no RabbitMQ.

## Status

| Parte | Situação |
|---|---|
| `nailly-api` | Em desenvolvimento: schema inicial do banco e perfis por ambiente prontos |
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

- **API:** Java 17, Spring Boot 4.1, Maven Wrapper, Spring Data JPA, Bean Validation, Spring Security (JWT em breve)
- **Banco:** MySQL 8.4 com migrations no Flyway; H2 em memória só nos testes
- **Qualidade:** JaCoCo (cobertura) e SonarQube Cloud (Quality Gate)
- **Fases futuras:** Flutter/Dart, RabbitMQ, Prometheus e Grafana
- **Arquitetura:** Clean Architecture em cada serviço (`domain`, `application`, `infra`)

## Funcionalidades previstas

- Cada profissional cadastra os próprios serviços (técnica, mãos/pés, duração, preço, atendimento a domicílio)
- Agendamento de vários serviços em um único horário (combo), no salão ou a domicílio
- Aprovação automática ou manual do pedido, com expiração de pedidos pendentes
- Taxa de deslocamento por faixa de distância para atendimento a domicílio
- Controle de horário de funcionamento e conflitos de agenda

## Rodando a API localmente

Requisitos: Java 17 ou superior e Docker.

1. Suba um MySQL 8.4 (exemplo):

   ```bash
   docker run -d --name nailly-mysql -p 3306:3306 \
     -e MYSQL_ROOT_PASSWORD=<sua-senha> -e MYSQL_DATABASE=nailly mysql:8.4
   ```

2. Crie o arquivo `nailly-api/.env` (já está no `.gitignore`, nunca versione):

   ```properties
   DB_PASSWORD=<sua-senha>
   ```

   Por padrão o perfil `local` usa `jdbc:mysql://localhost:3306/nailly` e o usuário `root`. Para mudar, defina `DB_URL` e `DB_USERNAME` no mesmo arquivo.

3. Rode a aplicação. O Flyway cria as tabelas na primeira subida:

   ```bash
   cd nailly-api
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

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
| `local` | Máquina do desenvolvedor, lê o `.env` |
| `develop`, `staging`, `prod` | Ambientes do GitHub (dev, homologação e produção) |

O `application.yml` não tem valores padrão: `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` vêm sempre de variáveis de ambiente. Os arquivos de perfil só ajustam comportamento (logs etc.) e nunca guardam segredos.

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
