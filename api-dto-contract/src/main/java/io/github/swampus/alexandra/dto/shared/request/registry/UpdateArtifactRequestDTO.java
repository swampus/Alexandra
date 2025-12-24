package io.github.swampus.alexandra.dto.shared.request.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Represents a request to update mutable metadata of an existing artifact.
 *
 * <p>This DTO does not allow modifying the artifact payload itself.
 * Payload updates must be performed via dedicated upload or replace
 * operations.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateArtifactRequestDTO(

        /**
         * Identifier of the artifact to be updated.
         */
        String artifactId,

        /**
         * Updated human-readable name of the artifact.
         */
        String name,

        /**
         * Updated description of the artifact.
         */
        String description,

        /**
         * Updated set of tags.
         */
        List<String> tags

) {}
