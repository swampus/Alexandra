# networkapi-artifact

## Table of Contents

- [Overview](#overview)
- [Motivation](#motivation)
- [Responsibilities](#responsibilities)
  - [What this module DOES](#what-this-module-does)
  - [What this module DOES NOT do](#what-this-module-does-not-do)
- [Architecture](#architecture)
  - [Core Concepts](#core-concepts)
- [Versioning Strategy](#versioning-strategy)
- [Public API](#public-api)
- [Dependencies](#dependencies)
- [Testing Strategy](#testing-strategy)
- [Design Principles](#design-principles)
- [Typical Usage](#typical-usage)
- [Status](#status)

---

## Overview

`networkapi-artifact` is a dedicated module responsible for **stable serialization and deserialization** of compiled neural network models into a versioned *runtime artifact* format.

It represents a strict architectural boundary between the **compiler layer** and **runtime / registry / executor layers**, ensuring that compiled models can be safely stored, transferred, and executed without depending on compiler internals.

```
NetworkModel (compiler)
    → JSON artifact
        → RuntimeNetworkModel (runtime)
```

---

## Motivation

Alexandra consists of multiple conceptual layers:

- **Compiler model** — rich, mutable, compiler-oriented representation (`NetworkModel`)
- **Runtime model** — minimal, immutable, execution-oriented representation
- **Registry / Storage** — persistent storage of compiled networks
- **Executor** — runtime execution engine

Direct coupling between these layers would:
- introduce cyclic dependencies,
- break backward compatibility,
- make long-term storage unsafe.

`networkapi-artifact` solves this by introducing an **explicit, versioned serialization contract**.

---

## Responsibilities

### What this module DOES

- Serializes compiled `NetworkModel` into a JSON runtime artifact
- Deserializes artifacts into immutable `RuntimeNetworkModel`
- Enforces versioned artifact formats (e.g. `artifact-v1`)
- Preserves structural and semantic runtime data:
  - layers
  - inputs / outputs
  - layer parameters
  - metadata

### What this module DOES NOT do

- ❌ Compile NureonLang
- ❌ Execute neural networks
- ❌ Manage storage or registry lifecycle
- ❌ Interpret graph execution semantics

---

## Architecture

### Core Concepts

- **Artifact**  
  A JSON representation of a compiled network suitable for runtime usage.

- **ArtifactSerializer (SPI)**  
  Versioned serializer interface used to encode and decode artifacts.

- **RuntimeNetworkModel**  
  Immutable runtime-safe representation of a network.

- **RuntimeLayer**  
  Lightweight runtime layer descriptor (name, type, params).

---

## Versioning Strategy

Each artifact format is explicitly versioned:

```
artifact-v1
artifact-v2 (future)
...
```

This allows:
- backward compatibility
- safe long-term storage
- gradual evolution of the runtime model

---

## Public API

### ArtifactSerializer SPI

```java
public interface ArtifactSerializer {

    String formatVersion();

    String serialize(NetworkModel model);

    RuntimeNetworkModel deserialize(String artifact);
}
```

### Current Implementation

```java
ArtifactSerializerV1
```

---

## Dependencies

### Required Dependencies

| Dependency | Purpose |
|-----------|--------|
| `networkapi-compiler` | Source of `NetworkModel` |
| `jackson-databind` | JSON serialization / deserialization |
| `lombok` | Immutable models, builders, `@Jacksonized` support |

### Dependency Direction (Important)

- `networkapi-artifact` **depends on compiler**
- Compiler **does NOT depend on artifact**
- Runtime / Registry **depend on artifact**, not compiler

This enforces Clean Architecture boundaries and prevents dependency cycles.

---

## Testing Strategy

The module is protected by **contract-level integration tests**.

### Core Invariant

> Any valid `NetworkModel` must successfully survive a round-trip:
>
> `NetworkModel → artifact → RuntimeNetworkModel`

### Tests Validate

- Structural integrity
- Input/output preservation
- Layer parameter preservation
- Metadata preservation
- Compatibility across refactors

Breaking changes in the artifact format are immediately detected.

---

## Design Principles

- **Immutability-first**
- **Explicit contracts**
- **Minimal runtime surface**
- **Versioned serialization**
- **Clean Architecture compliance**

---

## Typical Usage

```java
ArtifactSerializer serializer = new ArtifactSerializerV1();

String artifact = serializer.serialize(networkModel);

// store artifact (registry, filesystem, S3, etc.)

RuntimeNetworkModel runtime = serializer.deserialize(artifact);
```

---

## Status

- ✔ Stable
- ✔ Covered by integration tests
- ✔ Ready for Registry / Executor usage
- ✔ Safe for long-term artifact storage
