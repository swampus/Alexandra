package io.github.swampus.alexandra.dto.shared.weights;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable DTO for transferring network weights between services.
 * <p>
 * This DTO is part of the shared module and can be safely used by
 * oracle, trainer, executor and storage services.
 *
 * <p>Design goals:
 * <ul>
 *     <li>Immutable and thread-safe.</li>
 *     <li>Deep defensive copying of weight arrays.</li>
 *     <li>Framework-agnostic.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeightsDto(

        /**
         * Target network identifier.
         */
        String networkId,

        /**
         * Weights mapped by logical key (e.g. layer or parameter group name).
         * <p>
         * Each value is a raw array of doubles.
         */
        Map<String, double[]> weights

) {

    /**
     * Canonical constructor with deep defensive copying of weight arrays.
     */
    public WeightsDto {
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
     * @return {@code true} if this DTO contains at least one weight group.
     */
    public boolean hasWeights() {
        return weights != null && !weights.isEmpty();
    }
}
