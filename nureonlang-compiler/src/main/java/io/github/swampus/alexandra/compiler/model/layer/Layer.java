package io.github.swampus.alexandra.compiler.model.layer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base abstraction for all computational nodes in the network graph.
 *
 * <p>A {@code Layer} has:
 * <ul>
 *   <li>a unique {@link #name},</li>
 *   <li>zero or more input layers (upstream graph edges),</li>
 *   <li>zero or more output layers (downstream graph edges).</li>
 * </ul>
 *
 * <p>Subclasses are expected to implement:
 * <ul>
 *   <li>{@link #forward(Map)} — numeric forward pass (double[]-based),</li>
 *   <li>{@link #getSize()}, {@link #getActivation()}, {@link #getShape()} — metadata for validators/tools.</li>
 * </ul>
 *
 * <p>Note: this class is <b>not</b> thread-safe. It is designed for single-graph,
 * single-threaded compilation/execution scenarios.</p>
 */
// TODO: Layer – core abstraction hardening
//  1) Decide how to cache forward() results to avoid recomputing upstream layers on each resolveInput() call.
//  2) Add a proper execution context (e.g., EvaluationContext) instead of passing raw Map<String,double[]>.
//  3) Clarify and possibly separate "graph connectivity" from "execution API" (inputs/outputs vs. forward()).
//  4) Introduce a strongly-typed Shape abstraction instead of generic Object in getShape().
//  5) Extend forwardUniversal(...) to support mixed tensor types (double[][], int[], etc.).
//  6) Add defensive checks for cycles to prevent infinite recursion in resolveInput() (or rely on validators).
//  7) Add logging hooks / tracing IDs for debugging execution flows.
//  8) Provide unit tests for:
//       - resolveInput() with direct input map,
//       - resolveInput() with upstream layers only,
//       - concatInputs() with multiple inputs,
//       - forwardUniversal() fallback behavior.
//  9) Consider making inputs/outputs immutable from outside once the graph is compiled.
// 10) Evaluate whether Layer should expose a generic parameter map to reduce reflection in introspectors.
public abstract class Layer {

    protected final String name;
    protected final List<Layer> inputs = new ArrayList<>();
    protected final List<Layer> outputs = new ArrayList<>();

    protected Layer(String name) {
        this.name = name;
    }

    /**
     * Returns the logical name of this layer (unique within a model).
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of upstream layers feeding into this layer.
     */
    public List<Layer> getInputs() {
        return inputs;
    }

    /**
     * Returns the list of downstream layers that consume this layer's output.
     */
    public List<Layer> getOutputs() {
        return outputs;
    }

    /**
     * Adds an upstream layer to this layer's input list.
     */
    public void addInput(Layer layer) {
        inputs.add(layer);
    }

    /**
     * Adds a downstream layer to this layer's output list.
     */
    public void addOutput(Layer layer) {
        outputs.add(layer);
    }

    /**
     * Performs the numeric forward pass for this layer.
     *
     * <p>Subclasses MUST override this method. The default implementation always
     * throws {@link UnsupportedOperationException}.</p>
     *
     * @param inputByName map of named inputs (e.g., input layers or external feeds)
     * @return output vector for this layer
     */
    public double[] forward(Map<String, double[]> inputByName) {
        throw new UnsupportedOperationException("forward() not implemented for " + getClass().getSimpleName());
    }

    /**
     * Resolves input for this layer, either directly from the provided map
     * or by recursively calling {@link #forward(Map)} on upstream layers.
     *
     * <p>Resolution order:</p>
     * <ol>
     *   <li>Attempt to fetch a direct vector from {@code inputByName.get(getName())}.</li>
     *   <li>If absent, recursively evaluate all {@link #getInputs()} and concatenate results.</li>
     *   <li>If no inputs and no direct entry found, throw {@link IllegalArgumentException}.</li>
     * </ol>
     */
    public double[] resolveInput(Map<String, double[]> inputByName) {
        // Direct input by this layer's name
        double[] directInput = (inputByName != null) ? inputByName.get(getName()) : null;
        if (directInput != null) {
            return directInput;
        }

        // Otherwise, aggregate inputs from upstream layers
        if (getInputs() != null && !getInputs().isEmpty()) {
            List<double[]> all = new ArrayList<>();
            for (Layer in : getInputs()) {
                all.add(in.forward(inputByName));
            }
            return concatInputs(all);
        }

        throw new IllegalArgumentException(
                "Input not found for " + getClass().getSimpleName() + ": " + getName());
    }

    /**
     * Concatenates a list of input vectors into a single flat vector.
     */
    private double[] concatInputs(List<double[]> inputs) {
        int total = inputs.stream().mapToInt(arr -> arr.length).sum();
        double[] out = new double[total];
        int pos = 0;
        for (double[] arr : inputs) {
            System.arraycopy(arr, 0, out, pos, arr.length);
            pos += arr.length;
        }
        return out;
    }

    /**
     * Generic forward pass accepting arbitrary objects.
     *
     * <p>Default implementation supports the common case where all values in
     * {@code inputByName} are {@code double[]} and delegates to {@link #forward(Map)}.
     * Subclasses that need richer tensor types (e.g., {@code double[][]}) should
     * override this method.</p>
     *
     * @param inputByName map of named inputs with arbitrary payload types
     * @return output in a type chosen by the subclass (usually double[] or double[][])
     */
    public Object forwardUniversal(Map<String, Object> inputByName) {
        if (inputByName != null && inputByName.values().stream().allMatch(v -> v instanceof double[])) {
            @SuppressWarnings("unchecked")
            Map<String, double[]> doubleMap = (Map<String, double[]>) (Map<?, ?>) inputByName;
            return forward(doubleMap);
        }
        throw new UnsupportedOperationException("forwardUniversal() not implemented for " + getClass().getSimpleName());
    }

    /**
     * Returns a parameter map describing this layer for validators and tooling.
     *
     * <p>Default implementation returns an empty map; subclasses may override.</p>
     */
    public Map<String, Object> getParams() {
        return Map.of();
    }

    /**
     * Returns the logical output size (e.g., number of units in a Dense layer).
     */
    public abstract int getSize();

    /**
     * Returns the activation name (if any) associated with the layer.
     */
    public abstract String getActivation();

    /**
     * Returns shape metadata for this layer, or {@code null} if not specified.
     * The concrete format is layer-specific (e.g., int[], List&lt;Integer&gt;, or symbolic).
     */
    public abstract Object getShape();
}
