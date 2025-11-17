# NureonLang Compiler CLI
Part of the Alexandra Neural Architecture Framework

`nueronlang-compiler-cli` is a minimal yet production-grade command-line tool for compiling models written in **NureonLang** into the intermediate representation **NetworkModel IR**, fully compatible with the Alexandra ecosystem.

This CLI:
- parses a NureonLang source file,
- validates it (optional),
- compiles it into IR,
- prints a structured summary.

---

## Installation

### Build with Maven
```
mvn clean package
```

The CLI JAR will appear in:

```
nueronlang-compiler-cli/target/nueronlang-compiler-cli.jar
```

---

## Usage

### General Form
```
java -jar nueronlang-compiler-cli.jar compile <source.nl> [--validation none|structural|shapes]
```

### Commands

| Command | Description |
|---------|-------------|
| `compile` | Compiles a NureonLang source file into a NetworkModel |

### Validation Levels

| Level | Description |
|--------|-------------|
| `none` | No validation |
| `structural` | Checks structure of the graph |
| `shapes` (default) | Full validation of shapes, connectivity, and tensor compatibility |

---

## Examples

### Minimal example
```
java -jar nueronlang-compiler-cli.jar compile model.nl
```

### No validation
```
java -jar nueronlang-compiler-cli.jar compile model.nl --validation none
```

### Debug logging
```
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug      -jar nueronlang-compiler-cli.jar compile model.nl
```

---

## Pipeline Overview

```
NureonLang Source
        │
ANTLR Lexer/Parser
        │
AST (Program)
        │
IR Instruction Tree
        │
NetworkCompilerFacade
        │
Validation (optional)
        │
NetworkModel
```

---

## Supported NureonLang Syntax Examples

### 1. Simple Network
```
LAYER input INPUT size=4
LAYER dense1 DENSE size=8 activation="relu"
LAYER output OUTPUT size=2

CONNECT input -> dense1
CONNECT dense1 -> output
```

### 2. Conditional Logic
```
LET n = 4;

IF n > 2
BEGIN
    LAYER hidden DENSE size=32
    CONNECT input -> hidden
END
ELSE
BEGIN
    LAYER tiny DENSE size=4
    CONNECT input -> tiny
END
```

### 3. Module
```
MODULE Block
BEGIN
    LAYER d1 DENSE size=16
    LAYER d2 DENSE size=16
    CONNECT d1 -> d2
END

Block
Block
```

### 4. Macro
```
DEFINE DenseBlock(x, out)
BEGIN
    LAYER dense DENSE size=out
    CONNECT x -> dense
END

DenseBlock(input, 32)
DenseBlock(dense, 32)
```

---

## Output Example

```
INFO Model summary:
INFO   Total layers: 3
INFO   Input layers:
INFO     - input (InputLayer)
INFO   Output layers:
INFO     - output (OutputLayer)
INFO Compilation finished successfully.
```

---

## Module Integration (Alexandra Ecosystem)

| Module | Responsibility |
|--------|----------------|
| `nureonlang-core` | ANTLR grammar & AST |
| `nureonlang-infrastructure` | Parsing & visitors |
| `nureonlang-compiler` | NureonLang → IR compiler |
| `nureonlang-compiler-cli` | **This CLI** |
| `network-executor` | Executes the compiled model |
| `alexandra-oracle` | Orchestrates models and reasoning |

---

## Roadmap

- JSON export (`--out model.json`)
- GraphViz/Mermaid graph export
- Error reporting with context & positions
- Layer-shape summary
- `--dry-run`
- NureonLang formatter
- IntelliJ Plugin

---

## Conclusion

`nueronlang-compiler-cli` is the first public entry point of the Alexandra ecosystem, enabling declarative neural architecture specification and high-level reasoning through DSL-based compilation.

It is intentionally minimal, extensible, and suitable for production workflows, CI pipelines, and research environments.

