package io.github.swampus.alexandra.cli;

import io.github.swampus.alexandra.compiler.NetworkCompilerFacade;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.compiler.validator.ValidationLevel;
import io.github.swampus.alexandra.infrastructure.NureonLangInfrastructureFacade;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Minimal command-line entrypoint for the NureonLang compiler.
 *
 * Usage:
 *   java -jar nueronlang-compiler-cli.jar compile path/to/model.nl [--validation none|structural|shapes]
 *
 * This CLI:
 *  - parses a NureonLang source file,
 *  - compiles it into a NetworkModel,
 *  - prints a short summary via logger.
 */
public final class NueronlangCompilerCli {

    private static final Logger log = LoggerFactory.getLogger(NueronlangCompilerCli.class);

    public static void main(String[] args) {
        int exitCode = new NueronlangCompilerCli().run(args);
        System.exit(exitCode);
    }

    private final NureonLangInfrastructureFacade infra = new NureonLangInfrastructureFacade();

    public int run(String[] args) {
        if (args.length == 0 || isHelp(args[0])) {
            printUsage();
            return 0;
        }

        String command = args[0];
        if (command.equals("compile")) {
            return handleCompileCommand(slice(args, 1));
        }
        System.err.println("Unknown command: " + command);
        printUsage();
        return 1;
    }

    private boolean isHelp(String arg) {
        return "-h".equals(arg) || "--help".equals(arg) || "help".equals(arg);
    }

    private void printUsage() {
        // Usage — это по сути «help», логгер тут тоже норм
        log.info("""
                NueronLang Compiler CLI
                -----------------------
                
                Usage:
                  nueronlang-compiler-cli compile <source.nl> [--validation none|structural|shapes]
                
                Commands:
                  compile     Parse and compile a NureonLang source file into a NetworkModel.
                
                Options (for `compile`):
                  --validation LEVEL
                      Validation level: none | structural | shapes
                      Default: shapes
                """);
    }

    private int handleCompileCommand(String[] args) {
        if (args.length == 0) {
            System.err.println("Missing source file path.");
            printUsage();
            return 1;
        }

        String sourcePath = args[0];
        ValidationLevel level = ValidationLevel.SHAPES; // default

        // Parse optional flags
        for (int i = 1; i < args.length; i++) {
            if ("--validation".equals(args[i]) && i + 1 < args.length) {
                level = parseValidationLevel(args[++i]);
            } else {
                System.err.println("Unknown option: " + args[i]);
                printUsage();
                return 1;
            }
        }

        try {
            Path path = Path.of(sourcePath);
            if (!Files.exists(path)) {
                System.err.println("File not found: " + path);
                return 1;
            }

            String code = Files.readString(path);

            log.info("Parsing NureonLang source: {}", path);
            Instruction root = infra.parseCode(code);

            log.info("Compiling with validation level: {}", level);
            NetworkCompilerFacade compiler = new NetworkCompilerFacade(level);
            NetworkModel model = compiler.compile(root);

            printModelSummary(model);
            log.info("Compilation finished successfully.");
            return 0;
        } catch (Exception e) {
            System.err.println("Compilation failed: " + e.getMessage());
            log.error("Compilation failed", e);
            return 2;
        }
    }

    private ValidationLevel parseValidationLevel(String raw) {
        if (raw == null) return ValidationLevel.SHAPES;
        String v = raw.toLowerCase(Locale.ROOT);
        return switch (v) {
            case "none" -> ValidationLevel.NONE;
            case "structural" -> ValidationLevel.STRUCTURAL;
            case "shapes" -> ValidationLevel.SHAPES;
            default -> {
                log.warn("Unknown validation level: {} (allowed: none, structural, shapes). Falling back to SHAPES.", raw);
                yield ValidationLevel.SHAPES;
            }
        };
    }

    private void printModelSummary(NetworkModel model) {
        List<Layer> all = model.getAllLayers();
        log.info("Model summary:");
        log.info("  Total layers: {}", all.size());

        log.info("  Input layers:");
        for (Layer l : model.getInputLayers()) {
            log.info("    - {} ({})", l.getName(), l.getClass().getSimpleName());
        }

        log.info("  Output layers:");
        for (Layer l : model.getOutputLayers()) {
            log.info("    - {} ({})", l.getName(), l.getClass().getSimpleName());
        }
    }

    // Small helper: slice array from offset
    private static String[] slice(String[] src, int from) {
        if (from >= src.length) return new String[0];
        String[] out = new String[src.length - from];
        System.arraycopy(src, from, out, 0, out.length);
        return out;
    }
}
