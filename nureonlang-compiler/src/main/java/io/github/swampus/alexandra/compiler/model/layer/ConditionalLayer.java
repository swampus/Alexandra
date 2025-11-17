package io.github.swampus.alexandra.compiler.model.layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conditional layer that selects between two subgraphs ({@code thenLayer} and
 * {@code elseLayer}) based on a simple expression over input values.
 *
 * <p>The condition expression supports forms like:</p>
 * <ul>
 *   <li>{@code var[0] == 1}</li>
 *   <li>{@code var[3] >= 0.5}</li>
 *   <li>{@code mood >= 0}</li>
 * </ul>
 *
 * <p>Execution model:</p>
 * <ul>
 *   <li>Evaluate {@link #conditionExpr} using the original {@code inputByName} map.</li>
 *   <li>Select {@code thenLayer} or {@code elseLayer} as the branch root.</li>
 *   <li>Run {@link #forwardRecursive(Layer, Map, Map)} to evaluate the chosen branch with memoization.</li>
 * </ul>
 */
public class ConditionalLayer extends Layer {

    private static final Logger log = LoggerFactory.getLogger(ConditionalLayer.class);

    private final String conditionExpr;
    private final Layer thenLayer;
    private final Layer elseLayer;

    public ConditionalLayer(String name, String conditionExpr, Layer thenLayer, Layer elseLayer) {
        super(name);
        this.conditionExpr = conditionExpr;
        this.thenLayer = thenLayer;
        this.elseLayer = elseLayer;
    }

    public String getConditionExpr() {
        return conditionExpr;
    }

    public Layer getThenLayer() {
        return thenLayer;
    }

    public Layer getElseLayer() {
        return elseLayer;
    }

    @Override
    public double[] forward(Map<String, double[]> inputByName) {
        if (log.isDebugEnabled()) {
            log.debug("[ConditionalLayer '{}'] forward, input keys={}", getName(), inputByName.keySet());
        }

        boolean cond = evalCondition(inputByName);
        Layer root = cond ? thenLayer : elseLayer;

        if (root == null) {
            // No branch defined — fall back to direct input (if present)
            return inputByName.get(getName());
        }

        Map<String, double[]> cache = new HashMap<>();
        return forwardRecursive(root, cache, inputByName);
    }

    /**
     * Recursively evaluates the subgraph rooted at {@code layer}, caching
     * intermediate results by layer name.
     */
    private double[] forwardRecursive(Layer layer,
                                      Map<String, double[]> cache,
                                      Map<String, double[]> inputByName) {
        if (cache.containsKey(layer.getName())) {
            return cache.get(layer.getName());
        }

        double[] out;
        if (layer instanceof InputLayer) {
            // Base case: inputs are read from the original input map.
            out = layer.forward(inputByName);
        } else {
            // Evaluate all upstream dependencies first.
            Map<String, double[]> layerInputs = new HashMap<>();
            for (Layer input : layer.getInputs()) {
                double[] in = forwardRecursive(input, cache, inputByName);
                layerInputs.put(input.getName(), in);
            }
            out = layer.forward(layerInputs);
        }

        cache.put(layer.getName(), out);
        return out;
    }

    /**
     * Evaluates {@link #conditionExpr} against the given input map.
     *
     * <p>Supported forms:</p>
     * <ul>
     *   <li>{@code var[index] OP value}, e.g. {@code x[0] == 1}</li>
     *   <li>{@code var OP value}, using index 0, e.g. {@code mood >= 0}</li>
     * </ul>
     */
    public boolean evalCondition(Map<String, double[]> inputByName) {
        String expr = conditionExpr.replaceAll("\\s+", "");

        // Pattern: var[index] OP value  e.g. x[0] >= 1.0
        Pattern p = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\[(\\d+)]([=!<>]+)([-0-9.]+)");
        Matcher m = p.matcher(expr);
        if (m.matches()) {
            String var = m.group(1);
            int idx = Integer.parseInt(m.group(2));
            String op = m.group(3);
            double val = Double.parseDouble(m.group(4));

            double[] vector = inputByName.get(var);
            if (vector == null || idx < 0 || idx >= vector.length) {
                throw new IllegalArgumentException(
                        "Condition evaluation failed: variable '" + var + "' with index " + idx +
                                " is not available in input map or index is out of bounds."
                );
            }

            double actual = vector[idx];
            return compare(actual, op, val);
        }

        // Pattern: var OP value  e.g. x >= 0
        p = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)([=!<>]+)([-0-9.]+)");
        m = p.matcher(expr);
        if (m.matches()) {
            String var = m.group(1);
            String op = m.group(2);
            double val = Double.parseDouble(m.group(3));

            double[] vector = inputByName.get(var);
            if (vector == null || vector.length == 0) {
                throw new IllegalArgumentException(
                        "Condition evaluation failed: variable '" + var + "' is not available or empty."
                );
            }

            double actual = vector[0];
            return compare(actual, op, val);
        }

        throw new UnsupportedOperationException("Unsupported condition expression: " + conditionExpr);
    }

    private boolean compare(double left, String op, double right) {
        switch (op) {
            case "==": return left == right;
            case "!=": return left != right;
            case "<":  return left < right;
            case "<=": return left <= right;
            case ">":  return left > right;
            case ">=": return left >= right;
            default:
                throw new IllegalArgumentException("Unknown operator in condition: " + op);
        }
    }

    @Override
    public int getSize() {
        if (thenLayer != null) {
            return thenLayer.getSize();
        }
        if (elseLayer != null) {
            return elseLayer.getSize();
        }
        return -1;
    }

    @Override
    public String getActivation() {
        // Conditional layer is structural; it does not apply its own activation.
        return null;
    }

    @Override
    public Object getShape() {
        if (thenLayer != null) {
            return thenLayer.getShape();
        }
        if (elseLayer != null) {
            return elseLayer.getShape();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for contributors / students)
    // -------------------------------------------------------------------------

    // TODO: (1) Extend condition language to support logical operators (&&, ||, !).
    // TODO: (2) Add support for multi-variable expressions and more complex syntax.
    // TODO: (3) Extract condition parsing/evaluation into a reusable expression engine.
    // TODO: (4) Integrate with ShapeAndDryRunValidator for branch consistency checks.
    // TODO: (5) Add debug hooks to log which branch was taken, with evaluated value.
    // TODO: (6) Add unit tests for:
    //           - indexed conditions (x[0] == 1),
    //           - scalar conditions (x >= 0),
    //           - missing variable / out-of-bounds index,
    //           - branch shape/size consistency.
    // TODO: (7) Consider merging ConditionalLayer and IfLayer semantics or clarifying their roles.
}
