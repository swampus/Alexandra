package io.github.swampus.alexandra.dto.shared.request.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Represents a declarative search request for artifacts stored in the registry.
 *
 * <p>This DTO describes <b>what</b> the caller wants to search by,
 * not <b>how</b> the search is performed.</p>
 *
 * <p>No business logic is allowed in this class.
 * Interpretation and execution of search criteria
 * is the responsibility of the corresponding use case.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchArtifactRequestDTO(

        /**
         * Exact artifact identifier.
         * <p>
         * If provided, other search parameters may be ignored.
         */
        String artifactId,

        /**
         * Logical cluster identifier used for grouping artifacts.
         */
        String clusterId,

        /**
         * Logical task identifier associated with artifacts.
         */
        String taskId,

        /**
         * Artifact version identifier.
         */
        String version,

        /**
         * Artifact source language or representation.
         */
        String language,

        /**
         * Optional limit on number of returned results.
         */
        Integer limit,

        /**
         * Optional offset for pagination.
         */
        Integer offset,

        List<String> tags

) {

    /**
     * @return {@code true} if no search criteria were provided.
     */
    public boolean isEmpty() {
        return artifactId == null
                && clusterId == null
                && taskId == null
                && version == null
                && language == null;
    }
}
