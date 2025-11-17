package io.github.swampus.alexandra.compiler.validator;

/**
 * Validation strictness used after compilation.
 *
 * <p>Levels are cumulative in intent: higher levels include the checks of
 * the lower ones (conceptually), though the actual wiring is up to the caller.</p>
 *
 * @since 0.9.0
 */
public enum ValidationLevel {

    /**
     * No validation. Parsing/assembly only.
     */
    NONE,

    /**
     * Structural checks only: e.g., cycle detection and basic graph integrity.
     */
    STRUCTURAL,

    /**
     * Structural checks plus shape inference and a lightweight dry-run.
     */
    SHAPES
}
