# Blood Donation System

API REST para gerenciamento de doadores de sangue — Java 25 + Spring Boot 4.

## Requisitos

- Java 25
- Docker + Docker Compose

## Como executar

1. Suba o PostgreSQL:

```bash
docker compose up -d
```

2. Execute a aplicação (perfil padrão: `local`):

```bash
./gradlew bootRun
# Windows: gradlew.bat bootRun
```

3. Health check:

```
GET http://localhost:8080/actuator/health
```

## Configuração

A aplicação lê as seguintes variáveis de ambiente (valores padrão de desenvolvimento em `application-local.yml`):

| Variável | Descrição |
|---|---|
| `DB_URL` | URL JDBC do PostgreSQL |
| `DB_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Segredo para assinatura do JWT (mín. 32 bytes) |
| `JWT_EXPIRATION_SECONDS` | Expiração do token (padrão: 3600) |
| `APP_CORS_ORIGINS` | Origens permitidas para CORS, separadas por vírgula (local: `http://localhost:5173`) |

O PostgreSQL do docker-compose é exposto na porta **5433** (variável `POSTGRES_PORT`), para não conflitar com instâncias locais na 5432.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/auth/register` | Cadastro de usuário |
| POST | `/api/v1/auth/login` | Login e obtenção de JWT |
| GET | `/api/v1/users/me` | Perfil do usuário autenticado |
| PUT | `/api/v1/users/me` | Atualização do perfil |
| GET | `/api/v1/donors` | Busca paginada de doadores disponíveis (`bloodType`, `city`, `state`, `available`, `page`, `size`) |
| GET | `/api/v1/donors/{id}` | Perfil público de um doador disponível |
| PATCH | `/api/v1/donors/me/availability` | Ativar/desativar disponibilidade para doação |
| GET | `/api/v1/admin/users` | Lista paginada de usuários (somente `ADMIN`) |
| DELETE | `/api/v1/admin/users/{id}` | Excluir usuário (somente `ADMIN`) |

Endpoints protegidos exigem `Authorization: Bearer <JWT>`.

## Testes da API (Swagger UI e Postman)

- **Swagger UI:** `http://localhost:8080/swagger-ui.html` — documentação interativa; use o botão **Authorize** para informar o JWT (Bearer).
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Postman:** importe a collection diretamente pela URL `http://localhost:8080/v3/api-docs` (Postman → Import → Link).

### Criando um usuário ADMIN (apenas desenvolvimento local)

Conecte-se ao PostgreSQL do docker-compose e execute:

```sql
-- Senha: admin123 (troque em qualquer ambiente que não seja dev)
INSERT INTO users (email, password_hash, role)
VALUES ('admin@example.com', '$2a$10$Owrfy35C3GO8Ule9LLR7a.lv87bOOdqyxM6GEIFm32URZ2IMWX1sW', 'ADMIN');
```

Depois, faça login normalmente em `POST /api/v1/auth/login` com `admin@example.com` / `admin123` e use o token nos endpoints `/api/v1/admin/**`.

## Testes

```bash
./gradlew test
# Windows: gradlew.bat test
```

Os testes de integração usam Testcontainers (PostgreSQL real) e exigem Docker em execução.

## Banco de dados

As alterações estruturais são gerenciadas por Flyway em `src/main/resources/db/migration`.
