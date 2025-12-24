package io.github.swampus.alexandra.dto.shared.request.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a request to retrieve a single artifact by its identifier.
 *
 * <p>The registry may return metadata only or metadata together with
 * the payload depending on the API endpoint and access policy.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GetArtifactByIdRequestDTO(

        /**
         * Identifier of the artifact to retrieve.
         */
        String artifactId

) {}

