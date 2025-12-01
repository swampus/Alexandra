package io.github.swampus.alexandra.dto.shared.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a request to set or update network weights.
 * <p>
 * Can be used to either fully replace existing weights
 * or merge with the current weight storage.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SetWeightsRequestDto(

        /**
         * Weights mapped by logical key (e.g. layer or parameter group name).
         * <p>
         * Each value is a raw array of doubles representing weights.
         */
        Map<String, double[]> weights,

        /**
         * If {@code true}, provided weights will be merged with existing ones.
         * If {@code false}, existing weights will be replaced entirely.
         */
        boolean merge

) {

    /**
     * Canonical constructor with deep defensive copying of arrays.
     */
    public SetWeightsRequestDto {
        weights = copyWeightsOrNull(weights);
    }

    private static Map<String, double[]> copyWeightsOrNull(Map<String, double[]> source) {
        if (source == null) {
            return null;
        }

        Map<String, double[]> copy = new LinkedHashMap<>(source.size());
        for (var entry : source.entrySet()) {
            double[] value = entry.getValue();
            copy.put(entry.getKey(), value == null ? null : value.clone());
        }

        return Map.copyOf(copy);
    }

    /**
     * @return {@code true} if this request contains at least one weight entry.
     */
    public boolean hasWeights() {
        return weights != null && !weights.isEmpty();
    }
}
