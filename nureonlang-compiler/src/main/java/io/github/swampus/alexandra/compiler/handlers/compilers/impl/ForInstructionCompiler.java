package io.github.swampus.alexandra.compiler.handlers.compilers.impl;

import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.ForLoopLayer;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Compiler for FOR loop instructions.
 *
 * <p>Current behavior is intentionally minimal and mostly a stub:</p>
 * <ul>
 *   <li>Extracts loop variable name, {@code from} and {@code to} bounds from the IR.</li>
 *   <li>Creates a {@link ForLoopLayer} with a {@code null} body.</li>
 *   <li>Does NOT yet compile or wire the loop body subgraph.</li>
 * </ul>
 *
 * <p>Real loop body compilation and use of the loop variable still need to be implemented.</p>
 */
public class ForInstructionCompiler implements InstructionCompiler {

    private static final Logger log = LoggerFactory.getLogger(ForInstructionCompiler.class);

    @Override
    public void compile(Instruction instr,
                        NetworkModel model,
                        Map<String, Layer> layers) {

        Objects.requireNonNull(instr, "instruction must not be null");
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(layers, "layers map must not be null");

        // Name derived from loop variable or auto-generated
        String loopVar = instr.getVar();
        String name = (loopVar != null && !loopVar.isBlank())
                ? loopVar
                : "for_" + System.nanoTime();

        int from = (instr.getFromVal() instanceof Number nFrom) ? nFrom.intValue() : 0;
        int to   = (instr.getToVal()   instanceof Number nTo)   ? nTo.intValue()   : 1;

        if (log.isDebugEnabled()) {
            log.debug("Compiling FOR loop '{}': from={} to={}", name, from, to);
        }

        Layer bodyLayer = null;

        if (instr.getBody() != null && !instr.getBody().isEmpty()) {
            // TODO: implement compilation of loop body.
            // For now this is a pure stub: we ignore body instructions.
            // Convention idea:
            //   - instr.getBody() contains a block describing the loop body subgraph.
            //   - we compile it, locate its "root" layer, and assign it to bodyLayer.
        }

        ForLoopLayer forLayer = new ForLoopLayer(name, from, to, bodyLayer);

        if (layers.containsKey(name)) {
            log.warn("FOR loop layer '{}' overrides an existing layer in the current scope", name);
        }

        layers.put(name, forLayer);
        model.addLayer(forLayer);

        if (log.isDebugEnabled()) {
            log.debug("FOR loop '{}' registered as ForLoopLayer", name);
        }
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO (1): Compile loop body:
    //           - compile instructions in instr.getBody()
    //           - determine a "root" layer for the body and assign it to bodyLayer.
    //
    // TODO (2): Pass loop variable into the body:
    //           - support binding of the loop index (e.g. i) to expressions / names.
    //
    // TODO (3): Define IR convention for FOR:
    //           - semantics of from/to (inclusive/exclusive),
    //           - support for step, e.g. FOR i = 0..N STEP 2.
    //
    // TODO (4): Add validation:
    //           - ensure from <= to (or define behavior for reversed bounds),
    //           - ensure body is not empty for non-trivial loops.
    //
    // TODO (5): Integrate with ForLoopLayer improvements:
    //           - shape stability across iterations,
    //           - optional accumulation / reduction semantics.
    //
    // TODO (6): Add unit tests:
    //           - default bounds (no from/to),
    //           - explicit from/to,
    //           - empty body vs non-empty body,
    //           - name auto-generation behavior.
}
