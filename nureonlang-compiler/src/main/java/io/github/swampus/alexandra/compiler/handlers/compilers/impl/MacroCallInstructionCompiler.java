package io.github.swampus.alexandra.compiler.handlers.compilers.impl;

import io.github.swampus.alexandra.compiler.IRNetworkCompiler;
import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Compiler for macro invocation instructions (MACRO_CALL / CALL).
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>Looks up the macro definition in {@link NetworkModel} by name.</li>
 *   <li>Builds a binding map between macro parameters and call arguments.</li>
 *   <li>Generates a unique suffix for all internal names in this macro instance.</li>
 *   <li>Clones and rewrites the macro body using
 *       {@link IRNetworkCompiler#deepCloneWithMultipleReplace(Instruction, Map, Set)}.</li>
 *   <li>Compiles each cloned instruction into the current graph.</li>
 * </ul>
 *
 * <p>The goal is to make each macro call expand into its own isolated subgraph
 * while preserving external argument names.</p>
 */
public class MacroCallInstructionCompiler implements InstructionCompiler {

    private static final Logger log = LoggerFactory.getLogger(MacroCallInstructionCompiler.class);

    private final IRNetworkCompiler compiler;

    public MacroCallInstructionCompiler(IRNetworkCompiler compiler) {
        this.compiler = Objects.requireNonNull(compiler, "compiler must not be null");
    }

    @Override
    public void compile(Instruction instr,
                        NetworkModel model,
                        Map<String, Layer> layers) {

        if (instr == null) {
            throw new IllegalArgumentException("MacroCallInstructionCompiler: instruction must not be null");
        }

        String macroName = instr.getName();
        List<String> args = instr.getInputs();

        if (macroName == null || macroName.isBlank()) {
            throw new IllegalArgumentException("Macro call has no name: " + instr);
        }

        Instruction macroDef = model.getMacro(macroName);
        if (macroDef == null) {
            throw new IllegalArgumentException("Macro not found: " + macroName);
        }

        List<String> params = macroDef.getInputs();
        if (params == null) {
            throw new IllegalStateException("Macro '" + macroName + "' is defined without a parameter list.");
        }

        if (args == null) {
            args = List.of();
        }

        if (params.size() != args.size()) {
            throw new IllegalArgumentException(
                    "Argument mismatch in macro call '" + macroName + "': expected " +
                            params.size() + " args, got " + args.size()
            );
        }

        if (log.isDebugEnabled()) {
            log.debug("Expanding macro call '{}' with args={}", macroName, args);
        }

        // Build bindings param -> arg
        Map<String, String> bindings = new HashMap<>();
        for (int i = 0; i < params.size(); i++) {
            bindings.put(params.get(i), args.get(i));
        }

        // Unique suffix to avoid name clashes between different macro instances
        String macroSuffix = "__" + macroName + "_" + UUID.randomUUID().toString().substring(0, 4);
        bindings.put("__suffix__", macroSuffix);

        // External names known in the current compilation scope
        Set<String> externalNames = new HashSet<>(layers.keySet());

        // Expand each instruction in macro body
        if (macroDef.getBody() != null) {
            for (Instruction sub : macroDef.getBody()) {
                Instruction clone = compiler.deepCloneWithMultipleReplace(sub, bindings, externalNames);
                compiler.compileInstruction(clone, model, layers);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Macro '{}' expanded with suffix {}", macroName, macroSuffix);
        }
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO (1): Add support for default parameter values and named arguments.
    //
    // TODO (2): Add constraint checks:
    //           - detect recursive macro expansion (infinite recursion),
    //           - detect ambiguous or conflicting parameter names.
    //
    // TODO (3): Add debug tracing:
    //           - record expansion trace in NetworkModel meta,
    //           - optionally emit a human-readable expansion log.
    //
    // TODO (4): Integrate with validator:
    //           - ensure that macro expansion does not break shape consistency,
    //           - ensure that expanded graph has no cycles (unless explicitly allowed).
    //
    // TODO (5): Add caching of expanded macro templates where possible
    //           (reuse structure for identical call signatures).
    //
    // TODO (6): Add unit tests for:
    //           - simple macro with one parameter,
    //           - macro with multiple parameters,
    //           - nested macro calls,
    //           - name collision scenarios.
    //
    // TODO (7): Consider introducing a dedicated MacroExpander service
    //           to separate expansion logic from compiler wiring.
}
