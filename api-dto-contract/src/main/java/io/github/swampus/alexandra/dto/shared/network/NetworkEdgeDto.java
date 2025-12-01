package io.github.swampus.alexandra.dto.shared.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Represents a directed or logical edge between two network nodes.
 * <p>
 * Used to describe structural, data-flow or control-flow connections
 * inside compiled Alexandra network models.
 *
 * <p>Design goals:
 * <ul>
 *     <li>Immutable and thread-safe.</li>
 *     <li>Framework-agnostic.</li>
 *     <li>JSON-serialization friendly.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record NetworkEdgeDto(

        /**
         * Source node identifier.
         */
        String fromId,

        /**
         * Target node identifier.
         */
        String toId,

        /**
         * Logical or physical channel name.
         * <p>
         * Examples: "data", "control", "residual", "skip".
         */
        String channel,

        /**
         * Direction of the edge.
         * <p>
         * Typical values:
         * <ul>
         *     <li>"FORWARD"</li>
         *     <li>"BACKWARD"</li>
         *     <li>"BIDIRECTIONAL"</li>
         * </ul>
         */
        String direction,

        /**
         * Type of the connection.
         * <p>
         * Examples:
         * <ul>
         *     <li>"LINEAR"</li>
         *     <li>"CONDITIONAL"</li>
         *     <li>"ATTENTION"</li>
         *     <li>"RESIDUAL"</li>
         * </ul>
         */
        String type,

        /**
         * Free-form metadata attached to this edge.
         * <p>
         * Must only contain JSON-serializable values.
         */
        Map<String, Object> meta
) {

    /**
     * Canonical constructor with defensive copying.
     */
    public NetworkEdgeDto {
        meta = copyOrNull(meta);
    }

    private static <K, V> Map<K, V> copyOrNull(Map<K, V> source) {
        return source == null ? null : Map.copyOf(source);
    }

    /**
     * @return {@code true} if this edge is self-referencing.
     */
    public boolean isSelfLoop() {
        return fromId != null && fromId.equals(toId);
    }
}
