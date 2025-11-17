package io.github.swampus.alexandra.compiler.validator.spi.impl;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.compiler.validator.spi.LayerIntrospector;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Default reflection-based implementation of {@link LayerIntrospector}.
 *
 * <p>This implementation uses Java reflection to extract {@code getName()},
 * {@code getType()}, and {@code getParams()} from layer instances. It is
 * intended as a generic fallback for heterogeneous layer hierarchies where
 * no static interface contract is guaranteed.</p>
 *
 * <p>Implementations should prefer direct accessors if available for performance.</p>
 *
 * @since 0.9.0
 */
public final class DefaultLayerIntrospector implements LayerIntrospector {

    /**
     * Returns all layers from the given model using {@link NetworkModel#getAllLayers()}.
     */
    @Override
    public List<Layer> layers(NetworkModel model) {
        return model.getAllLayers();
    }

    /**
     * Reflectively resolves the name of the layer using its {@code getName()} method.
     *
     * @throws IllegalStateException if the layer does not provide a {@code getName()} method
     */
    @Override
    public String name(Layer layer) {
        try {
            Method method = layer.getClass().getMethod("getName");
            return String.valueOf(method.invoke(layer));
        } catch (Exception e) {
            throw new IllegalStateException("Layer must declare a getName() method", e);
        }
    }

    /**
     * Returns the kind/type of the layer via its {@code getType()} method if present,
     * otherwise falls back to the simple class name.
     */
    @Override
    public String kind(Layer layer) {
        try {
            Method method = layer.getClass().getMethod("getType");
            return String.valueOf(method.invoke(layer));
        } catch (Exception ignore) {
            return layer.getClass().getSimpleName();
        }
    }

    /**
     * Attempts to retrieve the layer parameter map using {@code getParams()}.
     * If not available or not a map, returns an immutable empty map.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> params(Layer layer) {
        try {
            Method method = layer.getClass().getMethod("getParams");
            Object value = method.invoke(layer);
            if (value instanceof Map<?, ?> raw) {
                Map<String, Object> out = new LinkedHashMap<>();
                raw.forEach((k, v) -> out.put(String.valueOf(k), v));
                return out;
            }
        } catch (Exception ignore) {
            // Fallback: return empty map
        }
        return Map.of();
    }

    /**
     * Returns an explicit output shape if the layer declares one intrinsically
     * (for example, {@code Input} or {@code Reshape} layers).
     */
    @Override
    public Optional<int[]> intrinsicOutputShape(Layer layer, Map<String, Object> params) {
        String kind = kind(layer).toLowerCase(Locale.ROOT);
        if (kind.contains("input") || kind.contains("reshape")) {
            int[] shape = asShape(params.get("shape"));
            if (shape != null) return Optional.of(shape);
        }
        return Optional.empty();
    }

    // ---------- Internal helpers ----------

    private Integer asInt(Object o) {
        if (o instanceof Integer i) return i;
        if (o instanceof Number n) return n.intValue();
        if (o instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    private int[] asShape(Object o) {
        if (o instanceof int[] array) return array;
        if (o instanceof List<?> list) {
            int[] result = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = Optional.ofNullable(asInt(list.get(i))).orElse(0);
            }
            return result;
        }
        if (o instanceof String s) {
            String[] parts = s.replaceAll("[()\\[\\]\\s]", "").split(",");
            if (parts.length == 1 && parts[0].isEmpty()) return null;
            int[] result = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Integer.parseInt(parts[i]);
            }
            return result;
        }
        return null;
    }
}
