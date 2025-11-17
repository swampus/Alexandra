---
title: NureonLang Complete Tutorial and Specification
version: 1.0
author: Swampus
license: MIT
last_updated: 2025-11-09
---

```
███╗   ██╗██╗   ██╗██████╗ ███████╗ ██████╗ ███╗   ██╗
████╗  ██║██║   ██║██╔══██╗██╔════╝██╔═══██╗████╗  ██║
██╔██╗ ██║██║   ██║██████╔╝█████╗  ██║   ██║██╔██╗ ██║
██║╚██╗██║╚██╗ ██╔╝██╔══██╗██╔══╝  ██║   ██║██║╚██╗██║
██║ ╚████║ ╚████╔╝ ██║  ██║███████╗╚██████╔╝██║ ╚████║
╚═╝  ╚═══╝  ╚═══╝  ╚═╝  ╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═══╝
```

> **NureonLang** — Neural Architecture Description Language

## 📘 Contents

- [1. Introduction](#1-introduction)
- [2. Core Philosophy](#2-core-philosophy)
- [3. Basic Syntax Overview](#3-basic-syntax-overview)
- [4. Program Structure](#4-program-structure)
- [5. Blocks and Statements](#5-blocks-and-statements)
- [6. Layer Declarations](#6-layer-declarations)
- [7. Parameters and Shapes](#7-parameters-and-shapes)
- [8. Expressions](#8-expressions)
- [9. Connections](#9-connections)
- [10. Control Flow (FOR / IF)](#10-control-flow-for--if)
- [11. Modules](#11-modules)
- [12. Macros](#12-macros)
- [13. File References](#13-file-references)
- [14. Expand Statements](#14-expand-statements)
- [15. MiniResNet Example](#15-miniresnet-example)
- [16. Syntax and Semantic Validation](#16-syntax-and-semantic-validation)
- [17. Grammar Appendix](#17-grammar-appendix)
- [18. Future Directions](#18-future-directions)
- [19. Acknowledgments](#19-acknowledgments)

# 1. Introduction

NureonLang is a domain-specific language for describing and manipulating neural architectures declaratively.

Unlike imperative frameworks (TensorFlow, PyTorch), NureonLang focuses on structure and composition, enabling reasoning about architecture separately from execution.

# Acknowledgments

Developed by Swampus  
Special thanks to the open-source community and the ANTLR team for foundational tools and ideas.



## 2. Core Philosophy

NureonLang is built on the principle that **neural architecture should be declared, not coded**.  
Its philosophy follows three main ideas:

1. **Separation of Architecture and Execution** –  
   Define *what* a network is, not *how* it runs.

2. **Declarative Composition** –  
   Use a language of *structure*, not control flow.  
   Architecture becomes a *blueprint*, not a script.

3. **Symbolic Reasoning** –  
   Because the language is structural, it can be analyzed, transformed, optimized, or even evolved automatically.

💡 *Analogy:*  
If TensorFlow is an engine, then NureonLang is an architect’s blueprint — you can reason about it before construction begins.

---

## 3. Basic Syntax Overview

NureonLang uses a clean, readable syntax inspired by Pascal and DSLs used in compilers.  
Blocks are enclosed in `BEGIN ... END`, and indentation has no semantic meaning.

Example:

```nl
BEGIN
    LAYER input shape=(3,224,224)
    LAYER conv1 Conv2D filters=32 kernel=(3,3)
    CONNECT input -> conv1
END
```

Key syntax rules:
- Keywords are **UPPERCASE** (`LAYER`, `CONNECT`, `FOR`, `MODULE`, etc.).
- Identifiers are **case-sensitive**.
- Comments use `//` or `/* ... */`.
- Every construct forms a *statement* that can exist inside a block.

---

## 4. Program Structure

A valid NureonLang program consists of **statements**, **blocks**, and an optional **FILE** declaration.

```nl
BEGIN
    // Layers and connections
    LAYER input shape=(3,224,224)
    LAYER dense Dense units=128 activation=ReLU
    CONNECT input -> dense

    FILE "network.json"
END
```

Core structure:

| Element | Description |
|----------|-------------|
| `BEGIN ... END` | Defines a top-level or nested block |
| `LAYER` | Declares a new layer or node |
| `CONNECT` | Defines a directional link between components |
| `DEFINE`, `MODULE` | Define reusable templates or modules |
| `FILE` | Specifies export destination for compiled network |

💡 *Tip:*  
A NureonLang file can contain multiple modules and macros, but only one top-level `BEGIN...END` structure and one `FILE` statement.

---

## 5. Blocks and Statements

A **block** defines a local scope in NureonLang.  
Blocks can contain any valid statements, including nested `BEGIN...END` structures.

```nl
BEGIN
    LAYER input shape=(3,224,224)
    LAYER conv1 filters=64 kernel=(3,3)
    BEGIN
        LAYER inner type=ReLU
        CONNECT conv1 -> inner
    END
    CONNECT inner -> output
END
```

Blocks are used in:
- **Modules** — reusable architectural units.
- **Control Flow** — e.g. FOR loops.
- **Macros** — encapsulated code templates.

💡 *Tip:* A block never needs braces or indentation — structure is **semantic**, not positional.

---

## 6. Layer Declarations

Layers are the atomic building blocks of neural architectures.

There are two forms:

```nl
LAYER ID param+
LAYER ID ID param+
```

- The first (`#anonLayer`) defines a layer type with its parameters.
- The second (`#namedLayer`) declares a named layer instance.

Example:

```nl
LAYER Dense units=128 activation=ReLU
LAYER conv1 Conv2D filters=32 kernel=(3,3) stride=1 padding=Same
```

Each layer defines configuration metadata that can later be linked with others using `CONNECT`.

---

## 7. Parameters and Shapes

Parameters specify configurable attributes for layers or modules.  
Each parameter follows the form `ID = value`.

Example:

```nl
LAYER Dense units=256 activation=ReLU
LAYER Input shape=(3,224,224)
```

Shapes can be written in two ways:

```nl
shape = 256 x 256        // Simple shape (rows × cols)
shape = (3, 224, 224)    // Tuple shape (channels, height, width)
```

Supported value types:
- **INT** — integers (e.g. 128)
- **FLOAT** — floating-point numbers (e.g. 0.01)
- **STRING** — quoted strings
- **EXPR** — mathematical expressions

---

## 8. Expressions

Expressions provide arithmetic and logical evaluation.  
They can appear in parameters, control conditions, or assignments.

Supported forms:

| Type | Example | Meaning |
|------|----------|---------|
| Addition/Subtraction | `x + y`, `n - 1` | Basic arithmetic |
| Multiplication/Division | `a * b`, `x / y` | Scaling or normalization |
| Power | `x ^ 2` | Exponentiation |
| Parentheses | `(x + 2) * 3` | Grouping |
| Variable reference | `ID` | Uses a defined variable |
| Array access | `weights[3]` | Retrieves array element |

Example:

```nl
LET i = 0;
FOR i FROM 0 TO 4 BEGIN
    LAYER Dense units=(i+1)*64
END
```

---

## 9. Connections

Connections define directed edges between components — layers, modules, or macros.

```nl
CONNECT source -> target
```

Example:

```nl
LAYER input shape=(3,224,224)
LAYER conv filters=32 kernel=(3,3)
LAYER relu type=ReLU
LAYER output type=Dense units=10

CONNECT input -> conv
CONNECT conv -> relu
CONNECT relu -> output
```

Connections can use **dotted identifiers** to access nested modules:

```nl
CONNECT encoder.block1.conv -> decoder.block2.deconv
```

💡 *Best Practice:*  
Always connect **outputs → inputs** logically.  
While NureonLang does not require explicit tensor typing, proper flow ensures clean IR compilation.

---

## 10. Control Flow (FOR / IF)

Control flow in NureonLang provides a declarative way to express repetition and conditional structure inside architecture definitions.

### FOR Loop

A `FOR` loop allows repeating a sequence of statements over a numeric range.

```nl
FOR i FROM 0 TO 3 BEGIN
    LAYER Dense units=(i+1)*64 activation=ReLU
    CONNECT Dense[i] -> Dense[i+1]
END
```

Rules:
- The iterator `ID` is scoped to the loop block.
- Expressions after `FROM` and `TO` can be numeric or computed.
- Loops can contain any valid statement, including module declarations.

Example – stacking convolutional blocks:

```nl
FOR i FROM 1 TO 4 BEGIN
    LAYER conv{i} Conv2D filters=32*i kernel=(3,3)
    LAYER relu{i} ReLU
    CONNECT conv{i} -> relu{i}
END
```

💡 *Tip:* The compiler can unroll loops for IR or directly translate to a runtime builder pattern.

### IF Statement

Conditional statements allow structural branching.

```nl
IF input_size > 512 BEGIN
    LAYER compress Dense units=256
ELSE
    LAYER expand Dense units=1024
END
```

You can also nest them freely:

```nl
IF mode == "training" BEGIN
    LAYER dropout Dropout rate=0.3
ELSE
    IF mode == "eval" BEGIN
        LAYER norm BatchNorm
    END
END
```

---

## 11. Modules

Modules encapsulate reusable groups of layers and connections.  
They are defined using the `MODULE` keyword followed by a name and a block.

```nl
MODULE Encoder BEGIN
    LAYER conv1 Conv2D filters=64 kernel=(3,3)
    LAYER relu1 ReLU
    CONNECT conv1 -> relu1
END
```

To use a module, simply **call** it by name:

```nl
Encoder
```

You can also connect modules together:

```nl
CONNECT Encoder -> Decoder
```

Modules can be nested and parameterized (if macros or `LET` variables are used inside).

Example – Encoder-Decoder pair:

```nl
MODULE Encoder BEGIN
    LAYER conv1 Conv2D filters=128 kernel=(3,3)
    LAYER relu1 ReLU
    CONNECT conv1 -> relu1
END

MODULE Decoder BEGIN
    LAYER dense1 Dense units=256
    LAYER out Dense units=10 activation=Softmax
    CONNECT dense1 -> out
END

CONNECT Encoder -> Decoder
```

Modules support full scope isolation, allowing the same layer names to be reused in different modules.

---

## 12. Macros

Macros in NureonLang are reusable code templates.  
They are defined using the `DEFINE` keyword.

```nl
DEFINE ResidualBlock(x, filters) BEGIN
    LAYER conv1 Conv2D filters=filters kernel=(3,3)
    LAYER relu1 ReLU
    LAYER conv2 Conv2D filters=filters kernel=(3,3)
    CONNECT x -> conv1
    CONNECT conv1 -> relu1
    CONNECT relu1 -> conv2
END
```

A macro can be called as:

```nl
ResidualBlock(input, 64)
```

### Rules

- Macro names follow the same naming convention as modules.
- Parameters are positional.
- The number of arguments must match the declared parameters.

Example with nested macros:

```nl
DEFINE Block(x) BEGIN
    LAYER dense Dense units=128
    CONNECT x -> dense
END

DEFINE DeepNetwork(input) BEGIN
    Block(input)
    Block(dense)
END
```

💡 *Tip:* Macros can contain control flow or module calls — they are fully composable templates.

---

## 13. File References

A **file reference** defines where to export the compiled network architecture or metadata.

```nl
FILE "model_output.json"
```

The `FILE` keyword must appear **once per program**, usually at the end of the file.  
It can export to any supported format: JSON, XML, YAML, or custom.

Example:

```nl
BEGIN
    MODULE Encoder BEGIN
        LAYER conv1 Conv2D filters=64 kernel=(3,3)
        LAYER relu1 ReLU
        CONNECT conv1 -> relu1
    END

    MODULE Decoder BEGIN
        LAYER dense1 Dense units=128
        LAYER out Dense units=10 activation=Softmax
        CONNECT dense1 -> out
    END

    CONNECT Encoder -> Decoder
END

FILE "autoencoder.json"
```

---

## 14. Expand Statements

The `EXPAND` statement allows structural replication and transformation of blocks or spaces.  
It’s used to describe **symmetric**, **multi-dimensional**, or **grouped** topologies.

### Syntax

```nl
EXPAND target block
```

Where `target` may define the expansion rule:

| Type | Example | Meaning |
|------|----------|---------|
| Dimensional | `SPACE = 3D D` | Expands the block in 3D space |
| Named Group | `GROUP = Residual` | Repeats for named groups |
| With Group | `WITH GROUP Residual` | Adds content to an existing group |
| Custom | `ID` | Arbitrary rule, resolved by compiler |

### Example – Multi-branch expansion

```nl
EXPAND SPACE = 3D BEGIN
    LAYER conv1 Conv3D filters=32 kernel=(3,3,3)
    LAYER relu ReLU
    CONNECT conv1 -> relu
END
```

### Example – Group Expansion

```nl
EXPAND GROUP = Residual BEGIN
    DEFINE Block(x) BEGIN
        LAYER conv Conv2D filters=64 kernel=(3,3)
        LAYER relu ReLU
        CONNECT x -> conv
        CONNECT conv -> relu
    END
    Block(input)
END
```

### Notes

- `EXPAND` is primarily used for **generator-style architectures** (e.g., UNet, ResNet).
- It can appear at any scope level, including inside macros.

---

## 15. MiniResNet Example

Below is a practical example of how to describe a **MiniResNet** architecture in NureonLang.

```nl
// MiniResNet Example
BEGIN
    // Input Layer
    LAYER input shape=(3,224,224)

    // Residual Block Macro
    DEFINE ResidualBlock(x, filters) BEGIN
        LAYER conv1 Conv2D filters=filters kernel=(3,3) padding=Same
        LAYER relu1 ReLU
        LAYER conv2 Conv2D filters=filters kernel=(3,3) padding=Same
        CONNECT x -> conv1
        CONNECT conv1 -> relu1
        CONNECT relu1 -> conv2
    END

    // Encoder Module
    MODULE Encoder BEGIN
        ResidualBlock(input, 64)
        ResidualBlock(conv2, 128)
    END

    // Decoder Module
    MODULE Decoder BEGIN
        LAYER dense1 Dense units=256
        LAYER output Dense units=10 activation=Softmax
        CONNECT dense1 -> output
    END

    // Combine Encoder and Decoder
    CONNECT Encoder -> Decoder

    // File output
    FILE "miniresnet.json"
END
```

### Highlights

- Demonstrates **macros**, **modules**, and **connections**.
- Easy to convert to intermediate IR or direct code.
- Shows how NureonLang abstracts away explicit tensor management.

💡 *Pro Tip:* You can extend this into a **Conditional AutoEncoder** by adding `IF` logic for training mode or by wrapping residual blocks inside `FOR` loops for dynamic depth control.

---

## 16. Syntax and Semantic Validation

NureonLang uses **ANTLR4** for syntax parsing and **semantic validation layers** for structure checking.

### 16.1 Syntax Validation

The grammar defines lexical and parser rules for all keywords, identifiers, and expressions.

Example of valid parsing:

```nl
LAYER conv Conv2D filters=64 kernel=(3,3)
CONNECT conv -> relu
```

Common syntax errors:

| Error | Example | Fix |
|-------|----------|-----|
| Missing `END` | `BEGIN LAYER a Dense END` → `BEGIN LAYER a Dense END` | Ensure all blocks close properly |
| Unknown token | `LAYER# Dense units=64` | Use valid identifiers (letters, numbers, underscores) |
| Unmatched parentheses | `LAYER x shape=(3,224,224` | Always close parentheses |

💡 *Tip:* The parser can be run independently for linting or IDE integration.

### 16.2 Semantic Validation

After parsing, the **Semantic Validator** ensures:
- All layers and connections are defined before use.
- No duplicate layer names exist in the same scope.
- Loops and macros expand correctly.
- File reference is unique per program.

Example semantic error:

```nl
CONNECT a -> b   // Error: undefined layer 'a'
```

Validation process typically follows:

1. Parse (ANTLR4 → AST)
2. Build symbol table
3. Validate structure
4. Export IR / JSON / GraphML

### 16.3 Best Practices

✅ Always define modules and macros before usage.  
✅ Use consistent naming (`camelCase` or `snake_case`).  
✅ Keep expansions (`EXPAND`, `FOR`) logically isolated.  
✅ Use comments generously — NureonLang supports both `//` and `/* */`.

---

## 17. Grammar Appendix

This is the official ANTLR4 grammar used to generate the parser.

```antlr
grammar NureonLang;

program : statement+ fileRef? EOF ;

block : BEGIN statement* END ;

statement
    : block
    | layerDecl
    | connectStmt
    | controlStmt
    | moduleDecl
    | moduleCall
    | letStmt
    | macroDecl
    | macroCall
    | expandStmt
    ;

layerDecl
    : LAYER ID param+
    | LAYER ID ID param+
    ;

param : ID EQ value ;

value : expr | shape ;

shape
    : INT ID INT
    | LPAREN INT (COMMA INT)* RPAREN
    ;

expr
    : expr POW expr
    | expr op=('*'|'/') expr
    | expr op=('+'|'-') expr
    | ID LBRACK expr RBRACK
    | ID
    | INT
    | FLOAT
    | STRING
    | LPAREN expr RPAREN
    ;

dottedId : ID ('.' ID)* (LBRACK expr RBRACK)* ;

connectStmt : CONNECT dottedId ARROW dottedId ;

controlStmt : forLoop | ifStmt ;

forLoop : FOR ID FROM expr TO expr block ;

ifStmt : IF condition block (ELSE block)? ;

condition : expr comparator value ;

comparator : EQ | NEQ | EQEQ | LT | LTE | GT | GTE ;

moduleDecl : MODULE ID block ;

moduleCall : ID ;

letStmt : LET ID (LBRACK expr RBRACK)? EQ expr SEMI ;

macroDecl : DEFINE ID LPAREN paramList? RPAREN block ;

macroCall : ID LPAREN argList? RPAREN ;

paramList : ID (COMMA ID)* ;

argList : expr (COMMA expr)* ;

fileRef : FILE STRING ;

expandStmt : EXPAND expandTarget block ;

expandTarget
    : SPACE EQ INT ID
    | GROUP EQ ID
    | WITH GROUP ID
    | ID
    ;

BEGIN : 'BEGIN';
END : 'END';
LAYER : 'LAYER';
CONNECT : 'CONNECT';
MODULE : 'MODULE';
DEFINE : 'DEFINE';
LET : 'LET';
FOR : 'FOR';
FROM : 'FROM';
TO : 'TO';
IF : 'IF';
ELSE : 'ELSE';
FILE : 'FILE';
EXPAND : 'EXPAND';
SPACE : 'SPACE';
GROUP : 'GROUP';
WITH : 'WITH';

EQEQ : '==';
EQ : '=';
NEQ : '!=';
LT : '<';
LTE : '<=';
GT : '>';
GTE : '>=';

ARROW : '->';
COMMA : ',';
LPAREN : '(';
RPAREN : ')';
LBRACK : '[';
RBRACK : ']';
SEMI : ';';
POW : '^';

ID : [a-zA-Z_][a-zA-Z_0-9]*;
INT : [0-9]+;
FLOAT : [0-9]+ '.' [0-9]*;
STRING : '"' (~["\] | '\' .)* '"';

LINE_COMMENT : '//' ~[\r\n]* -> skip;
BLOCK_COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\r\n]+ -> skip;
```

---

## 18. Future Directions

The roadmap for NureonLang includes:

1. **Type System Integration**  
   Add tensor typing and automatic shape inference.

2. **Conditional Compilation**  
   Allow macros and modules to be toggled via environment flags.

3. **External Function Binding**  
   Integrate custom Java/Python functions via FFI (Foreign Function Interface).

4. **Optimizer Plugins**  
   Implement compiler passes for graph optimization, pruning, and quantization.

5. **IDE and LSP Support**  
   Provide syntax highlighting, autocomplete, and real-time validation.

6. **Quantum and Probabilistic Extensions**  
   Experimental: describe superposed architectures for quantum ML backends.

---

## 19. Acknowledgments

Developed by **Swampus**  
© 2025 — MIT License

Special thanks to:
- The **ANTLR** project for the foundation of parsing.
- **OpenAI** tools for assisted language prototyping.
- Early testers who provided feedback on syntax design.
- The future — for making this language real.

---

> *"Architecture is not written — it is composed."*  
> — Swampus, *Notes on NureonLang*, 2025
