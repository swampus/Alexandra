package io.github.swampus.alexandra.dto.shared.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Describes a logical task associated with a network.
 * <p>
 * Tasks are used to describe what the network is expected to do,
 * provide examples and additional metadata for selection, training
 * and evaluation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskDto(

        /**
         * Human-readable task name.
         * Examples: "Sentiment Analysis", "Digit Classification".
         */
        String name,

        /**
         * Free-text description of the task in natural language.
         */
        String description,

        /**
         * Tags used for classification and filtering.
         * Examples: "nlp", "vision", "experimental".
         */
        List<String> tags,

        /**
         * Optional formal description of the task.
         * <p>
         * Can contain a reference to a specification language,
         * constraints or a more structured definition.
         */
        String formalDescription,

        /**
         * Example inputs for this task.
         * <p>
         * Intended for documentation, UI hints and testing.
         */
        List<String> inputExample,

        /**
         * Example outputs for this task.
         * <p>
         * Must correspond to {@link #inputExample} by index.
         */
        List<String> outputExample,

        /**
         * Free-form metadata for this task.
         * <p>
         * Reserved for cross-cutting concerns such as
         * versioning, origin or additional hints.
         */
        Map<String, Object> meta
) {

    /**
     * Canonical constructor with defensive copying of collections.
     */
    public TaskDto {
        tags = copyOrNull(tags);
        inputExample = copyOrNull(inputExample);
        outputExample = copyOrNull(outputExample);
        meta = copyOrNull(meta);
    }

    private static <T> List<T> copyOrNull(List<T> source) {
        return source == null ? null : List.copyOf(source);
    }

    private static <K, V> Map<K, V> copyOrNull(Map<K, V> source) {
        return source == null ? null : Map.copyOf(source);
    }

    /**
     * @return {@code true} if this task has at least one example pair.
     */
    public boolean hasExamples() {
        return inputExample != null && !inputExample.isEmpty();
    }
}
