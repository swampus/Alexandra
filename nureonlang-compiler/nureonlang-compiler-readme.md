# NureonLang Compiler

## Table of Contents
1. [High-Level Responsibilities](#1-high-level-responsibilities)
2. [Architecture Overview](#2-architecture-overview)  
   2.1 [Main Components](#21-main-components)  
   2.2 [Instruction Flow Diagram](#22-instruction-flow-diagram)
3. [Compilation Pipeline](#3-compilation-pipeline)
4. [Intermediate Representation (IR)](#4-intermediate-representation-ir)
5. [Layers, Handlers and Extensibility](#5-layers-handlers-and-extensibility)
6. [Validation Model](#6-validation-model)
7. [CLI Integration](#7-cli-integration)
8. [Feature Checklist](#8-feature-checklist)
9. [Planned Work / TODO](#9-planned-work--todo)
10. [Example Snippet](#10-example-snippet)
11. [FAQ](#11-faq)

---

## 1. High-Level Responsibilities

The **NureonLang Compiler** converts the high‑level neural architecture DSL (NureonLang)  
into a validated, fully deterministic **NetworkModel**.

It performs:

- Parsing (ANTLR4)
- Construction of IR (Instruction Tree)
- Macro and module expansion
- Loop unrolling and conditional evaluation
- Construction of the executable network graph
- Validation (structural + shape-level)
- Error reporting at every stage

The compiler is the foundation of the **Alexandra** ecosystem.

---

## 2. Architecture Overview

```
NureonLang Source
        │
        ▼
+-----------------------+
|     ANTLR Parser      |
+-----------------------+
        │ AST
        ▼
+-------------------------------+
|  NureonLangToIRVisitor       |
|  (AST → Instruction IR)      |
+-------------------------------+
        │ IR Tree
        ▼
+-------------------------------+
|      IRNetworkCompiler       |
|  (IR → NetworkModel Build)   |
+-------------------------------+
        │ Validated Model
        ▼
+-------------------------------+
|        NetworkModel          |
+-------------------------------+
```

### 2.1 Main Components

| Component | Responsibility |
|----------|----------------|
| `NureonLangLexer/Parser` | Converts source to AST |
| `NureonLangToIRVisitor` | Converts AST → IR |
| `Instruction` | Universal IR node |
| `IRNetworkCompiler` | Builds NetworkModel |
| `ValidationEngine` | Structural / shape checks |
| `LayerHandlers` | Create layers dynamically |

---

### 2.2 Instruction Flow Diagram

```
               +-----------------+
               |  PROGRAM (root) |
               +--------+--------+
                        |
     ------------------------------------------------
     |                      |                       |
 +-------+           +-------------+         +--------------+
 | LAYER |           |   CONNECT   |         |   MODULE     |
 +-------+           +-------------+         +--------------+
                        |
      +-----------------+-------------------+
      |                 |                   |
  +--------+      +----------+        +-------------+
  |  FOR   |      |   IF     |        |   EXPAND    |
  +--------+      +----------+        +-------------+
```

Each block expands, normalizes, resolves, and contributes to the final directed acyclic computation graph.

---

## 3. Compilation Pipeline

### Step 1 — Parsing
ANTLR grammar → AST

### Step 2 — AST → IR
Handled by `NureonLangToIRVisitor`.

### Step 3 — IR Transformations
- Loop unrolling
- Macro expansion
- Module inlining
- Expression evaluation
- Variable resolution

### Step 4 — Validation
Depending on mode (NONE / STRUCTURAL / SHAPES)

### Step 5 — NetworkModel Assembly
Layers and connections are materialized into executable Java objects.

---

## 4. Intermediate Representation (IR)

IR is a structured tree of `Instruction` objects.

Benefits:

- Enables static analysis
- Allows symbolic manipulation
- Decouples DSL from backend
- Enables future backends such as MLIR, ONNX, PyTorch, etc.

---

## 5. Layers, Handlers and Extensibility

Each layer type has a **Handler**, responsible for:

- Param parsing
- Shape inference
- Runtime validation
- Layer object construction

This design makes the compiler naturally extensible.

---

## 6. Validation Model

### NONE
Only syntax is enforced.

### STRUCTURAL
Checks:
- dangling references
- cyclic dependencies
- missing inputs/outputs

### SHAPES (default)
Includes structural +:
- shape propagation
- broadcasting rules
- connection compatibility

---

## 7. CLI Integration

```
nureonlang-compiler-cli compile model.nl --validation shapes
```

The CLI performs:

1. Parsing
2. Compilation (IR → NetworkModel)
3. Summary output
4. Error reporting

---

## 8. Feature Checklist

### ✔ Supported
- Macros
- Modules
- Expand blocks
- Conditionals
- Loops
- LET-bound variables
- Shape inference
- ANTLR4 grammar
- Clean Architecture structure

### ❌ Not Yet
- Imports
- Backend codegen
- MLIR export
- Graph optimizers

---

## 9. Planned Work / TODO

- Shape Engine v2 (broadcasting, tensor algebra)
- Static optimizer
- IR → MLIR / ONNX translator
- Dead-layer pruning
- GraphViz export
- Advanced condition evaluator

---

## 10. Example Snippet

```
MODULE Encoder
BEGIN
    LAYER e1 dense size=128
    LAYER e2 dense size=64
    CONNECT e1 -> e2
END

INPUT x shape=(1,128)
CALL Encoder
OUTPUT e2
```

This compiles into a concrete feed-forward stack.

---

## 11. FAQ

### Is the compiler tied to Java?
No — the IR itself is backend-agnostic.

### Can networks be dynamic?
Yes — loops, conditionals, macros, modules.

### How hard is it to extend the compiler?
Trivial: add new handlers + grammar extensions.

---

End of README.
