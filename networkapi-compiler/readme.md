# networkapi-compiler
### Neural Network Compiler REST API for Alexandra

`networkapi-compiler` is an independent REST service that provides access to **parsing and compilation of neural networks** described in **NureonLang** into a formal internal representation (`NetworkModel`, IR).

The module is part of the **Alexandra** ecosystem, but is designed as a **standalone Compiler-as-a-Service**, accessible via a clear and stable HTTP API.

---

## Module Purpose

In Alexandra's architecture, neural network compilation is a **separate engineering responsibility** that:

- relies on DSL parsing, ANTLR grammars, and multi-stage transformations,
- must be isolated from runtime, training, and orchestration concerns,
- must not be duplicated across services.

`networkapi-compiler` solves this by exposing a **strict REST contract** for:

- network compilation,
- syntax parsing,
- early validation,
- UI / IDE / CI integrations.

---

## What the Service Does

The service provides HTTP endpoints for:

### ✔ Parsing
- syntax parsing of NureonLang sources,
- early detection of syntax errors,
- structured error reporting (line, column, message).

### ✔ Compilation
- full compilation pipeline:
    - parsing,
    - semantic and structural validation,
    - macro expansion,
    - conditional and loop resolution,
    - network graph construction,
- returns a compiled `NetworkModel` or structured compilation errors.

---

## What the Service Does NOT Do

Clear responsibility boundaries:

- ❌ does not execute networks,
- ❌ does not train models,
- ❌ does not store weights,
- ❌ does not manage registries or versions,
- ❌ does not make network selection decisions.

This is a **pure compiler service**, not a runtime or orchestrator.

---

## Architecture

The module follows **Clean Architecture** principles:

- `application/usecase` — compilation business logic,
- `application/port` — input/output contracts,
- `web` — REST adapter (Spring),
- no dependencies from use cases to web or infrastructure.

Controllers are **thin HTTP adapters** and contain no business logic.

---

## REST API

### Base URL
```
/api/v1/compiler
```

### Endpoints

#### `POST /compile`
Compiles a neural network source into a `NetworkModel`.

- **Request**: `CompileRequestDTO`
- **Response (200)**: `CompileResponseDto`
- **Response (400)**: compilation errors (syntax / semantic / structural)

#### `POST /parse`
Performs syntax parsing without full compilation.

Use cases:
- UI validation,
- IDE feedback,
- early error detection.

- **Response (200)**: source is syntactically valid
- **Response (400)**: syntax errors detected

Full request/response contracts are available via Swagger UI.

---

## Swagger / OpenAPI

The module is fully documented using OpenAPI (Swagger):

- all endpoints documented,
- all request / response DTOs annotated,
- examples included.

Swagger UI is available at the standard Spring Boot endpoint:
```
/swagger-ui.html
```

Swagger acts as a **formal API contract** between Alexandra modules.

---

## Why REST

REST was chosen deliberately:

- compilation is a synchronous operation,
- no streaming or ultra-low latency requirements,
- low to moderate request frequency,
- no need for binary protocols.

gRPC or message queues may be added in the future if requirements change,  
but are **intentionally avoided** at this stage to keep the system simple and explicit.

---

## Interaction with Other Alexandra Modules

Other Alexandra components (Oracle, UI, Trainer, Registry):

- send neural network source code to `networkapi-compiler`,
- receive compiled network models or structured errors,
- do **not** depend on DSL grammar, parsing, or IR internals.

This enables:
- independent language evolution,
- separate scaling of the compiler,
- strict separation of responsibilities.

---

## Module Status

- Architecture: ✔ Production-ready
- API contracts: ✔ Stable
- Swagger documentation: ✔ Complete
- Role: ✔ Compiler-as-a-Service

The module is ready for integration as part of the Alexandra Platform.

---

## Related Modules

- **NureonLang Compiler** — DSL compiler implementation
- **NureonLang Core** — base symbolic and structural abstractions
- **NureonLang IR** — intermediate representation
- **Oracle** — system orchestrator

---

## Summary

`networkapi-compiler` is a **clean, isolated, deterministic REST compiler service**  
designed to serve as the compilation backbone of the Alexandra neural ecosystem.
