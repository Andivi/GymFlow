# GymFlow

GymFlow is a multi-tenant SaaS for gyms: member records, membership plans and
subscriptions, expiry reminders over SMS and WhatsApp, and renewal payments through
Hubtel. Each gym gets its own PostgreSQL database, located through a shared
control-plane database.

## Repository layout

| Path | What it holds |
| --- | --- |
| `apps/api` | Spring Boot 4 API (Java 21, Gradle) |
| `apps/web` | React + TypeScript staff dashboard and public renewal pages (Vite) |
| `contracts/openapi.yaml` | The reviewed API contract; the web app generates its types from it |
| `infra/` | Docker Compose for local development, Dockerfiles, deployment notes |
| `docs/` | Architecture, API conventions and runbooks |

## Quick start

Requirements: Java 21, Node.js 22 and Docker.

```sh
# 1. Start PostgreSQL
docker compose -f infra/compose.yaml up -d postgres

# 2. Run the API on http://localhost:8080 (uses the "local" profile)
cd apps/api && ./gradlew bootRun

# 3. Run the web app on http://localhost:5173 (proxies /api to the API)
cd apps/web && npm ci && npm run dev
```

Or run everything in containers with `docker compose -f infra/compose.yaml up --build`.

See [docs/runbooks/local-development.md](docs/runbooks/local-development.md) for tests,
configuration and troubleshooting.

## Documentation

- [Architecture](docs/architecture.md)
- [API conventions](docs/api.md)
- [Local development](docs/runbooks/local-development.md)
