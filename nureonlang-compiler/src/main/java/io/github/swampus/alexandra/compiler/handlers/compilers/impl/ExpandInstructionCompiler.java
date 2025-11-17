package io.github.swampus.alexandra.compiler.handlers.compilers.impl;

import io.github.swampus.alexandra.compiler.IRNetworkCompiler;
import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Compiler for EXPAND instructions.
 *
 * <p>Current behavior:</p>
 * <ul>
 *   <li>If the instruction is an EXPAND with meta key {@code "GROUP"},
 *       writes this value into {@link NetworkModel#getMeta()} under the same key.</li>
 *   <li>Then simply compiles all instructions in {@code instr.getBody()} using
 *       the shared {@link IRNetworkCompiler} instance.</li>
 * </ul>
 *
 * <p>Essentially this is a "group annotation + inline body" construct.</p>
 */
public class ExpandInstructionCompiler implements InstructionCompiler {

    private static final Logger log = LoggerFactory.getLogger(ExpandInstructionCompiler.class);

    private final IRNetworkCompiler compiler;

    public ExpandInstructionCompiler(IRNetworkCompiler compiler) {
        this.compiler = Objects.requireNonNull(compiler, "compiler must not be null");
    }

    @Override
    public void compile(Instruction instr,
                        NetworkModel model,
                        Map<String, Layer> layers) {

        Objects.requireNonNull(instr, "instruction must not be null");
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(layers, "layers map must not be null");

        // Optional: handle GROUP metadata
        if (instr.getOp() == OpCode.EXPAND && instr.getMeta() != null
                && instr.getMeta().containsKey("GROUP")) {

            Object groupValue = instr.getMeta().get("GROUP");
            model.getMeta().put("GROUP", groupValue);

            if (log.isDebugEnabled()) {
                log.debug("EXPAND set GROUP meta to {}", groupValue);
            }
        }

        // Inline-expand the body: compile each child instruction as-is
        if (instr.getBody() != null) {
            for (Instruction sub : instr.getBody()) {
                compiler.compileInstruction(sub, model, layers);
            }
        }
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO (1): Define a proper schema for EXPAND meta:
    //           - GROUP, TAGS, NAMESPACE, etc.
    //
    // TODO (2): Support nested grouping:
    //           - maintain a stack of GROUP values instead of a single key in meta.
    //
    // TODO (3): Add validation for GROUP usage:
    //           - ensure GROUP values are well-formed (e.g. non-empty strings).
    //
    // TODO (4): Add integration with NetworkModel:
    //           - track groups per layer, not only at global meta level.
    //
    // TODO (5): Add unit tests:
    //           - EXPAND with GROUP and non-empty body,
    //           - EXPAND without GROUP,
    //           - nested EXPAND blocks.
    //
    // TODO (6): Consider introducing a dedicated "Group" / "Scope" abstraction
    //           instead of using plain meta keys on the NetworkModel.
}
