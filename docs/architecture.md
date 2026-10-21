# Architecture

This is a summary of the decisions that shape the codebase. Each section names the rule
the code is expected to follow.

## Components

- **React staff dashboard** (`apps/web`): one application, gym-scoped access. The public
  renewal flow (`/pay/:token`) is kept separate from the authenticated `/app` routes.
- **Spring Boot API** (`apps/api`): authentication, tenant resolution, business modules
  and provider adapters in a single deployable. Split into services only when there is a
  measurable reason.
- **Control-plane database**: one shared PostgreSQL database holding `gyms`, `users`,
  `gym_users`, `tenant_database_registry`, `saas_subscriptions` and platform audit records.
- **Tenant databases**: one PostgreSQL database per gym holding members, plans,
  subscriptions, payments, reminders, notification logs, outbox events and gym audit records.
- **Providers**: Hubtel for payments and SMS, Meta WhatsApp Business Platform for
  WhatsApp, each behind an adapter interface.

## Tenancy rules

- The control plane stores a logical database key and a secret reference for each gym,
  never a plaintext database password.
- A staff request is authenticated, the user's active gym membership is verified in the
  control plane, the gym's status is checked, and only then is the tenant datasource
  selected. The browser never names a gym or database.
- One request has exactly one tenant boundary. Never switch databases inside a
  transaction. Background jobs establish their own tenant context.
- Tenant datasources come from a bounded, per-tenant pool registry. They are not Spring
  beans.
- Provisioning uses a privileged infrastructure identity. The runtime database account
  cannot create databases.

## Migrations

The two schemas have separate Flyway locations and separate history tables:

| Schema | Location | History table | Runs |
| --- | --- | --- | --- |
| Control plane | `db/migration/control` | `flyway_control_history` | On API startup, against the primary datasource |
| Tenant | `db/migration/tenant` | `flyway_tenant_history` | Per gym, against that gym's datasource, via `TenantMigrations` |

Spring Boot's Flyway auto-configuration is deliberately not used (see
`com.gymflow.tenancy.migration.MigrationConfig`). `TenantMigrations` refuses to run
against the control-plane datasource, or against any database that already holds the
control-plane history table.

The starter migrations are not production-ready. Before production, add composite keys
or equivalent checks tying a payment's member to its subscription's member, renewal
request tokens, reminder deduplication, notification and outbox tables, refunds and
reconciliation, and audit records.

## Backend module layout

Domain-oriented packages under `com.gymflow`, each with four layers:

- `api`: controllers and request/response DTOs. Controllers validate input and call
  application services.
- `application`: use cases, transaction boundaries and orchestration. Only application
  services coordinate changes spanning payments and subscriptions.
- `domain`: entities, value objects and business rules.
- `infrastructure`: repositories, provider adapters and external integrations.

Clients never send authoritative prices, statuses, end dates or gym ids. The server
derives them.

## Money

Amounts are integer minor units (`*_minor` columns, `long` in Java, `number` in the web
app for display only). Never use floating point for payment calculations. Gym membership
revenue and GymFlow's own SaaS revenue are separate flows with separate records.

## Build order

1. Foundation: this repository skeleton, CI, migrations wiring, error handling, OpenAPI.
2. Tenancy: provisioning, identity, staff roles, datasource registry. Gym A staff
   cannot reach Gym B data.
3. Membership: members, plans, subscriptions, without payments.
4. Automation: expiry checks, reminder stages, consent records, outbox, fake providers.
5. Payments: Hubtel hosted checkout, verified callbacks, atomic subscription updates.
6. Messaging: Hubtel SMS and Meta WhatsApp.
7. Commercialize: SaaS billing, monitoring, backups and provisioning runbooks.

## Open questions

- **Hubtel merchant model** (blocks step 5): can GymFlow collect for many independent
  gyms, or does each gym need its own merchant account and credentials?
- **Hubtel callback authenticity**: is there a signature, or must every callback be
  confirmed through the transaction status API?
- **Identity provider** (step 2): an OIDC provider with HTTP-only session cookies is
  preferred over building password reset, revocation and MFA in-house.
- **WhatsApp templates and pricing** for renewal reminders.
