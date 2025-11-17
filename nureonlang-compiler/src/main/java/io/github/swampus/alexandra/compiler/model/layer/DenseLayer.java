package io.github.swampus.alexandra.compiler.model.layer;

import io.github.swampus.alexandra.compiler.model.Activation;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fully-connected (dense) layer with optional activation.
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>Concatenates all input layer outputs into a single vector.</li>
 *   <li>Computes {@code y = W x + b}, where {@code W[size][inDim]} and {@code b[size]}.</li>
 *   <li>Applies optional activation (e.g. "relu", "sigmoid") via {@link Activation}.</li>
 * </ul>
 *
 * <p>If {@code weights} or {@code bias} are {@code null}, the layer will lazily
 * initialize them with zeros for smoke tests. This is convenient for demos, but
 * not intended for real training pipelines.</p>
 */
public class DenseLayer extends Layer {

    private static final Logger log = LoggerFactory.getLogger(DenseLayer.class);

    /** Number of output units. */
    private final int size;

    /** Weight matrix of shape [size][inputDim]. */
    @Getter
    private double[][] weights;

    /** Bias vector of length [size]. */
    private double[] bias;

    /** Optional activation name (e.g., "relu", "sigmoid", "tanh", "linear"). */
    private final String activation;

    public DenseLayer(String name, int size, String activation) {
        super(name);
        this.size = size;
        this.activation = activation;
    }

    // -------------------------------------------------------------------------
    // Metadata for validators & tooling
    // -------------------------------------------------------------------------

    @Override
    public Map<String, Object> getParams() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("size", size);          // legacy alias
        p.put("units", size);         // preferred key
        p.put("activation", activation);
        return p;
    }

    // -------------------------------------------------------------------------
    // Weight initialization
    // -------------------------------------------------------------------------

    /**
     * Initializes weights from a flat array in row-major order.
     *
     * @param flatWeights flat array of length size * inDim
     * @param inDim       input dimensionality
     */
    public void setWeights(double[] flatWeights, int inDim) {
        weights = new double[size][inDim];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < inDim; j++) {
                weights[i][j] = flatWeights[i * inDim + j];
            }
        }
    }

    public void setBias(double[] bias) {
        this.bias = bias;
    }

    // -------------------------------------------------------------------------
    // Forward pass
    // -------------------------------------------------------------------------

    @Override
    public double[] forward(Map<String, double[]> inputByName) {
        // Concatenate all inputs from connected layers in order
        int inDim = getInputs().stream()
                .mapToInt(l -> inputByName.get(l.getName()).length)
                .sum();

        double[] inputVec = new double[inDim];
        int pos = 0;
        for (Layer in : getInputs()) {
            double[] arr = inputByName.get(in.getName());
            System.arraycopy(arr, 0, inputVec, pos, arr.length);
            pos += arr.length;
        }

        // Lazy init for smoke tests (zero weights and biases)
        if (weights == null) {
            if (log.isWarnEnabled()) {
                log.warn("DenseLayer '{}' has null weights; initializing with zeros for smoke test (size={} inDim={})",
                        getName(), size, inDim);
            }
            setWeights(new double[size * inDim], inDim);
        }
        if (bias == null) {
            bias = new double[size];
        }

        double[] out = new double[size];
        for (int i = 0; i < size; i++) {
            double sum = bias[i];
            for (int j = 0; j < inDim; j++) {
                sum += inputVec[j] * weights[i][j];
            }
            out[i] = sum;
        }

        // Linear / no activation
        if (activation == null || activation.equalsIgnoreCase("linear")) {
            return out;
        }

        // Non-linear activation via shared Activation helper
        return Activation.apply(activation, out);
    }

    // -------------------------------------------------------------------------
    // Training helpers
    // -------------------------------------------------------------------------

    /**
     * Simple SGD-like weight update: W -= lr * (delta ⊗ input), b -= lr * delta.
     *
     * @param input input vector x
     * @param delta gradient dL/dy for this layer's output
     * @param lr    learning rate
     */
    public void updateWeights(double[] input, double[] delta, double lr) {
        if (weights == null || bias == null) {
            // Nothing to update, probably not initialized yet.
            return;
        }
        int inDim = input.length;
        for (int i = 0; i < size; i++) {
            bias[i] -= lr * delta[i];
            for (int j = 0; j < inDim; j++) {
                weights[i][j] -= lr * delta[i] * input[j];
            }
        }
    }

    // -------------------------------------------------------------------------
    // Layer metadata
    // -------------------------------------------------------------------------

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getActivation() {
        return activation;
    }

    @Override
    public Object getShape() {
        // 1-D vector of length `size`; could be upgraded to a formal Shape later
        return null;
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO: (1) Implement a proper Shape for DenseLayer (e.g., [*, size]) and return it in getShape().
    // TODO: (2) Add validation to ensure weights and bias shapes match size and input dimensionality.
    // TODO: (3) Add Xavier/He initializers instead of zero-initialization for real training.
    // TODO: (4) Integrate with an optimizer abstraction (SGD/Adam) instead of manual updateWeights().
    // TODO: (5) Add unit tests for:
    //           - forward() with known weights/bias,
    //           - activation application,
    //           - lazy zero-init path,
    //           - updateWeights() correctness.
    // TODO: (6) Extend forwardUniversal() to support batch inputs (double[][]).
    // TODO: (7) Consider supporting mixed precision (float32/float64).
}
