package io.github.swampus.alexandra.dto.shared.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a compiled or runtime neural network instance in Alexandra.
 * <p>
 * This DTO is intentionally high-level and shared between multiple modules:
 * compiler, trainer, executor, catalog, etc.
 *
 * <p>Design goals:
 * <ul>
 *     <li>Immutable and thread-safe.</li>
 *     <li>JSON-serialization friendly.</li>
 *     <li>No framework or infrastructure dependencies.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record NNetworkDto(

        /**
         * Unique network identifier within the system.
         */
        String id,

        /**
         * Human-readable network name.
         */
        String name,

        /**
         * Internal IR representation of the network.
         * <p>
         * This can contain a serialized graph, instruction list or
         * another intermediate form used by compiler / executor.
         */
        String ir,

        /**
         * Weights associated with this network, grouped by key.
         * <p>
         * Key is a logical identifier (e.g. layer name).
         * Value is an array of weights for that group.
         * <p>
         * For heavy models, this is expected to be null and
         * weights should be referenced via external storage keys.
         */
        Map<String, double[]> weights,

        /**
         * Current lifecycle state of the network.
         * <p>
         * Examples: "REGISTERED", "COMPILED", "TRAINED", "ARCHIVED".
         */
        String state,

        /**
         * Structural connections between nodes inside this network.
         */
        List<NetworkEdgeDto> edges,

        /**
         * Nested sub-networks composed into this network.
         * <p>
         * Key is a logical name or alias.
         * This allows hierarchical / modular network definitions.
         */
        Map<String, NNetworkDto> subNetworks,

        /**
         * Tasks associated with this network.
         * <p>
         * Typical usage: training tasks, evaluation tasks,
         * scheduled jobs, etc.
         */
        List<TaskDto> tasks,

        /**
         * Tags for classification, search and filtering.
         * <p>
         * Examples: "nlp", "vision", "experimental", "production".
         */
        List<String> tags,

        /**
         * Free-form metadata container.
         * <p>
         * Reserved for cross-cutting concerns such as tracing,
         * version info, origin markers, etc.
         */
        Map<String, Object> meta,

        /**
         * Original NureonLang source code used to define this network.
         * <p>
         * Kept for traceability, debugging and regeneration.
         *
         * NOTE: JSON property name is preserved as "nueronLangSource"
         * for backward compatibility with older payloads.
         */
        @JsonProperty("nueronLangSource")
        String nureonLangSource,

        /**
         * Network-local memory or state snapshot.
         * <p>
         * Can be used by higher-level orchestration logic to store
         * lightweight runtime information.
         */
        Map<String, Object> memory
) {

    /**
     * Canonical constructor with defensive copying of collections and arrays.
     */
    public NNetworkDto {
        edges = copyOrNull(edges);
        subNetworks = copyOrNull(subNetworks);
        tasks = copyOrNull(tasks);
        tags = copyOrNull(tags);
        meta = copyOrNull(meta);
        memory = copyOrNull(memory);
        weights = copyWeightsOrNull(weights);
    }

    private static <T> List<T> copyOrNull(List<T> source) {
        return source == null ? null : List.copyOf(source);
    }

    private static <K, V> Map<K, V> copyOrNull(Map<K, V> source) {
        return source == null ? null : Map.copyOf(source);
    }

    private static Map<String, double[]> copyWeightsOrNull(Map<String, double[]> source) {
        if (source == null) {
            return null;
        }
        Map<String, double[]> copy = new LinkedHashMap<>(source.size());
        for (Map.Entry<String, double[]> entry : source.entrySet()) {
            double[] value = entry.getValue();
            copy.put(entry.getKey(), value == null ? null : value.clone());
        }
        return Map.copyOf(copy);
    }

    /**
     * @return {@code true} if this network contains at least one sub-network.
     */
    public boolean isComposite() {
        return subNetworks != null && !subNetworks.isEmpty();
    }

    /**
     * @return {@code true} if this network has any attached tasks.
     */
    public boolean hasTasks() {
        return tasks != null && !tasks.isEmpty();
    }

    /**
     * @return {@code true} if this DTO carries in-memory weights.
     */
    public boolean hasInMemoryWeights() {
        return weights != null && !weights.isEmpty();
    }
}
