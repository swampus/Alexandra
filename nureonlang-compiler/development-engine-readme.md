# Alexandra Neural Development Engine

Alexandra supports a **development-phase compilation model** in which neural networks are not always defined explicitly.
Instead, they can be generated from a compact **genetic program (genome)** written in NureonLang.

This enables deterministic expansion of neural architectures, compact storage of large network definitions, and opens the door for evolutionary or generative approaches to architecture design.

---

## 🧬 Concept: Genome → Development → Network

Traditional pipeline:

```
Source → IR → Compiler → Network
```

Development-enabled pipeline:

```
Source (Genome)
        ↓
IR (Genetic Program)
        ↓
Development Engine
        ↓
Expanded IR (Concrete Instructions)
        ↓
Compiler
        ↓
NetworkModel
```

In this mode, loops, conditions, macros, and expansion rules are resolved **before** network compilation.

---

## ✨ Example

Instead of writing:

```
LAYER dense1 size=32
LAYER dense2 size=32
LAYER dense3 size=32
```

You can write:

```
FOR i FROM 1 TO 3
BEGIN
    LAYER dense[i] size=32
END
```

During development, this expands to:

```
LAYER dense1 size=32
LAYER dense2 size=32
LAYER dense3 size=32
```

The compiler then builds the network from the expanded instructions.

---

## 🚀 Why Development Mode Exists

### 1. Compact architecture definition

Large networks can be described in a few lines of code.

### 2. Deterministic generation

The same genome always produces the same network.

### 3. Foundation for neural evolution

Genetic programs can be mutated or recombined.

### 4. Easier cluster / swarm definitions

Multiple agents or networks can be generated programmatically.

### 5. Separation of concerns

* Development Engine = expands structure
* Compiler = builds runtime model

---

## 🛠️ What the Development Engine Does

The development stage currently supports:

* **FOR loops** → unrolled into repeated instructions
* **IF statements** → resolved into a single branch
* **Macros / modules** → inlined into the instruction stream
* **Nested structures** → expanded recursively

The result is always a **flat IR without generative constructs**.

---

## 🔧 Enabling Development Mode

Development mode can be enabled in the compilation pipeline.

Example:

```java
Instruction genome = parser.parse(source);

IRDeveloper developer = new IRDeveloper(List.of(
        new ForExpander(),
        new IfExpander()
));

List<Instruction> developed = developer.develop(List.of(genome));

NetworkModel model = compiler.compile(developed);
```

If development mode is disabled, the compiler works directly on the original IR.

---

## 🧪 Testing

Unit tests cover:

* loop expansion correctness
* empty body handling
* boundary conditions
* integration of development pipeline

Run tests:

```
mvn test
```

---

## 🧠 Design Philosophy

Alexandra treats neural architecture descriptions not only as static topologies, but also as **developmental programs**.

This mirrors biological systems:

| Biology     | Alexandra             |
| ----------- | --------------------- |
| DNA         | NureonLang program    |
| Development | IRDeveloper expansion |
| Organism    | NetworkModel          |

The network is not stored explicitly — it is **developed from a compact genome**.

---

## 📌 Future Extensions

Planned improvements include:

* expression evaluation engine for conditions
* mutation operators for genome evolution
* caching of developed IR
* visualization of development stages
* probabilistic or stochastic expansion rules

---

## 📜 License

(put your project license here)

---

## 👤 Author

Swampus / Alexandra Project
