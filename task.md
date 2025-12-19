# Alexandra Tasks & Roadmap

This document describes the evolution path of the **Alexandra Modular Neural Architecture Ecosystem**.

It is both:
- a **technical roadmap** for the system, and
- a **task board** for contributors (students, engineers, researchers).

> If a branch for a task **does not exist**, it means **no one is actively working on it yet**.  
> To start working on a task:
> 1. Create a branch with the suggested name.
> 2. Implement the changes.
> 3. Open a Pull Request (PR).
> 4. Request review.
> 5. Only after review is approved, the work is considered complete.

---

## Legend

- **LEVEL**
  - `EASY` – good for students; focused exercises; no heavy math.
  - `MODERATE` – requires understanding DSL/IR, compiler passes, or system architecture.
  - `CHALLENGING` – advanced math, ML, compiler design, distributed or orchestration logic.

- **STATUS**
  - `TODO` – not started.
  - `WIP` – work in progress (branch exists).
  - `PLANNED` – concept defined, implementation not started.
  - `UNPUBLISHED` – exists in private/unpublished code and needs consolidation/refinement.

Modules mentioned below:

- **Published (current repo)**  
  `nureonlang-core`, `nureonlang-ir`, `nureonlang-compiler`, `nureonlang-compiler-cli`, `nureonlang-infrastructure`, `nureonlang-translator`.

- **Unpublished / prototype modules (live in private code for now)**  
  `nueronlang-executor`, `nueronlang-weight`, `networkapi-orchestrator`, `networkapi-trainer`, `alexandra-oracle`.

Tasks below refer to these modules explicitly so it is clear **where** the work lives.

---

## 1. Task Table (Overview)

### 1.1 EASY Tasks

| ID  | TASK                                       | BRANCH_NAME                    | DESCRIPTION                                                                 | MODULE(S)                 | REQUIRED_SKILLS                    | LEVEL | STATUS |
|-----|--------------------------------------------|--------------------------------|-----------------------------------------------------------------------------|---------------------------|------------------------------------|-------|--------|
| E01 | Add NureonLang examples to Core README     | `docs/core-readme-examples`    | Add minimal `.nl` examples (layers, shapes, connections) to core docs.     | `nureonlang-core`        | Markdown, basic DSL reading        | EASY  | TODO   |
| E02 | Add basic executor smoke tests             | `test/executor-smoke`          | Test `UniversalNetworkExecutor` on a tiny 2–3 layer network.               | `nueronlang-executor`    | Java, JUnit, basic NN intuition    | EASY  | UNPUBLISHED |
| E03 | Improve error messages for shape mismatches| `fix/shape-error-messages`     | Add human‑readable hints when symbolic shapes are incompatible.            | `nureonlang-core`, `nureonlang-compiler` | Java, exceptions, UX thinking | EASY  | TODO   |
| E04 | Simple unit tests for Dense layer handler  | `test/dense-layer-handler`     | Verify configuration + shape inference for dense layers.                   | `nureonlang-compiler`    | Java, JUnit                        | EASY  | TODO   |
| E05 | Document weight module with mini example   | `docs/weights-module-intro`    | Short README: how `nueronlang-weight` auto‑fills weights for a network.    | `nueronlang-weight`      | Markdown, Java reading             | EASY  | UNPUBLISHED |

---

### 1.2 MODERATE Tasks

| ID  | TASK                                    | BRANCH_NAME                     | DESCRIPTION                                                                                              | MODULE(S)                                | REQUIRED_SKILLS                      | LEVEL    | STATUS       |
|-----|-----------------------------------------|----------------------------------|----------------------------------------------------------------------------------------------------------|------------------------------------------|--------------------------------------|----------|--------------|
| M01 | TaskLang → internal Task IR             | `feat/tasklang-ir`               | Implement conversion from parsed TaskLang into unified internal Task IR.                                | `tasklang`, `alexandra-oracle`          | Java, ANTLR, IR design               | MODERATE | TODO         |
| M02 | RuleLang MVP interpreter                | `feat/rulelang-interpreter`      | Evaluate basic rule sets: conditions, numeric scores, selection by tags.                                | `rulelang`, `alexandra-oracle`          | ANTLR, Java, rule engines            | MODERATE | WIP          |
| M03 | Executor operator extensions            | `feat/executor-ops-extend`       | Add support for Softmax, MatMul, simple Conv in executor.                                               | `nueronlang-executor`                   | Java, linear algebra                 | MODERATE | UNPUBLISHED  |
| M04 | Weight inspection API                   | `feat/weights-inspection-api`    | Provide API to inspect weights (dump, shapes, stats) from compiled networks.                            | `nueronlang-weight`                     | Java, serialization                  | MODERATE | UNPUBLISHED  |
| M05 | Registry caching layer                  | `feat/registry-cache-layer`      | Add Redis/in‑memory caching for fast registry lookups.                                                  | `alexandra-oracle`, `networkapi-orchestrator`       | Java, caching, Spring Boot           | MODERATE | UNPUBLISHED  |
| M06 | Execution profiling hooks               | `feat/executor-profiling`        | Layer timing + tracing hooks; expose basic timing info to Oracle / Trainer.                             | `nueronlang-executor`, `alexandra-oracle` | Java, profiling, logging           | MODERATE | TODO         |
| M07 | IR visualizer integration               | `feat/ir-visualizer`             | Export NetworkModel / IR into Graphviz/DOT for visualization.                                           | `nureonlang-ir`                          | DOT/Graphviz, Java                   | MODERATE | PLANNED      |
| M08 | Implement InferUseCase in Network API   | `feat/networkapi-infer-usecase`  | Wire `InferUseCase` to executor + weights: load network, run inference, return named outputs.           | `networkapi-orchestrator`, `nueronlang-executor`, `nueronlang-weight` | Java, Spring, NN basics | MODERATE | UNPUBLISHED  |
| M09 | Network graph operations implementation | `feat/networkapi-graph-ops`      | Implement `Link/Merge/Split/UnlinkNetworksUseCase` with proper graph persistence and tests.             | `networkapi-orchestrator`                            | Java, graph modeling, tests          | MODERATE | UNPUBLISHED  |
| M10 | Cluster result aggregation strategies   | `feat/oracle-cluster-aggregation`| In `SelectByClusterIdUseCaseImpl`: implement average, majority vote, etc., plus strategy interface.     | `alexandra-oracle`                       | Java, basic stats, API design        | MODERATE | UNPUBLISHED  |
| M11 | Task-based default selection behaviour  | `feat/oracle-default-selection`  | In `SelectByTaskIdUseCaseImpl`: replace “take first network” with proper scoring + RuleLang fallback.   | `alexandra-oracle`, `rulelang`          | Java, rule logic                     | MODERATE | UNPUBLISHED  |
| M12 | Weight auto-fill integration from IR    | `feat/weights-ir-integration`    | Connect `nueronlang-weight` (IrShapeSpecProvider, WeightsFactory) with compiler output pipeline.        | `nueronlang-weight`, `nureonlang-compiler`, `nureonlang-ir` | Java, IR understanding | MODERATE | UNPUBLISHED  |
| M13 | Executor & Trainer documentation        | `docs/executor-trainer`          | Write focused docs on execution model, Trainer API, and how to plug in custom trainers.                 | `nueronlang-executor`, `networkapi-trainer` | Markdown, Java reading           | MODERATE | UNPUBLISHED  |

---

### 1.3 CHALLENGING Tasks

| ID  | TASK                                      | BRANCH_NAME                       | DESCRIPTION                                                                                         | MODULE(S)                                            | REQUIRED_SKILLS                            | LEVEL       | STATUS       |
|-----|-------------------------------------------|------------------------------------|-----------------------------------------------------------------------------------------------------|------------------------------------------------------|--------------------------------------------|-------------|--------------|
| C01 | Transformer training pipeline             | `feat/trainer-transformer`         | Finish training loop for transformer‑like layers (loss, scheduler, integration with TrainerContext).| `networkapi-trainer`, `nueronlang-executor`, `nueronlang-weight` | ML math, seq models, Java          | CHALLENGING | UNPUBLISHED  |
| C02 | Oracle reasoning pipeline                 | `feat/oracle-reasoning-pipeline`   | Implement full “solve task” pipeline: TaskLang → RuleLang → network selection → execution → scoring.| `alexandra-oracle`, `networkapi-orchestrator`, `rulelang`, `tasklang` | Systems design, DSLs, Java          | CHALLENGING | UNPUBLISHED  |
| C03 | Cluster Engine core                       | `feat/cluster-engine-core`         | Implement ensembles, branching, merging, voting, and multi‑stage clusters.                         | `alexandra-oracle` (cluster layer), `networkapi-orchestrator`    | ML ensembles, orchestration                | CHALLENGING | PLANNED      |
| C04 | Adaptive training loop                    | `feat/adaptive-training-loop`      | Auto‑retrain underperforming networks based on registry statistics and evaluation results.          | `networkapi-trainer`, `alexandra-oracle`, `networkapi-orchestrator` | ML, statistics, scheduling           | CHALLENGING | PLANNED      |
| C05 | RuleLang → execution plan compiler        | `feat/rulelang-pipeline-compiler`  | Compile RuleLang definitions into explicit execution plans (pipelines, scores, fallbacks).         | `rulelang`, `alexandra-oracle`                       | Compiler design, ANTLR, planning           | CHALLENGING | PLANNED      |
| C06 | Distributed weights backend               | `feat/distributed-weights-backend` | Implement S3/MinIO‑style storage for weights and connect it to training/inference flows.           | `nueronlang-weight`, `networkapi-orchestrator`                   | Distributed systems, storage APIs          | CHALLENGING | PLANNED      |
| C07 | Symbolic execution & validation           | `feat/symbolic-execution`          | Add ability to symbolically traverse networks to verify non‑numeric flows / conditional structures. | `nureonlang-ir`, `nueronlang-executor`               | Symbolic reasoning, graph algorithms       | CHALLENGING | PLANNED      |
| C08 | External backend integration (PyTorch)    | `feat/pytorch-backend-integration` | Prototype adapter that can export IR to a PyTorch‑friendly format and call an external runner.      | `nureonlang-ir`, `nueronlang-executor`, `networkapi-trainer` | PyTorch, Python+Java interop         | CHALLENGING | PLANNED      |
| C09 | Evaluation & benchmarking harness         | `feat/eval-harness`                | Create benchmark runner for multiple networks on shared tasks; log metrics into registry.           | `networkapi-orchestrator`, `alexandra-oracle`, `networkapi-trainer` | Experiment design, metrics, Java   | CHALLENGING | PLANNED      |
| C10 | Meta‑selection strategies (champion cycle)| `feat/meta-selection-strategies`   | Design configurable champion–challenger / evolutionary selection over registry networks.            | `alexandra-oracle`, `rulelang`, `networkapi-trainer` | ML, evolutionary ideas, systems design     | CHALLENGING | PLANNED      |

---

## 2. Kanban View

Recommended GitHub Project columns:

- **BACKLOG** – all `TODO` / `PLANNED` tasks.
- **WIP** – tasks with active branches and ongoing work.
- **REVIEW** – open PRs waiting for review.
- **DONE** – merged and verified.

Each task in this document can be mirrored as a GitHub issue and linked to PRs.

---

## 3. Branch Naming Rules

Use the following convention:

```text
<type>/<short-kebab-description>
```

Examples:
- `feat/rulelang-interpreter`
- `docs/getting-started-polish`
- `fix/shape-error-messages`
- `feat/trainer-transformer`

If you implement a task from this document:
1. Reuse the suggested `BRANCH_NAME` if possible.
2. Reference the task ID (e.g. `C01`) in the PR title/description.

---

## 4. Relation to Published / Unpublished Modules

Some tasks refer to **unpublished** modules that currently live only in local/private code:

- `nueronlang-executor` – generic execution engine (`NetworkExecutor`, `UniversalNetworkExecutor`, conditional branches, input providers).
- `nueronlang-weight` – weight shape specification, initialisation strategies, auto‑fill and adapters for IR/compiler.
- `networkapi-orchestrator` – Spring Boot API for storing networks, performing inference, and manipulating network graphs.
- `networkapi-trainer` – training layer (SGD trainer, `TrainerContext`, `TransformerTrainer` prototype).
- `alexandra-oracle` – orchestrator: solves tasks, talks to registry, applies RuleLang/TaskLang, manages clusters.

Tasks marked `UNPUBLISHED` typically mean:
- the core idea and some code already exist,
- but the module still needs **cleanup, tests, documentation**, and **synchronisation** with the published repository structure.

When these modules are open‑sourced, their tasks can be moved from `UNPUBLISHED` to normal `TODO / WIP / DONE`.

---

## 5. Long-Term Direction (High-Level)

The long‑term direction of Alexandra is to grow into:

- a **language‑driven ecosystem** of networks,
- a **population** of specialised models that can be composed for complex tasks,
- an **orchestrator** that selects, combines and retrains networks based on experience,
- a **continuous improvement loop** where underperforming networks are pruned or retrained and new ones are introduced.

This document lists the concrete engineering steps needed to move from:

> “compiler + execution for one network”  
> → to  
> “ecosystem of cooperating networks with tasks, rules, clusters, and adaptive training”.

Contributions are welcome at every level: from small documentation fixes and unit tests to deep work on training, orchestration and cross‑backend integration.

---
