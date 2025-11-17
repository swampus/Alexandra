# NureonLang IR

> **Intermediate Representation (IR)** of the NureonLang language — the *machine-understandable form* of a neural network architecture, ready for execution, training, composition, and weight binding inside the **Alexandra** ecosystem.

<p align="left">
  <a href="#"><img alt="Java" src="https://img.shields.io/badge/Java-17+-red"></a>
  <a href="#"><img alt="Build" src="https://img.shields.io/badge/build-maven-blue"></a>
  <a href="#"><img alt="License" src="https://img.shields.io/badge/license-MIT-green"></a>
  <a href="#"><img alt="Stability" src="https://img.shields.io/badge/stability-beta-yellow"></a>
</p>

---

## 🧩 What this module is

`nureonLang-ir` defines the **canonical data model** for compiled neural architectures in the Alexandra stack.  
It is the **output of the NureonLang compiler** and the **input for execution engines**, trainers, and optimizers.

If the NureonLang DSL is *source code* describing how a network should look,
then this IR is its *bytecode*: a structured, semantically resolved, language-agnostic representation.

Each `Instruction` node describes one logical operation — a layer, connection, control block, or module — and forms a tree that can be:

- **Executed** (instantiated into a real neural network),
- **Trained** (weights bound and updated),
- **Serialized** (saved and loaded between processes),
- **Composed** (linked with other IR modules into larger graphs).

The IR ensures that **compilation and execution are decoupled** —  
a core architectural principle that makes the system modular and inspectable.

---

## 🧠 Why IR exists

Traditional frameworks like **PyTorch** or **TensorFlow** merge three roles into one:
1. *Definition* of the model (Python code or graph),
2. *Execution* of operations,
3. *State management* (weights, optimizer state, etc.).

This tight coupling makes inspection, transformation, and reasoning about architectures difficult.  
NureonLang takes a different route — it’s **compiler-based**, not interpreter-based.

| Concept | PyTorch / TensorFlow | NureonLang + IR |
|----------|---------------------|----------------|
| Model definition | Imperative Python code | Declarative DSL (`.nl` files`) |
| Compilation | Implicit (JIT or tracing) | Explicit, via compiler → IR |
| Execution | Immediate (runtime binding) | Deferred (runtime loads IR) |
| Interchange | Hard to serialize architecture cleanly | IR is self-contained, portable |
| Meta-data | Spread across code | Centralized in `meta` and `params` |
| Transformation | Limited (requires custom passes) | Built-in structural manipulation via IR |

In short, **PyTorch executes your code**.  
**NureonLang compiles your intent**.

---

## ⚙️ What compiles what

- **Compiler (`nureonlang-compiler`)**  
  Parses `.nl` source → builds validated `Instruction` tree → emits IR (JSON/GraphML/DOT).

- **IR (`nureonlang-ir`, this module)**  
  Defines the structure of that tree — a stable contract between compiler, runtime, and tools.

- **Runtime / Trainer (future modules)**  
  Consume the IR to instantiate networks, attach weights, and run inference or training.

This separation enables multiple compilers, optimizers, and runtimes to coexist and evolve independently.

---

## 🔤 Grammar → IR mapping

The **NureonLang grammar** (written in ANTLR4) defines the *syntax* of the DSL.  
The compiler transforms that syntax into a **semantic structure** — the IR — represented by the `Instruction` class and `OpCode` enum.

Each grammar rule corresponds to one or more IR nodes:

| Grammar rule | Example | Resulting IR |
|---------------|----------|--------------|
| `layerDecl` | `LAYER conv1 Conv2D filters=64` | `Instruction(op=LAYER, name="conv1", type="Conv2D", params={filters:64})` |
| `connectStmt` | `CONNECT input -> conv1` | `Instruction(op=CONNECT, from="input", to="conv1")` |
| `moduleDecl` | `MODULE Encoder BEGIN ... END` | `Instruction(op=MODULE_DEF, name="Encoder", body=[...])` |
| `macroDecl` | `DEFINE Block(x) BEGIN ... END` | `Instruction(op=MACRO_DEF, name="Block", params=[x])` |
| `macroCall` | `Block(input)` | `Instruction(op=MACRO_CALL, name="Block", inputs=[input])` |
| `forLoop` | `FOR i FROM 1 TO 3 BEGIN ... END` | `Instruction(op=FOR, var="i", fromVal=1, toVal=3, body=[...])` |
| `ifStmt` | `IF x > 0 BEGIN ... END` | `Instruction(op=IF, cond=..., body=[...])` |
| `fileRef` | `FILE "model.json"` | `Instruction(op=FILE, path="model.json")` |
| `expandStmt` | `EXPAND SPACE = 3D` | `Instruction(op=EXPAND, space="3D")` |

> The grammar defines **syntax**; the IR defines **semantics**.  
> Together, they form a complete language-to-network pipeline.

This separation of syntax and semantics makes NureonLang *extensible*:  
you can modify the grammar without breaking runtimes, or extend the IR without changing the syntax.

---

## 🧩 Data model overview

**Package:** `io.github.swampus.alexandra.ir.model`

| Class | Responsibility |
|-------|----------------|
| **`Instruction`** | Universal node in the IR tree (layer, module, control block, etc.). |
| **`OpCode`** | Enumerates all supported operations — LAYER, CONNECT, MODULE, FOR, IF, etc. |

The model is designed to be **language-agnostic** and **serializable** via JSON, CBOR, or GraphML.

---

## 🧠 Example flow

```
NureonLang source (.nl)
     │
     ▼
 NureonLang Compiler (ANTLR + Semantic Analyzer)
     │
     ▼
 NureonLang IR (Instruction tree)
     │
     ├─► Runtime: builds neural graph
     ├─► Trainer: binds & updates weights
     ├─► Optimizer: structural transforms / pruning
     └─► Tools: visualization, export, linking
```

Example:

```nl
BEGIN
  LAYER input shape=(3,224,224)
  LAYER conv1 Conv2D filters=64 kernel=(3,3) padding=Same
  CONNECT input -> conv1
  FILE "mininet.json"
END
```

After compilation:

```json
{
  "op": "PROGRAM",
  "body": [
    {
      "op": "LAYER",
      "type": "Conv2D",
      "name": "conv1",
      "params": {"filters": 64, "kernel": "(3,3)", "padding": "Same"}
    },
    { "op": "CONNECT", "from": "input", "to": "conv1" }
  ],
  "meta": {"source": "mininet.nl"}
}
```

---

## 📦 Integration

**Dependency**
```xml
<dependency>
  <groupId>io.github.swampus.alexandra</groupId>
  <artifactId>nureonlang-ir</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Runtime deps:** none — pure Java POJOs.  
**Lombok:** compile-time only, not required at runtime.

---

## 🧠 Deeper architecture notes

### Layered design philosophy

```
Grammar (ANTLR) → Compiler (Java) → IR (POJO model) → Runtime (execution engine)
```

- **Grammar** — syntax rules for the NureonLang DSL (`NureonLang.g4`).
- **Compiler** — semantic transformer from syntax tree to IR (`Instruction`).
- **IR** — neutral representation of the network’s structure and intent.
- **Runtime** — executes or trains networks using the IR as a blueprint.

This makes the system modular, testable, and language-agnostic — like how LLVM IR serves as the bridge between different programming languages and CPU architectures.

### Conceptual parallels

| Component | Analogy |
|------------|---------|
| NureonLang grammar | High-level source code (C/C++/Python) |
| Compiler | LLVM front-end |
| NureonLang IR | LLVM IR or ONNX Graph |
| Runtime / Trainer | LLVM backend or hardware execution layer |

Thus, **NureonLang IR is to neural networks what LLVM IR is to compiled code.**  
It captures intent, structure, and data flow — not execution details.

---

## 🔬 Philosophical notes

- **Declarative over imperative** — You describe *what* the network is, not *how* to build it.
- **Portable intelligence** — IR can be moved between runtimes, languages, or even hardware backends.
- **Transparent AI engineering** — IR is inspectable, versionable, and reproducible.
- **Composable design** — Any IR tree can become a submodule of another network.
- **Future-proof** — The IR is designed for later integration with symbolic reasoning, neuroevolution, and distributed learning.

---

## 📜 License
MIT © 2025 Swampus
