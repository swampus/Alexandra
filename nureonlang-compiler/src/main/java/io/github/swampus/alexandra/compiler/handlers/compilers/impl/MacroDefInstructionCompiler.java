package io.github.swampus.alexandra.compiler.handlers.compilers.impl;

import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.compiler.model.layer.MacroLayer;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Compiler for macro definitions (MACRO_DEF / DEFINE-like IR instructions).
 *
 * <p>Current behavior:</p>
 * <ul>
 *   <li>Ensures the macro has a name (generates a fallback if needed).</li>
 *   <li>Wraps the macro body and parameters into a {@link MacroLayer}.</li>
 *   <li>Registers this layer in both the {@code layers} map and the {@link NetworkModel}.</li>
 * </ul>
 *
 * <p>Execution of the macro logic itself is handled elsewhere (e.g., via MACRO_CALL
 * or CALL instructions). This compiler only wires the definition into the graph.</p>
 */
public class MacroDefInstructionCompiler implements InstructionCompiler {

    private static final Logger log = LoggerFactory.getLogger(MacroDefInstructionCompiler.class);

    @Override
    public void compile(Instruction instr,
                        NetworkModel model,
                        Map<String, Layer> layers) {

        if (instr == null) {
            throw new IllegalArgumentException("MacroDefInstructionCompiler: instruction must not be null");
        }

        // Ensure we always have some name to bind the macro under
        String name = (instr.getName() != null && !instr.getName().isBlank())
                ? instr.getName()
                : "macro_" + System.nanoTime();

        if (log.isDebugEnabled()) {
            log.debug("Compiling macro definition '{}'", name);
        }

        MacroLayer macro = new MacroLayer(name);
        macro.setParams(instr.getParams()); // may be null; MacroLayer handles that
        macro.setBody(instr.getBody());     // IR body as-is

        if (layers.containsKey(name)) {
            log.warn("Macro '{}' overrides an existing layer with the same name", name);
        }

        layers.put(name, macro);
        model.addLayer(macro);

        if (log.isInfoEnabled()) {
            log.info("Macro '{}' registered as MacroLayer", name);
        }
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO (1): Also register macro definition in NetworkModel macros map
    //           (e.g., model.addMacro(name, instr)) if not already done elsewhere.
    //
    // TODO (2): Validate macro parameters:
    //           - check for duplicate parameter names
    //           - enforce naming conventions
    //
    // TODO (3): Add scope rules for macros:
    //           - global vs module-local macros
    //           - shadowing behavior
    //
    // TODO (4): Add diagnostics:
    //           - warn if body is empty
    //           - warn if macro is defined but never called
    //
    // TODO (5): Add unit tests:
    //           - macro with explicit name
    //           - macro without name (auto-generated)
    //           - macro overriding existing layer
    //           - null params / null body behavior
    //
    // TODO (6): Support macro-level metadata (e.g. tags, documentation comments).
    //
    // TODO (7): Add integration tests with MacroCallInstructionCompiler / CALL logic.
}
