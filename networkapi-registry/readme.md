# Network API Registry

## Table of Contents
- [Overview](#overview)
- [Canonical Artifact Payload](#canonical-artifact-payload)
- [Explicitly NOT Stored in Registry](#explicitly-not-stored-in-registry)
- [Architectural Layers](#architectural-layers)
- [Lifecycle Flow](#lifecycle-flow)
- [Registry Responsibilities](#registry-responsibilities)
- [Storage Model](#storage-model)
- [Serialization Contract](#serialization-contract)
- [Use Case Design](#use-case-design)
- [DTO Policy](#dto-policy)
- [Rationale for Module Separation](#rationale-for-module-separation)
- [Module Responsibilities Overview](#module-responsibilities-overview)
- [Why RuntimeNetworkModel Lives in artifact Module](#why-runtimenetworkmodel-lives-in-artifact-module)
- [Why Registry Does Not Depend on Network Semantics](#why-registry-does-not-depend-on-network-semantics)
- [Why DTOs Are Not Artifacts](#why-dtos-are-not-artifacts)
- [Explicit Boundary Enforcement](#explicit-boundary-enforcement)
- [Long-Term Benefits of This Design](#long-term-benefits-of-this-design)
- [Summary](#summary)

---

## Overview

`networkapi-registry` is a low-level storage and indexing module responsible for
persisting compiled neural network artifacts in Alexandra.

The registry **does not understand network semantics**.
It stores and retrieves opaque binary payloads together with searchable metadata.

This module intentionally separates:
- compilation
- runtime execution
- API transport
- physical storage

to ensure long-term maintainability and architectural stability.

---

## Canonical Artifact Payload

**Registry payload is strictly defined as:**

> **`RuntimeNetworkModel` (serialized)**

```
io.github.swampus.alexandra.networkapi.artifact.model.RuntimeNetworkModel
```

This decision is **intentional and enforced**.

### Why `RuntimeNetworkModel`?

- Immutable and thread-safe
- Jackson-serializable
- Free of compiler internals
- Free of API / UI concerns
- Stable across system boundaries
- Suitable for long-term storage

The registry treats the payload as an **opaque binary blob** and does not
inspect, mutate or interpret its contents.

---

## Explicitly NOT Stored in Registry

The following objects must **never** be stored directly in the registry:

- `NetworkModel` (compiler, mutable, build-time)
- `NNetworkDto` (transport / API DTO)
- Any compiler IR, AST or execution state
- Any framework-specific or runtime-bound object

---

## Architectural Layers

The system operates with **four distinct representations** of a neural network:

```
1. NetworkModel
   (compiler, mutable, build-time)

2. RuntimeNetworkModel
   (artifact payload, immutable, serializable)

3. StoredArtifact
   (registry storage: metadata + byte[] payload)

4. NNetworkDto
   (API / transport / orchestration view)
```

Each representation has a **single responsibility** and must not replace others.

---

## Lifecycle Flow

```
NureonLang source
   ↓
NetworkModel
   ↓ (compiler mapping)
RuntimeNetworkModel
   ↓ (serialization)
byte[]
   ↓
Registry storage
   ↓
byte[]
   ↓ (deserialization)
RuntimeNetworkModel
   ↓ (API mapping)
NNetworkDto
```

There is **no reverse flow** from DTO or registry back into compiler models.

---

## Registry Responsibilities

The registry is responsible for:

- Persisting binary artifact payloads
- Maintaining searchable metadata indices
- Supporting multiple storage backends
- Providing stable CRUD and search operations

The registry is **not** responsible for:

- Compilation
- Validation
- Optimization
- Execution
- DTO shaping
- Business logic

---

## Storage Model

### StoredArtifact

Conceptually, each stored entry consists of:

```
StoredArtifact
├── metadata   (indexed, searchable)
└── payload    (byte[] = serialized RuntimeNetworkModel)
```

### Metadata

Metadata is used **only for indexing and search**, e.g.:

- artifactId
- clusterId
- task names
- tags
- lifecycle state
- timestamps

Payload contents are never inspected during search.

---

## Serialization Contract

A **single, canonical serializer** must be used for artifact payloads:

- Jackson-based
- Version-tolerant
- Deterministic

All serialization and deserialization of `RuntimeNetworkModel`
must go through this serializer.

---

## Use Case Design

Use cases are split according to intent:

### Command Use Cases
- Register artifact
- Update metadata
- Upload payload
- Delete artifact

### Query Use Cases
- Get artifact by ID
- Search by cluster
- Search by task
- Search by tags
- Filter by state

Command and query responsibilities must not be mixed.

---

## DTO Policy

Shared DTOs (`api-dto-contract`) are used **only** for API boundaries.

DTOs:
- represent user intent
- describe request/response shape
- contain no business logic
- are not persisted as canonical artifacts

---

## Rationale for Module Separation

Alexandra intentionally separates neural network concerns into multiple
independent modules instead of using a single “network” abstraction.

This separation is **architectural**, not accidental.

### Why Not a Single Network Module?

A single, monolithic network representation inevitably leads to:

- tight coupling between compiler, runtime and API
- accidental dependencies on mutable or framework-specific state
- inability to evolve serialization formats safely
- fragile backward compatibility
- unclear ownership of responsibilities

In large systems, this quickly becomes unmaintainable.

---

## Module Responsibilities Overview

Each module in Alexandra owns **exactly one responsibility**.

```
networkapi-compiler
├── NetworkModel
│   - mutable
│   - compiler-internal
│   - build-time only
│   - NOT serializable for long-term storage
```

```
networkapi-artifact
├── RuntimeNetworkModel
│   - immutable
│   - runtime-ready
│   - serialization-safe
│   - stable across system boundaries
```

```
networkapi-registry
├── StoredArtifact
│   - metadata + byte[] payload
│   - storage- and backend-agnostic
│   - no knowledge of network semantics
```

```
api-dto-contract
├── NNetworkDto
│   - transport / orchestration view
│   - optimized for API usage
│   - NOT a canonical model
```

Each module **knows as little as possible** about the others.

---

## Why RuntimeNetworkModel Lives in artifact Module

`RuntimeNetworkModel` represents the **only format that is allowed to cross
system boundaries**.

It is placed in a dedicated `artifact` module because:

- it must not depend on compiler internals
- it must not depend on API concerns
- it must not depend on storage backends
- it must remain stable over long periods of time

This makes it suitable for:

- long-term persistence
- caching
- replication
- transport between services
- offline storage

The registry stores **only serialized artifacts**, never higher-level objects.

---

## Why Registry Does Not Depend on Network Semantics

The registry is designed as a **generic artifact store**, not a neural network system.

It deliberately does **not** know:

- how networks are compiled
- how they are executed
- what layers mean
- how inference works
- how training happens

This allows:

- swapping compilers without touching storage
- changing runtime implementations independently
- adding new network types without registry changes
- using alternative artifact payloads in the future

The registry’s only invariant is:

> **payload is opaque, metadata is searchable**

---

## Why DTOs Are Not Artifacts

`NNetworkDto` exists purely for **API communication and orchestration**.

It may:
- aggregate data
- flatten structures
- expose convenience fields
- change shape over time

For this reason, DTOs:

- are never stored in registry
- are never used as canonical models
- are never deserialized back into compiler/runtime structures

This prevents accidental coupling between API evolution and storage stability.

---

## Explicit Boundary Enforcement

Alexandra enforces **one-way data flow** between modules:

```
compiler → artifact → registry → API
```

Reverse dependencies are intentionally forbidden.

This guarantees that:
- storage does not dictate API shape
- API changes do not corrupt stored artifacts
- compiler evolution does not break old models

---

## Long-Term Benefits of This Design

This modular separation enables:

- safe schema evolution
- backward compatibility
- multi-version coexistence
- independent scaling of subsystems
- clean testing boundaries
- clear ownership of responsibilities

While this design may appear verbose in early stages,
it prevents architectural debt at scale.

---

## Summary

- **Registry payload is always `RuntimeNetworkModel` (serialized).**
- Registry stores bytes and metadata, nothing more.
- Compiler, runtime, API and storage concerns are strictly separated.
- This design favors correctness, evolvability and operational safety
  over short-term convenience.
