# Local development

## Prerequisites

- Java 21 (the Gradle wrapper downloads Gradle itself)
- Node.js 22
- Docker with Compose

## Database

```sh
docker compose -f infra/compose.yaml up -d postgres
```

This starts PostgreSQL 17 on `localhost:5432` with the control-plane database
`gymflow_control` and the dev-only user `gymflow` / `gymflow`. Data persists in the
`postgres-data` volume; `docker compose -f infra/compose.yaml down -v` wipes it.

## API

```sh
cd apps/api
./gradlew bootRun     # http://localhost:8080, "local" profile
./gradlew test        # needs the database above
./gradlew build       # compile, test and package
```

Control-plane migrations run automatically on startup. Useful URLs:

- http://localhost:8080/actuator/health
- http://localhost:8080/api/v1/system/info
- http://localhost:8080/swagger-ui.html

### Configuration

| Variable | Default (local profile) | Purpose |
| --- | --- | --- |
| `GYMFLOW_CONTROL_DB_URL` | `jdbc:postgresql://localhost:5432/gymflow_control` | Control-plane JDBC URL |
| `GYMFLOW_CONTROL_DB_USERNAME` | `gymflow` | Control-plane user |
| `GYMFLOW_CONTROL_DB_PASSWORD` | `gymflow` | Control-plane password |
| `GYMFLOW_CONTROL_DB_POOL_SIZE` | `10` | Control-plane pool size |
| `GYMFLOW_API_DOCS_ENABLED` | `true` | Serve `/v3/api-docs` and Swagger UI |
| `PORT` | `8080` | HTTP port |

Outside the `local` and `test` profiles there are no database defaults: the API fails to
start until the variables are set. Never commit real credentials.

The integration tests create and drop a throwaway tenant database
(`gymflow_tenant_test`), so the test user needs `CREATEDB`. The compose user is a
superuser, which covers this.

## Web

```sh
cd apps/web
npm ci
npm run dev          # http://localhost:5173, proxies /api and /actuator to :8080
npm test
npm run lint && npm run typecheck && npm run format:check
npm run gen:api      # regenerate src/lib/api-types.ts after editing contracts/openapi.yaml
```

Set `GYMFLOW_API_URL` to point the dev proxy at a different API.

## Everything in containers

```sh
docker compose -f infra/compose.yaml up --build
```

The web app is on http://localhost:5173 (nginx, proxying `/api` to the API container) and
the API on http://localhost:8080.
