# nureonLang

**nureonLang** is a formal, human-readable language for describing neural network architectures.

Modern AI frameworks provide thousands of models — yet there is no **standard language** that formally describes how these networks are structured, connected, and evolved.  
nureonLang introduces a **standard textual representation** for defining neural architectures in a structured, modular, and compiler-friendly way.

> **Note on spelling**  
> The name **nureonLang** intentionally differs from “NeuronLang”.  
> It signals that the language is a **higher-level structural DSL** for organizing and reasoning about networks,  
> not a low-level neuron simulator.

---

## 🧠 What the language can do (at a glance)

- **Layers & parameters:** `LAYER` with typed attributes and shapes (`shape=28 x 28`, `(3,224,224)`).
- **Connections:** explicit graph wiring via `CONNECT a -> b`, including nested or indexed paths like `module.sub[i]`.
- **Variables & expressions:** `LET` with arithmetic (`+ - * / ^`), indexing, and backend-defined string operations.
- **Control flow:** `FOR` loops and nested `IF ... ELSE` for programmatic topology definition.
- **Modularity:** `MODULE` (reusable blocks) and `DEFINE` (parameterized macros).
- **Expansion:** `EXPAND` over **SPACE/GROUP** for symmetry or dimensional replication.
- **File linkage:** `FILE "..."` to bind weights, checkpoints, or IR artifacts.
- **Deterministic parsing:** unambiguous ANTLR4 grammar → stable AST → portable IR.

---

## 💡 Minimal Example

```nl
BEGIN
  LET hidden = 128;
  LAYER INPUT  name=x shape=28 x 28
  LAYER DENSE  name=h size=hidden activation=relu
  LAYER OUTPUT name=y size=10 activation=softmax
  CONNECT x -> h
  CONNECT h -> y
END
FILE "mnist.bin"
```

---

## 📘 Navigation

- [Design Principles](#design-principles)
- [Language Overview](#language-overview)
- [Grammar Reference](#grammar-reference)
- [Examples & Tests](#examples--tests)
- [Compiler Integration](#compiler-integration)
- [Future Directions](#future-directions)

---

nureonLang is **framework-neutral** and serves as an **interchange format** between human-designed and machine-generated architectures —  
a language that even an AI (like *Alexandra*) can write and reason about.