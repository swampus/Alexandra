# Getting Started with Alexandra
_A quick guide to compiling, executing and experimenting with NureonLang networks._

---

## 1. Clone & Build
```bash
git clone https://github.com/swampus/Alexandra.git
cd Alexandra
mvn clean install
```

---

## 2. Create a Simple Network (`example.nl`)
```nl
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

---

## 3. Compile (CLI)
```bash
cd nureonlang-compiler
java -jar target/nureonlang-compiler-cli.jar ../example.nl --validation SHAPES --print-ir
```

---

## 3a. Compile via REST API (`networkapi-compiler`)

In addition to the CLI compiler, Alexandra provides a **REST-based compiler service**
that exposes parsing and compilation as HTTP endpoints.

This approach is recommended for:
- UI / frontend integration
- IDE support
- remote compilation
- orchestration via Oracle
- programmatic access from other services

### Start the Compiler API
```bash
cd networkapi-compiler
mvn spring-boot:run
```

The service starts on:
```
http://localhost:8080
```

Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

---

### Compile a Network (JSON)

**Endpoint**
```
POST /api/v1/compiler/compile
```

**Request**
```json
{
  "source": "MODULE Encoder\nBEGIN\n  LAYER e1 dense size=128\n  LAYER e2 dense size=64\n  CONNECT e1 -> e2\nEND\n\nINPUT x shape=(1,128)\nCALL Encoder\nOUTPUT e2",
  "compilationTraceRequired": false
}
```

**Successful Response (200)**
```json
{
  "valid": true,
  "networkModel": {
    "layers": [],
    "connections": [],
    "inputs": [],
    "outputs": []
  },
  "compilationTimeMs": 12
}
```

**Error Response (400)**
```json
{
  "valid": false,
  "errors": [
    {
      "type": "SYNTAX_ERROR",
      "message": "Unexpected token 'CONNECT'",
      "line": 5,
      "column": 3
    }
  ]
}
```

---

### Parse Only (Syntax Check)

**Endpoint**
```
POST /api/v1/compiler/parse
```

**Request**
```json
{
  "source": "INPUT x shape=(1,128)\nOUTPUT x",
  "compilationTraceRequired": false
}
```

**Response (valid source)**
```json
{
  "valid": true,
  "errors": []
}
```

**Response (invalid source)**
```json
{
  "valid": false,
  "errors": [
    {
      "type": "SYNTAX_ERROR",
      "message": "Missing OUTPUT definition",
      "line": 2,
      "column": 1
    }
  ]
}
```

---

### When to Use CLI vs REST

| Use Case | Recommended |
|--------|-------------|
| Local experiments | CLI |
| CI / batch jobs | CLI |
| UI / Web frontend | REST API |
| IDE integration | REST API |
| Oracle / Registry | REST API |
| Remote compilation | REST API |

Both interfaces use the **same compiler core** and produce identical results.

---

## 4. Execute
```bash
cd executor
mvn exec:java -Dexec.mainClass="io.github.swampus.alexandra.executor.DemoExecutor"
```

---

## 5. Dump IR
```bash
java -jar nureonlang-compiler-cli.jar example.nl --dump-json model.json
```

---

## 6. Next Steps
- Registry (coming)
- Oracle (coming)
- RuleLang (in development)
- Cluster Engine (planned)
- UI Console (uses `networkapi-compiler`)

---

## 7. Directory Map
```
Alexandra/
 ├── nureonlang-core/
 ├── nureonlang-ir/
 ├── nureonlang-compiler/
 ├── networkapi-compiler/
 ├── executor/
 ├── trainer/
 ├── registry/
 ├── oracle/
 ├── docs/
 └── GETTING_STARTED.md
```
