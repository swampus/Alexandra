package io.github.swampus.alexandra.compiler.validator.impl;

import io.github.swampus.alexandra.compiler.extensions.GraphContainsCyclesException;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.compiler.validator.NetworkModelValidator;

import java.util.HashSet;
import java.util.Set;

/**
 * Detects cycles in a directed layer graph using DFS with an "on-stack" set.
 *
 * <p><b>Behavior:</b> Identical to the original implementation. The validator
 * traverses all components, throwing {@link GraphContainsCyclesException} at the
 * first detected back-edge.</p>
 *
 * <p>Time complexity: O(V + E). Space complexity: O(V).</p>
 *
 * @since 0.9.0
 */
public final class CycleValidator implements NetworkModelValidator {

    @Override
    public void validate(NetworkModel model) throws GraphContainsCyclesException {
        Set<Layer> visited = new HashSet<>();
        Set<Layer> onStack = new HashSet<>();
        for (Layer layer : model.getAllLayers()) {
            if (dfsHasCycle(layer, visited, onStack)) {
                throw new GraphContainsCyclesException("Network contains a cycle at layer: " + layer.getName());
            }
        }
    }

    /**
     * Depth-first search that reports whether a cycle is reachable from {@code layer}.
     *
     * @return {@code true} if a back-edge (cycle) is found, otherwise {@code false}
     */
    private boolean dfsHasCycle(Layer layer, Set<Layer> visited, Set<Layer> onStack) {
        if (onStack.contains(layer)) return true;     // back-edge → cycle
        if (visited.contains(layer)) return false;    // already explored

        visited.add(layer);
        onStack.add(layer);

        for (Layer out : layer.getOutputs()) {
            if (dfsHasCycle(out, visited, onStack)) return true;
        }

        onStack.remove(layer);
        return false;
    }
}
