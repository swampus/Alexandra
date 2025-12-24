# networkapi-orchestrator (Clean Architecture)

Generated scaffold aligned with Alexandra style:
- `domain` — pure domain types (no Spring)
- `application` — use cases + ports (no Spring)
- `infrastructure` — adapters (HTTP clients) + Spring configuration
- `web` — Spring Boot REST API (controllers only)

## Endpoints (initial)
- `POST /api/v1/networks/validate`
- `POST /api/v1/networks/compile`
- `POST /api/v1/networks` (validate -> compile -> register)
- `GET  /api/v1/networks/{id}`
- `GET  /api/v1/networks`

## Notes
- DTOs are placeholders. Replace with your `api-dto-contract` / artifact contract types.
- All orchestration logic lives in `application` use cases.
