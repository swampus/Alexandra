package io.github.swampus.alexandra.compiler.validator.spi;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface providing metadata access and structural introspection
 * for network layers within a compiled {@link NetworkModel}.
 *
 * <p>This abstraction serves as a bridge between the compiler's internal
 * representation and validators, allowing shape and parameter inference
 * without hard-coding model-specific details.</p>
 *
 * <p>Implementations must be deterministic and side-effect free.</p>
 *
 * @since 0.9.0
 */
public interface LayerIntrospector {

    /**
     * Returns a full, ordered list of all layers within the given network model.
     *
     * @param model the network model (non-null)
     * @return list of layers, possibly empty but never {@code null}
     */
    List<Layer> layers(NetworkModel model);

    /**
     * Returns the canonical name of the given layer.
     * <p>This method provides the "source of truth" for node identifiers
     * used throughout the compiler and validators.</p>
     *
     * @param layer the layer (non-null)
     * @return unique layer name (non-null)
     */
    String name(Layer layer);

    /**
     * Returns the kind/type of the given layer, used for validation rules
     * and diagnostic messages (e.g. {@code "Dense"}, {@code "Conv2D"},
     * {@code "Reshape"}, {@code "Input"}).
     *
     * @param layer the layer (non-null)
     * @return type string (never {@code null}, but may be empty)
     */
    String kind(Layer layer);

    /**
     * Returns the parameter map associated with the layer, containing items such as
     * {@code units}, {@code shape}, {@code filters}, {@code kernel_size}, etc.
     *
     * <p>Implementations may return an empty map but must never return {@code null}.</p>
     *
     * @param layer the layer (non-null)
     * @return unmodifiable parameter map (non-null)
     */
    Map<String, Object> params(Layer layer);

    /**
     * If the layer declares an explicit intrinsic output shape (e.g. {@code Input},
     * {@code Const}, or {@code Reshape}), returns that shape.
     *
     * <p>For all other layer types, returns {@link Optional#empty()}.</p>
     *
     * @param layer  the layer (non-null)
     * @param params parameters previously retrieved via {@link #params(Layer)} (non-null)
     * @return optional explicit output shape
     */
    Optional<int[]> intrinsicOutputShape(Layer layer, Map<String, Object> params);
}
