package io.github.swampus.alexandra.compiler.exception;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a generic compilation failure during the translation of
 * source code into an intermediate or executable form.
 *
 * <p>This is the base class for all compiler-related runtime exceptions.
 * It preserves the diagnostic trace (e.g., rule names, node paths, or
 * symbol contexts) to assist in debugging and tooling integration.</p>
 *
 * <p>Instances of this class are immutable and thread-safe.</p>
 *
 * @since 0.9.0
 */
public class CompilationException extends RuntimeException {

    /** Ordered diagnostic trace, usually from top-level to deepest node. */
    private final List<String> trace;

    /**
     * Constructs a new {@code CompilationException} with the given message
     * and diagnostic trace.
     *
     * @param message the human-readable error description (non-null)
     * @param trace   the diagnostic context or null if unavailable
     * @throws NullPointerException if {@code message} is null
     */
    public CompilationException(String message, List<String> trace) {
        super(Objects.requireNonNull(message, "message"));
        this.trace = (trace == null) ? List.of() : List.copyOf(trace);
    }

    /**
     * Returns the immutable diagnostic trace associated with this error.
     *
     * @return non-null, possibly empty list of diagnostic entries
     */
    public List<String> getTrace() {
        return trace;
    }

    @Override
    public String toString() {
        return super.toString() +
                (trace.isEmpty() ? "" : " | trace=" + String.join(" → ", trace));
    }
}
