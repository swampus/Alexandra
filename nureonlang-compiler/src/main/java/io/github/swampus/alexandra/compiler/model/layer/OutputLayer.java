package io.github.swampus.alexandra.compiler.model.layer;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Terminal layer representing the model's output.
 *
 * <p><b>Behavior:</b> Preserves the original logic:
 * <ul>
 *   <li>Concatenates all input vectors in encounter order.</li>
 *   <li>If {@code size == 1}, returns a single-element vector; applies {@code sigmoid} if declared.</li>
 *   <li>Otherwise returns the input vector unchanged (identity), regardless of {@code activation}.</li>
 * </ul>
 * No numerical stabilization or additional activations are applied to keep parity with existing tests.
 *
 * @since 0.9.0
 */
// TODO: OutputLayer hardening & extensions
//  1) Add optional handling for common output activations (softmax, tanh) with stable implementations.
//  2) Enforce OutputContract (THIN/THICK) at runtime when mismatch is detected (currently done in validator only).
//  3) Consider deterministic input order: require LinkedHashMap or explicit key order for concatenation.
//  4) Support multi-head/multi-port outputs with named outputs instead of blind concatenation.
//  5) Add unit tests: (a) size==1 + sigmoid; (b) passthrough; (c) mixed multi-input concatenation; (d) shape metadata.
//  6) Consider exposing a typed shape (int[]) instead of generic Object for stronger contracts.
@Getter
@Setter
public class OutputLayer extends Layer {

    private final int size;
    private final String activation;
    private final Object shape;

    /**
     * Declares an output by scalar/vector size and optional activation.
     *
     * @param name        layer name
     * @param size        number of output units (use 1 for scalars)
     * @param activation  optional activation name (e.g., "sigmoid"), case-insensitive
     */
    public OutputLayer(String name, int size, String activation) {
        super(name);
        this.size = size;
        this.activation = activation;
        this.shape = null;
    }

    /**
     * Declares an output by explicit shape (metadata-only).
     *
     * @param name  layer name
     * @param shape arbitrary shape descriptor; kept as-is for validators/metadata
     */
    public OutputLayer(String name, Object shape) {
        super(name);
        this.size = -1;
        this.activation = null;
        this.shape = shape;
    }

    /**
     * Concatenates all incoming vectors and applies minimal post-processing
     * to preserve prior behavior (scalar + optional sigmoid).
     */
    @Override
    public double[] forward(Map<String, double[]> inputByName) {
        double[] inputVec = inputByName.values().stream()
                .flatMapToDouble(Arrays::stream)
                .toArray();

        // Special-case: single-unit output (optionally with sigmoid)
        if (size == 1 && inputVec.length > 0) {
            double[] out = new double[1];
            if ("sigmoid".equalsIgnoreCase(activation)) {
                out[0] = 1.0 / (1.0 + Math.exp(-inputVec[0]));
            } else {
                out[0] = inputVec[0];
            }
            return out;
        }

        // NOTE: Keep behavior: no softmax/tanh/etc. here to avoid changing tests.
        // If desired, extend via the TODO block.

        // Passthrough if size matches (legacy behavior): return as-is.
        return inputVec;
    }

    /**
     * Exposes layer parameters for validators and tooling.
     */
    @Override
    public Map<String, Object> getParams() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("units", size);
        p.put("size", size);
        if (activation != null) {
            p.put("activation", activation);
        }
        p.put("shape", shape);
        return p;
    }
}
