package io.github.swampus.alexandra.compiler.postcompile.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates validation diagnostics (errors and warnings)
 * produced during post-compilation checks.
 *
 * <p>This class is mutable during validation, but the lists it
 * exposes are read-only snapshots. Instances are not thread-safe.</p>
 *
 * <p>Intended usage pattern:</p>
 * <pre>{@code
 * ValidationReport report = new ValidationReport();
 * report.error("Missing output layer");
 * report.warn("Layer 'Dense1' has unused weights");
 *
 * if (!report.ok()) {
 *     throw new InvalidNetworkException(report.toString());
 * }
 * }</pre>
 *
 * @since 0.9.0
 */
public final class ValidationReport {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    /**
     * Records a validation error.
     *
     * @param message human-readable error message (non-null)
     */
    public void error(String message) {
        errors.add(message);
    }

    /**
     * Records a validation warning.
     *
     * @param message human-readable warning message (non-null)
     */
    public void warn(String message) {
        warnings.add(message);
    }

    /**
     * Returns {@code true} if no errors were recorded.
     */
    public boolean ok() {
        return errors.isEmpty();
    }

    /**
     * Returns an immutable view of all recorded errors.
     */
    public List<String> errors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns an immutable view of all recorded warnings.
     */
    public List<String> warnings() {
        return Collections.unmodifiableList(warnings);
    }

    @Override
    public String toString() {
        return "ValidationReport{" +
                "errors=" + errors +
                ", warnings=" + warnings +
                '}';
    }
}
