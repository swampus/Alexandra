package io.github.swampus.alexandra.compiler.handlers.compilers.impl;

import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.IfLayer;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Compiler for simple IF instructions.
 *
 * <p>Current behavior is intentionally minimal and acts as a stub:</p>
 * <ul>
 *   <li>Creates an {@link IfLayer} with a predicate that always returns {@code true}.</li>
 *   <li>{@code thenLayer} and {@code elseLayer} are not wired yet (both {@code null}).</li>
 *   <li>The layer is registered in {@code layers} and {@link NetworkModel}.</li>
 * </ul>
 *
 * <p>Real condition parsing and branch wiring should be implemented later.</p>
 */
public class IfInstructionCompiler implements InstructionCompiler {

    private static final Logger log = LoggerFactory.getLogger(IfInstructionCompiler.class);

    @Override
    public void compile(Instruction instr,
                        NetworkModel model,
                        Map<String, Layer> layers) {

        Objects.requireNonNull(instr, "instruction must not be null");
        Objects.requireNonNull(model, "model must not be null");
        Objects.requireNonNull(layers, "layers map must not be null");

        String name = (instr.getName() != null && !instr.getName().isBlank())
                ? instr.getName()
                : "if_" + System.nanoTime();

        // TODO: compile real predicate from instr.getCond()
        Predicate<double[]> cond = x -> true;

        Layer thenLayer = null;
        Layer elseLayer = null;

        if (instr.getBody() != null && !instr.getBody().isEmpty()) {
            // TODO: convention: body[0] = then-block, body[1] = else-block (if present)
            //       compile subgraphs and bind their root layers to thenLayer / elseLayer.
        }

        IfLayer ifLayer = new IfLayer(name, cond, thenLayer, elseLayer);

        if (layers.containsKey(name)) {
            log.warn("IF layer '{}' overrides an existing layer in the current scope", name);
        }

        layers.put(name, ifLayer);
        model.addLayer(ifLayer);

        if (log.isDebugEnabled()) {
            log.debug("Compiled IF instruction into IfLayer '{}'", name);
        }
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO (1): Implement real condition compilation:
    //           - parse instr.getCond().getExpr()
    //           - support basic comparisons and logical operators.
    //
    // TODO (2): Compile then/else bodies:
    //           - body[0] => thenLayer subgraph root
    //           - body[1] => elseLayer subgraph root (optional)
    //
    // TODO (3): Define and document the IR convention for IF:
    //           - how many body blocks,
    //           - how they are structured (BLOCK, SEQ, etc.).
    //
    // TODO (4): Integrate with ConditionalLayer or unify semantics between IfLayer
    //           and ConditionalLayer to avoid duplication.
    //
    // TODO (5): Add validator support:
    //           - ensure then/else branches produce compatible shapes and sizes.
    //
    // TODO (6): Add unit tests:
    //           - IF with only then-branch,
    //           - IF with then+else branches,
    //           - malformed conditions (should fail early),
    //           - name auto-generation behavior.
}
