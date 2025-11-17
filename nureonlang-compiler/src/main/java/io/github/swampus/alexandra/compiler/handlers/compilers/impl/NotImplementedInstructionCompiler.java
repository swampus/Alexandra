package io.github.swampus.alexandra.compiler.handlers.compilers.impl;

import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Fallback compiler for IR instructions that have no implementation yet.
 *
 * <p>This class is intentionally thrown during compilation to indicate that
 * the IR operation type is known but not supported. Use this as a placeholder
 * for new instruction types during incremental compiler development.</p>
 */
public class NotImplementedInstructionCompiler implements InstructionCompiler {

    private static final Logger log = LoggerFactory.getLogger(NotImplementedInstructionCompiler.class);

    private final String opName;

    public NotImplementedInstructionCompiler(String opName) {
        this.opName = opName;
    }

    @Override
    public void compile(Instruction instr, NetworkModel model, Map<String, Layer> layers) {
        // Log first — developers get context immediately in output
        log.error("Instruction '{}' is not implemented. IR: {}", opName, instr);

        // Hard fail — this path must not silently succeed
        throw new UnsupportedOperationException(
                "Compiler for operation '" + opName + "' is not implemented. " +
                        "Instruction: " + instr
        );
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / labs)
    // -------------------------------------------------------------------------

    // TODO (1): Add detailed diagnostic:
    //           Print expected input/output types and number of arguments.
    //
    // TODO (2): Add "suggestion engine":
    //           If opName similar to an implemented operation, suggest it.
    //
    // TODO (3): Add ability to auto-generate boilerplate compiler templates
    //           for new instructions.
    //
    // TODO (4): Add metrics counter:
    //           Track number of unimplemented instructions encountered.
    //
    // TODO (5): Add optional 'strict' mode:
    //           In strict mode -> throw exception,
    //           in relaxed mode -> log warning and skip the instruction.
    //
    // TODO (6): Implement a registry self-check:
    //           Detect if the IR contains operations without compilers.
    //
    // TODO (7): Add unit tests:
    //           - verify exception thrown
    //           - verify log message content
    //           - verify error includes opName and instruction details
    //
    // TODO (8): Add plugin interface to let external modules provide compilers.
}
