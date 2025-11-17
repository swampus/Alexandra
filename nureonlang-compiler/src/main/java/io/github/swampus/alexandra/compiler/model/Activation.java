package io.github.swampus.alexandra.compiler.model;

/**
 * Utility class providing common activation functions for neural computations.
 *
 * <p>All methods are pure and side-effect free. Array-based methods allocate
 * a new array of the same length and never modify the input array.</p>
 *
 * <p>Supported activations include:
 * <ul>
 *   <li>{@code relu}</li>
 *   <li>{@code sigmoid}</li>
 *   <li>{@code tanh}</li>
 *   <li>{@code leaky_relu}</li>
 *   <li>{@code elu}</li>
 * </ul>
 * Unknown activation types fall back to identity (no-op).</p>
 *
 * @since 0.9.0
 */
public final class Activation {

    private Activation() {
        // Utility class — prevent instantiation.
    }

    /**
     * Applies the specified activation element-wise to the given array.
     *
     * @param type  activation name (case-insensitive, may be {@code null})
     * @param input input vector (non-null)
     * @return new array with activation applied; original array unchanged
     */
    public static double[] apply(String type, double[] input) {
        if (type == null) return input;
        String act = type.toLowerCase();
        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = apply(act, input[i]);
        }
        return out;
    }

    /**
     * Applies a single activation function to a scalar value.
     *
     * @param type activation name (case-insensitive, non-null)
     * @param x    input value
     * @return activated value
     */
    public static double apply(String type, double x) {
        switch (type.toLowerCase()) {
            case "relu":
                return Math.max(0, x);
            case "sigmoid":
                return 1.0 / (1.0 + Math.exp(-x));
            case "tanh":
                return Math.tanh(x);
            case "leaky_relu":
                return x > 0 ? x : 0.01 * x;
            case "elu":
                // α = 1 → exp(x) - 1 for negative x
                return x > 0 ? x : Math.expm1(x);
            default:
                return x; // identity fallback
        }
    }

    // --- Individual variants for convenience and micro-optimizations ---

    /** Element-wise ReLU. */
    public static double[] relu(double[] input) {
        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = Math.max(0, input[i]);
        }
        return out;
    }

    /** Element-wise Sigmoid. */
    public static double[] sigmoid(double[] input) {
        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = 1.0 / (1.0 + Math.exp(-input[i]));
        }
        return out;
    }

    /** Element-wise Tanh. */
    public static double[] tanh(double[] input) {
        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = Math.tanh(input[i]);
        }
        return out;
    }

    /** Element-wise Leaky ReLU (α = 0.01). */
    public static double[] leakyRelu(double[] input) {
        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = input[i] > 0 ? input[i] : 0.01 * input[i];
        }
        return out;
    }

    /** Element-wise ELU (α = 1). */
    public static double[] elu(double[] input) {
        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            out[i] = input[i] > 0 ? input[i] : Math.expm1(input[i]);
        }
        return out;
    }
}
