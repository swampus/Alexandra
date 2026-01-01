package io.github.swampus.alexandra.networkapi.weight.infrastructure.spec;


import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.port.ShapeSpecProviderPort;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides parameter shapes for {@link ModelWithMeta}.
 *
 * <p>Conservative v1 strategy: expects a precomputed spec placed into
 * {@code model.meta["weights.shapeSpec"]} by the compiler or orchestrator.</p>
 */
public final class NetworkModelShapeSpecProvider implements ShapeSpecProviderPort<ModelWithMeta> {

    public static final String META_SHAPE_SPEC = "weights.shapeSpec"; // Map<String,int[]>

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, int[]> paramShapes(ModelWithMeta model) {
        Objects.requireNonNull(model, "model");
        Object spec = model.meta().get(META_SHAPE_SPEC);
        if (spec == null) return Map.of();

        Map<String, int[]> raw = (Map<String, int[]>) spec;
        Map<String, int[]> copy = new LinkedHashMap<>(raw.size());
        raw.forEach((k, v) -> copy.put(k, v == null ? null : v.clone()));
        return Map.copyOf(copy);
    }
}
