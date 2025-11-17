package io.github.swampus.alexandra.compiler.model.layer;

import java.util.Map;

/**
 * Represents a simple loop construct in the computation graph.
 *
 * <p>The {@code ForLoopLayer} repeatedly executes a single {@code body} layer
 * from {@code from} (inclusive) to {@code to} (exclusive). All iterations reuse
 * the same {@code inputByName} map; only the output of the body is passed to
 * the next iteration as the "current value".</p>
 *
 * <p>This layer models IR-level loops, but does not expose any
 * loop-variable-dependent behavior (i.e., {@code i} is not visible inside the body).</p>
 */
public class ForLoopLayer extends Layer {

    private final int from;
    private final int to;
    private final Layer body;

    public ForLoopLayer(String name, int from, int to, Layer body) {
        super(name);
        this.from = from;
        this.to = to;
        this.body = body;
    }

    /**
     * Repeatedly executes the loop body.
     *
     * <p>Important: Both the initial input and all loop iterations read from the
     * same input map {@code inputByName}. The loop output is the final output
     * of the body.</p>
     */
    @Override
    public double[] forward(Map<String, double[]> inputByName) {
        double[] out = resolveInput(inputByName);

        for (int i = from; i < to; i++) {
            out = body.forward(inputByName); // Always uses the same global inputs
        }

        return out;
    }

    @Override
    public int getSize() {
        // Loop size equals the body output size (if known)
        return (body != null) ? body.getSize() : -1;
    }

    @Override
    public String getActivation() {
        return null; // Loops are structural; no activation
    }

    @Override
    public Object getShape() {
        // Delegate shape reporting to the body
        return (body != null) ? body.getShape() : null;
    }


    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO: (1) Add loop-variable support — expose "i" to the body (via context map).
    // TODO: (2) Allow the body to use the previous iteration's output as input.
    // TODO: (3) Implement shape validation: ensure loop body shape is stable across iterations.
    // TODO: (4) Add runtime safety checks (e.g., from > to, negative bounds).
    // TODO: (5) Add unit tests:
    //           - zero iterations,
    //           - one iteration,
    //           - many iterations,
    //           - body with variable output shape (should fail in validator).
    // TODO: (6) Support loop unrolling during compile-time optimization.
    // TODO: (7) Integrate with forwardUniversal() for multi-dimensional tensors.
    // TODO: (8) Consider supporting break/continue semantics (advanced).
}
