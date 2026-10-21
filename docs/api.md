# API conventions

## Contract

`contracts/openapi.yaml` is the reviewed contract. Update it in the same change as the
endpoint it describes, then run `npm run gen:api` in `apps/web` to regenerate
`src/lib/api-types.ts`. CI fails if the generated types are out of date.

When `GYMFLOW_API_DOCS_ENABLED=true` (local, test and compose), the running API serves its
own generated document at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`. It is off
by default elsewhere.

## Paths

- Staff APIs live under `/api/v1/...` and will require authentication.
- Platform administration lives under `/api/v1/platform/...`.
- Provider callbacks live under `/api/v1/webhooks/...`. Reaching a webhook handler does not
  make an event trusted: verify it with the provider before acting on it.
- Health is `/actuator/health`, with `/actuator/health/liveness` and
  `/actuator/health/readiness` for orchestrators.

## Errors

Every error is an RFC 9457 problem document served as `application/problem+json`, with a
stable `code` that clients can switch on:

| code | Status | Meaning |
| --- | --- | --- |
| `VALIDATION_FAILED` | 400 | Request body failed validation; `errors` lists `{field, message}` |
| `BAD_REQUEST` | 4xx | Malformed request, unsupported method or media type |
| `NOT_FOUND` | 404 | Unknown route, or a resource not found in the current gym |
| `INTERNAL_ERROR` | 500 | Unexpected failure; details are logged, never returned |

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/api/v1/members",
  "code": "VALIDATION_FAILED",
  "errors": [{ "field": "phoneE164", "message": "must match \"^\\+[1-9]\\d{7,14}$\"" }]
}
```

Error responses never include stack traces, SQL, hostnames, credentials or tenant routing
details.
