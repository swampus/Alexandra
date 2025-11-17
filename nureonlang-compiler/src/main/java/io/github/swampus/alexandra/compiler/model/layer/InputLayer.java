package io.github.swampus.alexandra.compiler.model.layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Represents an input node in the computation graph.
 *
 * <p>An {@code InputLayer} does not modify data — it simply exposes externally
 * provided vectors or tensors to the graph. During the forward pass it looks up
 * its own name in {@code Map<String, double[]>} and returns the associated
 * vector.</p>
 *
 * <p>Input layers may define either:</p>
 * <ul>
 *     <li>a fixed vector size (e.g., {@code size = 128}), or</li>
 *     <li>a structured {@code shape} (e.g., [batch, seq, dim]).</li>
 * </ul>
 *
 * <p>No activation is applied. Input validation is performed in validator
 * modules, not during forward execution.</p>
 */
public class InputLayer extends Layer {

    private static final Logger log = LoggerFactory.getLogger(InputLayer.class);

    private final int size;     // Fixed vector length OR -1 if unspecified
    private final Object shape; // Arbitrary shape descriptor (List, int[], symbolic tuple)

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Constructs an InputLayer with an explicit 1-D vector size.
     */
    public InputLayer(String name, int size) {
        super(name);
        this.size = size;
        this.shape = null;
    }

    /**
     * Constructs an InputLayer with a structured or symbolic shape descriptor.
     */
    public InputLayer(String name, Object shape) {
        super(name);
        this.size = -1;
        this.shape = shape;
    }

    // -------------------------------------------------------------------------
    // Metadata
    // -------------------------------------------------------------------------

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getActivation() {
        return null; // Input layers do not apply activation
    }

    @Override
    public Object getShape() {
        return shape; // Returning actual declared shape
    }

    // -------------------------------------------------------------------------
    // Forward pass
    // -------------------------------------------------------------------------

    /**
     * Retrieves the externally provided input vector.
     *
     * @throws IllegalArgumentException if no input for this layer was supplied.
     */
    @Override
    public double[] forward(Map<String, double[]> inputByName) {
        if (log.isDebugEnabled()) {
            log.debug("InputLayer.forward('{}'): available keys={}", getName(), inputByName.keySet());
        }

        double[] in = inputByName.get(getName());
        if (in == null) {
            throw new IllegalArgumentException(
                    "Missing input for InputLayer '" + getName() +
                            "'. Available keys: " + inputByName.keySet()
            );
        }
        return in;
    }

    // -------------------------------------------------------------------------
    // Future work
    // -------------------------------------------------------------------------

    // TODO: (1) Implement shape enforcement — if size > 0, validate input length.
    // TODO: (2) Add multi-dimensional input support via forwardUniversal().
    // TODO: (3) Add optional dtype (float32/float64) for I/O consistency.
    // TODO: (4) Add metadata: normalization, padding, masking rules.
    // TODO: (5) Integrate with shape validator to freeze shape at compile-time.
    // TODO: (6) Write unit tests covering:
    //           - valid input retrieval,
    //           - missing input key,
    //           - mismatched size,
    //           - structured shape usage.
    // TODO: (7) Add debug hooks to print input summary (shape/value statistics).
}
