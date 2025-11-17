# Getting Started with Alexandra
_A quick guide to compiling, executing and experimenting with NureonLang networks._

## 1. Clone & Build
```bash
git clone https://github.com/swampus/Alexandra.git
cd Alexandra
mvn clean install
```

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

## 3. Compile (CLI)
```bash
cd nureonlang-compiler
java -jar target/nureonlang-compiler-cli.jar ../example.nl --validation SHAPES --print-ir
```

## 4. Execute
```bash
cd executor
mvn exec:java -Dexec.mainClass="io.github.swampus.alexandra.executor.DemoExecutor"
```

## 5. Dump IR
```bash
java -jar nureonlang-compiler-cli.jar example.nl --dump-json model.json
```

## 6. Next Steps
- Registry (coming)
- Oracle (coming)
- RuleLang (in development)
- Cluster Engine (planned)

## 7. Directory Map
```
Alexandra/
 ├── nureonlang-core/
 ├── nureonlang-ir/
 ├── nureonlang-compiler/
 ├── executor/
 ├── trainer/
 ├── registry/
 ├── oracle/
 ├── docs/
 └── GETTING_STARTED.md
```
