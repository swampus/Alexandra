package io.github.swampus.alexandra.compiler.model.layer;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Conditional layer that routes execution to one of two branches
 * ({@code thenLayer} or {@code elseLayer}) based on a predicate over the input.
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>Evaluates {@link #condition} on {@link #resolveInput(Map)} (a double[] view of the input).</li>
 *   <li>If the predicate returns {@code true}, forwards to {@code thenLayer}.</li>
 *   <li>If the predicate returns {@code false} and {@code elseLayer} is not {@code null},
 *       forwards to {@code elseLayer}.</li>
 *   <li>If {@code elseLayer} is {@code null}, returns {@code inputByName.get(getName())} as a fallback.</li>
 * </ul>
 *
 * <p>This is a runtime-level conditional, separate from compile-time IF constructs
 * used in the IR/Compiler.</p>
 */
public class IfLayer extends Layer {

    private final Layer thenLayer;
    private final Layer elseLayer;
    private final Predicate<double[]> condition;

    public IfLayer(String name,
                   Predicate<double[]> condition,
                   Layer thenLayer,
                   Layer elseLayer) {
        super(name);
        this.condition = condition;
        this.thenLayer = thenLayer;
        this.elseLayer = elseLayer;
    }

    /**
     * Routes the forward pass based on the evaluated predicate.
     */
    @Override
    public double[] forward(Map<String, double[]> inputByName) {
        double[] input = resolveInput(inputByName);

        if (condition.test(input)) {
            return thenLayer.forward(inputByName);
        } else if (elseLayer != null) {
            return elseLayer.forward(inputByName);
        } else {
            // Fallback: return direct input if no else-branch is defined.
            return inputByName.get(getName());
        }
    }

    @Override
    public int getSize() {
        // Delegate to thenLayer when available; otherwise unknown.
        return (thenLayer != null) ? thenLayer.getSize() : -1;
    }

    @Override
    public String getActivation() {
        // IF layer itself is not an activation function.
        return null;
    }

    @Override
    public Object getShape() {
        // Delegate to thenLayer when available; otherwise no explicit shape.
        return (thenLayer != null) ? thenLayer.getShape() : null;
    }

    // -------------------------------------------------------------------------
    // Future work (for TODOs / students / issues)
    // -------------------------------------------------------------------------

    // TODO: (1) Add support for passing a richer context into Predicate (not only double[]).
    // TODO: (2) Ensure that thenLayer and elseLayer have compatible shapes; enforce via validator.
    // TODO: (3) Add optional logging hook to trace which branch was taken (debug-level).
    // TODO: (4) Support nested conditionals and short-circuit evaluation semantics if needed.
    // TODO: (5) Add unit tests for:
    //           - predicate true/false,
    //           - null elseLayer behavior,
    //           - shape/size propagation.
    // TODO: (6) Consider merging IfLayer and ConditionalLayer concepts if they overlap in the model.
}
