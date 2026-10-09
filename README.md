# Nailly

Aplicativo de agendamento para salões de manicure. Projeto de estudo de microsserviços com Java/Spring Boot e Flutter.

## Estrutura do repositório (monorepo)

```
Nailly/
├── nailly-api/               API: clientes, agenda e autenticação (Spring Boot, Maven)
├── nailly-notification/      (planejado) consome o RabbitMQ e envia as notificações
├── app/                      (planejado) aplicativo Flutter
├── contracts/                (planejado) formato dos eventos entre serviços
├── infra/                    (planejado) docker-compose, Prometheus e Grafana
├── docs/                     guias e decisões
└── .github/workflows/        um workflow de CI por serviço
```

## Stack
- Java 17, Spring Boot, Maven, Spring Security (JWT), Spring Data JPA, Flyway
- MySQL (H2 em memória apenas nos testes)
- Flutter/Dart (app)
- RabbitMQ, Prometheus e Grafana (fases futuras)
- Arquitetura: Clean Architecture em cada serviço

## Rodando o build e os testes da API

```bash
cd nailly-api
./mvnw clean verify
```

Requer Java 17 ou superior.

## Qualidade e CI
- GitHub Actions: um workflow por serviço, disparado só quando a pasta do serviço muda
- Análise de código e cobertura no SonarQube Cloud, com Quality Gate bloqueando o merge

## Contribuição
Veja [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) para o padrão de branches, commits e Pull Requests.

## Ambientes e fluxo de branches
`feat/*` → `develop` (dev, squash) → `staging` (homologação, merge commit) → `main` (produção, merge commit).
A promoção para `production` exige aprovação manual. Detalhes em [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md).
