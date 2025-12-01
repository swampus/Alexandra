package io.github.swampus.alexandra.dto.shared.instruction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Represents a single high-level instruction in the Alexandra pipeline.
 * <p>
 * This DTO is intentionally generic and flexible:
 * it can describe layers, connections, control-flow (IF / FOR / BLOCK),
 * as well as higher-level composite constructs.
 *
 * <p>Design principles:
 * <ul>
 *     <li>Immutable record – safe to share between modules and threads.</li>
 *     <li>Serialization-friendly – only basic Java and JSON types.</li>
 *     <li>No business logic – purely a data container.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record InstructionDto(

        // --- Core operation description ---

        /**
         * Operation code.
         * Examples: "LAYER", "CONNECT", "MACRO", "IF", "FOR", "BLOCK".
         */
        String op,

        /**
         * Instruction type / sub-kind within the operation.
         * Examples: "DENSE", "CONV", "ATTENTION", "TRANSFORMER_BLOCK".
         */
        String type,

        /**
         * Human-readable and unique (within a scope) name of the instruction.
         */
        String name,

        /**
         * Source identifier (e.g. input layer or node name) used for connections.
         */
        String from,

        /**
         * Target identifier (e.g. output layer or node name) used for connections.
         */
        String to,

        /**
         * Explicit input names for this instruction.
         */
        List<String> inputs,

        /**
         * Explicit output names for this instruction.
         */
        List<String> outputs,

        /**
         * Activation function name.
         * Examples: "relu", "tanh", "sigmoid", "gelu".
         */
        String activation,

        /**
         * Shape definition.
         * <p>
         * Expected to be either:
         * <ul>
         *     <li>List&lt;Integer&gt; – e.g. [128, 256]</li>
         *     <li>Map&lt;String, Object&gt; – for structured shapes</li>
         *     <li>String – for simple symbolic definitions</li>
         * </ul>
         * Kept as Object to remain flexible across compiler/executor modules.
         */
        Object shape,

        /**
         * Generic size parameter.
         * Examples: layer width, embedding size, etc.
         */
        Integer size,

        /**
         * Expression used for computed values (e.g. custom formulas).
         * Can be parsed by expression engines in compiler/executor modules.
         */
        String expr,

        /**
         * Dimension index (e.g. for operations along a specific axis).
         */
        Integer dim,

        /**
         * Depth of the structure.
         * Examples: number of stacked layers / blocks.
         */
        Integer depth,

        /**
         * Whether this instruction uses attention mechanisms.
         */
        Boolean attention,

        /**
         * Dropout probability in range [0.0, 1.0].
         */
        Double dropout,

        /**
         * Number of attention heads (for multi-head attention).
         */
        Integer heads,

        /**
         * Logical group identifier for this instruction.
         * Can be used to group layers/blocks into higher-level modules.
         */
        String group,

        /**
         * Logical space or namespace this instruction belongs to.
         */
        String space,

        /**
         * Free-form parameters container.
         * <p>
         * Keys are expected to be stable, documented names.
         * Values are primitive types, lists or nested maps.
         */
        Map<String, Object> params,

        /**
         * Body of composite instructions.
         * <p>
         * Used for constructs like BLOCK, MACRO, IF, FOR, etc.
         */
        List<InstructionDto> body,

        // --- Control-flow related fields (FOR / IF / etc.) ---

        /**
         * Loop variable name for FOR-like instructions.
         */
        String var,

        /**
         * Lower bound (inclusive) for range-based constructs.
         * Expected to be Integer, Long or Double in practice.
         */
        Object fromVal,

        /**
         * Upper bound (exclusive or inclusive – depends on semantics)
         * for range-based constructs.
         */
        Object toVal,

        /**
         * Condition instruction or expression container for IF-like constructs.
         * <p>
         * Nested InstructionDto is used instead of a raw string to keep
         * the representation uniform and composable.
         */
        InstructionDto cond,

        // --- Misc metadata ---

        /**
         * Path to the underlying resource, script or definition file, if any.
         */
        String path,

        /**
         * User-defined tags for classification and filtering.
         */
        List<String> tags,

        /**
         * Free-form meta information.
         * <p>
         * Reserved for cross-cutting concerns such as tracing, debugging,
         * origin markers, etc.
         */
        Map<String, Object> meta,

        /**
         * Target identifier for this instruction.
         * Can be used for routing or addressing external components.
         */
        String target,

        /**
         * Optional weights attached directly to the instruction.
         * <p>
         * Intended only for lightweight or prototype cases.
         * For real models, weights should be referenced via storage keys.
         */
        List<Double> weights
) {

    /**
     * Canonical constructor with basic normalization.
     * <p>
     * Ensures collections are non-null and unmodifiable,
     * while preserving {@code null} for truly optional fields.
     */
    public InstructionDto {
        inputs = copyOrNull(inputs);
        outputs = copyOrNull(outputs);
        tags = copyOrNull(tags);
        body = copyOrNull(body);
        params = copyOrNull(params);
        meta = copyOrNull(meta);
        weights = copyOrNull(weights);
    }

    private static <T> List<T> copyOrNull(List<T> source) {
        return source == null ? null : List.copyOf(source);
    }

    private static <K, V> Map<K, V> copyOrNull(Map<K, V> source) {
        return source == null ? null : Map.copyOf(source);
    }

    /**
     * @return {@code true} if this instruction contains nested instructions.
     */
    public boolean isComposite() {
        return body != null && !body.isEmpty();
    }

    /**
     * @return {@code true} if this instruction has an associated condition.
     */
    public boolean hasCondition() {
        return cond != null;
    }
}
