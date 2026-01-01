package io.github.swampus.alexandra.networkapi.weight.domain.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable weights container (flat map).
 *
 * <p>Keys are logical identifiers (e.g., layer name or parameter group name).
 * Values are raw arrays of doubles representing serialized tensors.</p>
 */
public final class Weights {

    private final Map<String, double[]> flat;

    public Weights(Map<String, double[]> flat) {
        Objects.requireNonNull(flat, "flat");
        this.flat = deepCopy(flat);
    }

    public Map<String, double[]> flat() {
        return flat;
    }

    /** Returns a new Weights instance where {@code patch} entries replace existing ones. */
    public Weights mergedWith(Map<String, double[]> patch) {
        Objects.requireNonNull(patch, "patch");
        Map<String, double[]> merged = new LinkedHashMap<>(this.flat);
        for (var e : patch.entrySet()) {
            merged.put(e.getKey(), e.getValue() == null ? null : e.getValue().clone());
        }
        return new Weights(merged);
    }

    private static Map<String, double[]> deepCopy(Map<String, double[]> source) {
        Map<String, double[]> copy = new LinkedHashMap<>(source.size());
        for (var e : source.entrySet()) {
            copy.put(e.getKey(), e.getValue() == null ? null : e.getValue().clone());
        }
        return Map.copyOf(copy);
    }
}
