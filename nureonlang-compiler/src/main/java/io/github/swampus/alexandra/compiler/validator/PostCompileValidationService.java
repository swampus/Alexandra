package io.github.swampus.alexandra.compiler.validator;

import io.github.swampus.alexandra.compiler.extensions.InvalidNetworkException;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.validator.impl.CompositeNetworkModelValidator;
import io.github.swampus.alexandra.compiler.validator.impl.CycleValidator;
import io.github.swampus.alexandra.compiler.validator.spi.LayerIntrospector;
import io.github.swampus.alexandra.compiler.validator.spi.ShapeAndDryRunValidator;
import io.github.swampus.alexandra.compiler.validator.spi.impl.DefaultLayerIntrospector;

import java.util.List;
import java.util.Objects;

/**
 * Unified entry point for post-compilation validation in the compiler module.
 *
 * <p>Performs:</p>
 * <ol>
 *   <li><b>Structural graph checks</b> — e.g., cycles or unreachable nodes (via {@link CycleValidator}).</li>
 *   <li><b>Shape inference and dry-run</b> — performs static shape propagation and
 *       verifies that inferred tensor shapes are valid and allocatable.</li>
 * </ol>
 *
 * <p>All operations are pure and do not modify the underlying {@link NetworkModel}.</p>
 *
 * @since 0.9.0
 */
public final class PostCompileValidationService {

    private final NetworkModelValidator structural;   // structural validators (graph integrity)
    private final ShapeAndDryRunValidator shapes;     // shape inference + dry-run validator

    /**
     * Creates a validation service with default configuration:
     * <ul>
     *   <li>Structural: {@link CycleValidator}</li>
     *   <li>Shape inference: {@link ShapeAndDryRunValidator} using {@link DefaultLayerIntrospector}</li>
     *   <li>Max passes: 8</li>
     * </ul>
     */
    public PostCompileValidationService() {
        this(defaultStructural(), defaultShapes());
    }

    /**
     * Creates a validation service with explicit validator dependencies.
     *
     * @param structural structural graph validator (non-null)
     * @param shapes     shape inference validator (non-null)
     */
    public PostCompileValidationService(NetworkModelValidator structural,
                                        ShapeAndDryRunValidator shapes) {
        this.structural = Objects.requireNonNull(structural, "structural");
        this.shapes = Objects.requireNonNull(shapes, "shapes");
    }

    /**
     * Runs all configured validators (structural + shape/dry-run).
     *
     * @param model the network model to validate (non-null)
     * @throws InvalidNetworkException if any validation step fails
     */
    public void validateAll(NetworkModel model) throws InvalidNetworkException {
        structural.validate(model);
        shapes.validate(model);
    }

    /**
     * Runs validation according to the specified {@link ValidationLevel}.
     *
     * <ul>
     *   <li>{@link ValidationLevel#NONE} — skips all checks.</li>
     *   <li>{@link ValidationLevel#STRUCTURAL} — runs only structural validators.</li>
     *   <li>{@link ValidationLevel#SHAPES} — runs structural + shape/dry-run validators.</li>
     * </ul>
     *
     * @param model the network model to validate (non-null)
     * @param level validation level (non-null)
     * @throws InvalidNetworkException if validation fails at the chosen level
     */
    public void validate(NetworkModel model, ValidationLevel level) throws InvalidNetworkException {
        Objects.requireNonNull(level, "level");
        if (level == ValidationLevel.NONE) return;
        structural.validate(model);
        if (level == ValidationLevel.SHAPES) {
            shapes.validate(model);
        }
    }

    // --------- Default factory methods ---------

    private static NetworkModelValidator defaultStructural() {
        return new CompositeNetworkModelValidator(List.of(new CycleValidator()));
    }

    private static ShapeAndDryRunValidator defaultShapes() {
        LayerIntrospector li = new DefaultLayerIntrospector();
        return new ShapeAndDryRunValidator(li, /* maxPasses */ 8);
    }
}
