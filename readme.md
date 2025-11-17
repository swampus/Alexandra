# Alexandra — Modular Neural Architecture Ecosystem

_A next-generation system for constructing, compiling, orchestrating, and evolving populations of neural networks using domain-specific languages and a fully extensible architecture._

Alexandra is not a monolithic model.
It is an **ecosystem** of neural agents, DSL languages, compilers, orchestrators, rules, clusters, trainers, and runtime modules — all operating together through clear interfaces and formal specifications.

The core idea:
- Define networks in a high-level declarative language
- Compile into a structured IR
- Execute, train, and orchestrate them
- Allow multiple networks to cooperate, compete, and improve over time

---

## Table of Contents
1. [Introduction](#1-introduction)
2. [System Goals](#2-system-goals)
3. [Architecture Overview](#3-architecture-overview)
4. [Module Documentation Overview](#4-module-documentation-overview)
5. [Compilation Pipeline](#5-compilation-pipeline)
6. [Execution Model](#6-execution-model)
7. [Training](#7-training)
8. [Task & Rule Languages](#8-task--rule-languages)
9. [Network Clusters & Orchestration](#9-network-clusters--orchestration)
10. [Feature Checklist](#10-feature-checklist)
11. [Example](#11-example)
12. [Long-Term Vision](#12-long-term-vision)


---

## 1. Introduction

Alexandra is a modular neural-architecture ecosystem designed for constructing, managing, executing, and evolving large collections of neural networks.
The system provides a full pipeline—from high-level declarative descriptions (NureonLang) to execution, orchestration, selection rules, training and continuous improvement.

Alexandra treats neural networks as **independent agents** with their own:
- architecture definitions,
- performance metadata,
- task compatibility,
- selection rules,
- evolution history,
- and position inside multi-network pipelines.

This README presents the high-level architectural overview of the system.

For detailed module documentation see the dedicated READMEs for each subsystem:

- **[NureonLang Compiler](./nureonlang-compiler/nureonlang-compiler-readme.md)**  
  Full documentation of the compiler responsible for parsing, transforming, and building NetworkModel IR from `.nl` source files.

- **[NureonLang Core](./nureonlang-core/nureon-core-readme.md)**  
  Base abstractions for layers, shapes, symbolic expressions, and shared core types used across the compiler and runtime.

- **[NureonLang IR](./nureonlang-ir/nureonlang-ir-readme.md)**  
  The intermediate representation format, instruction model, and semantic rules used by the compiler and executor.

📘 For a quick hands-on introduction, see:
➡️ [GETTING_STARTED.md](./getting-started.md)

---



---

## 2. System Goals

### ✔ Declarativity
Architectures are defined through **NureonLang**, a domain-specific language supporting:
- layers
- modules
- macros
- expand blocks
- loops
- conditionals
- symbolic shapes
- arithmetic expressions

### ✔ Extensibility
Every part of the system is modular:
- new layer types
- new handlers
- new rule engines
- new cluster strategies
- new task processors

### ✔ Multi-Network Ecosystem
The system manages **populations** of networks:
- selecting
- routing
- combining
- retraining
- pruning
- evaluating
- evolving

### ✔ Clean Architecture
Modules are isolated and communicate through strict interfaces.

---

## 3. Architecture Overview

```
                                      +-------------------+
                                      |      User / UI    |
                                      +---------+---------+
                                                |
                                                v
                                         +--------------+
                                         |    Oracle    |
                                         +--------------+
                                           /     |     \
                                          v      |      v
                                 +----------+   |   +----------+
                                 | TaskLang |   |   | RuleLang |
                                 +----------+   |   +----------+
                                          \     |     /
                                           v    v    v
                                         +-------------------+
                                         |  Network Registry |
                                         +---------+---------+
                                                   |
                                                   v
                      +-----------+     +----------------------+     +--------------+
                      | Compiler  | --> |  Network Model (IR)  | --> |   Executor   |
                      +-----------+     +----------------------+     +--------------+
                                                               \
                                                                v
                                                            +--------+
                                                            |Trainer |
                                                            +--------+
```

---

## 4. Module Documentation Overview

### Core Modules
- **NureonLang Core** — symbolic shapes, base structures
- **NureonLang IR** — intermediate representation
- **NureonLang Compiler** — full DSL compiler
- **Compiler CLI** — command-line interface

### Runtime & Training
- **Infrastructure** — weights, serialization
- **Executor** — feed-forward execution
- **Trainer** — SGD trainer

### Orchestration
- **TaskLang** — task specification language
- **RuleLang** — rule-based selection
- **Cluster Engine** — multi-network cooperation
- **Oracle** — ecosystem orchestrator

---

## 5. Compilation Pipeline

```
NureonLang Source (.nl)
           |
           v
 [ ANTLR Parser ]  ---> AST
           |
           v
 [ IR Builder ] ---> IR Tree
           |
           v
 [ Semantic Passes ]
  - Macro Expansion
  - Loop Unrolling
  - Block Expansion
  - Symbol Resolution
  - Shape Preparation
           |
           v
 [ IR Network Compiler ]
           |
           v
     NetworkModel
```

---

## 6. Execution Model

Execution includes:
- Graph construction
- Symbolic shape propagation
- Feed-forward computation
- Extensible layer handlers
- Pluggable operators/backends

---

## 7. Training

Trainer supports:
- SGD optimization
- batching
- checkpointing
- weight integration
- registry updates

---

## 8. Task & Rule Languages

### TaskLang
Defines:
- task metadata
- IO specification
- tags (SUM, CLASSIFY, BOOL, etc.)

### RuleLang
Controls:
- network selection
- scoring
- ranking
- pipeline construction

---

## 9. Network Clusters & Orchestration

Clusters support:
- ensembles
- branching pipelines
- multi-stage reasoning
- parallel execution

Oracle performs:
1. Task parsing
2. Network selection
3. Pipeline building
4. Execution
5. Performance scoring
6. Retraining and pruning

---

## 10. Feature Checklist

| Feature                | Status              | Available Tags / Notes |
|------------------------|---------------------|-------------------------|
| **NureonLang Compiler** | ✔ **Done**          | Stable. Supports full DSL: layers, macros, loops, modules, expand-blocks. |
| **Compiler CLI**        | ✔ **Done**          | `--validation` modes; suitable for CI/CD pipelines and batch compilation. |
| **NetworkModel IR**     | ✔ **Done**          | Human-readable IR; symbolic shapes; arithmetic expressions; full introspection. |
| **Executor**            | ✔ **Prototype**     | Supports feed-forward execution for classical architectures; more operators planned. |
| **Trainer**             | ✔ **Prototype**     | Basic SGD trainer; planned integration with TensorFlow/PyTorch for large models. |
| **Weights Service**     | ✔ **Prototype**     | Load/save/version weights; inspect tensors; cross-network weight migration. |
| **Oracle**              | ⏳ *Almost ready*    | High-level task router; matches tasks to networks by metadata and tags. |
| **Registry**            | ⏳ *Almost ready*    | CRUD for networks; metadata; version control; storage backend abstraction. |
| **TaskLang**            | ✔ **Done**          | Declarative task spec. Built-in tags: `SUM`, `COMPARE`, `CLASSIFY`, `IMAGE`, `SEQUENCE`, `BOOL`, `ARITH`, etc. |
| **RuleLang**            | 🚧 *In development* | Rule sets for selecting networks; conditional logic; score functions. |
| **Cluster Engine**      | 🚧 *Planned*        | Multi-network pipelines; hierarchical ensembles; expert voting and aggregation. |
| **Adaptive Training Loop** | 🚧 *Planned*     | On-demand retraining; automated pruning; champion–challenger evaluation. |
| **UI Console**          | ⏳ *Almost ready*    | Interactive terminal interface for browsing networks, tasks, rules, and experiments. |


---

## 11. Example

```
MODULE Encoder
BEGIN
    LAYER e1 dense size=128
    LAYER e2 dense size=64
    CONNECT e1 -> e2
END

INPUT x shape=(1, 128)
CALL Encoder
OUTPUT e2
```

---

## 12. Long-Term Vision

Alexandra is designed to scale toward:
- large populations of networks
- rule-based reasoning
- hierarchical emergent behavior
- multi-network evolutionary cycles

Not through one large model,  
but through **coordination of many specialized models**.

---
