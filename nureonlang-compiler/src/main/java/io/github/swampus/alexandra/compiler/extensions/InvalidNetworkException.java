package io.github.swampus.alexandra.compiler.extensions;

/**
 * Base class for all exceptions indicating that a network definition
 * or its structural properties are invalid.
 *
 * <p>This exception type represents errors in the logical or
 * topological integrity of a compiled network (for example,
 * invalid connections, cycles, or inconsistent tensor shapes).</p>
 *
 * <p>Subclasses should provide more specific context and
 * may attach additional metadata for diagnostics.</p>
 *
 * @since 0.9.0
 */
public class InvalidNetworkException extends RuntimeException {

    /**
     * Constructs a new {@code InvalidNetworkException} with the specified message.
     *
     * @param message human-readable description of the validation failure (non-null)
     */
    public InvalidNetworkException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code InvalidNetworkException} with the specified
     * message and underlying cause.
     *
     * @param message human-readable description of the validation failure (non-null)
     * @param cause   the underlying cause (may be {@code null})
     */
    public InvalidNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
