package io.github.swampus.alexandra.compiler.contract;

/**
 * Defines how the {@code OUTPUT(...)} operation transforms its input
 * during compilation and lowering into the intermediate representation.
 *
 * <p>Use {@link #THIN} for a passthrough output (no weights or bias)
 * or {@link #THICK} for a weighted output that introduces learnable
 * parameters and changes the dimensionality.</p>
 *
 * @since 0.9.0
 */
public enum OutputContract {

    /**
     * Output without weights or bias.
     * <p>The output shape matches the input exactly —
     * a simple passthrough layer.</p>
     */
    THIN,

    /**
     * Output with weights and bias.
     * <p>The layer applies a learned affine transformation
     * that can change the output dimensionality.</p>
     */
    THICK
}
