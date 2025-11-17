package io.github.swampus.alexandra.compiler.extensions;

/**
 * Thrown when the compiler detects a cyclic dependency
 * in a neural or computational graph that must be acyclic.
 *
 * <p>This exception typically indicates an invalid network
 * definition, such as circular connections between layers
 * or modules that prevent topological sorting.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * // Layer A → Layer B → Layer A (cycle)
 * throw new GraphContainsCyclesException("Cycle detected: A → B → A");
 * }</pre>
 *
 * @since 0.9.0
 */
public class GraphContainsCyclesException extends InvalidNetworkException {

    /**
     * Constructs a new {@code GraphContainsCyclesException} with the specified message.
     *
     * @param message human-readable description of the detected cycle (non-null)
     */
    public GraphContainsCyclesException(String message) {
        super(message);
    }
}
