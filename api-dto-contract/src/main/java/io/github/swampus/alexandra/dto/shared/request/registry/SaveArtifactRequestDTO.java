package io.github.swampus.alexandra.dto.shared.request.registry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Represents a request to persist a compiled network artifact in the registry.
 *
 * <p>This DTO describes a fully materialized artifact, including its
 * identifying metadata and binary payload. It is intended to be used
 * by registry-facing APIs after successful compilation or training.</p>
 *
 * <p>The registry treats the payload as an opaque binary blob and does not
 * interpret its internal structure.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SaveArtifactRequestDTO(

        /**
         * Globally unique identifier of the artifact.
         * <p>
         * Must be stable across re-uploads and uniquely identify
         * the artifact within the registry.
         */
        String artifactId,

        String version,

        /**
         * Human-readable name of the network or artifact.
         */
        String name,

        /**
         * Author or owner of the artifact.
         */
        String author,

        /**
         * Optional textual description explaining the purpose of the artifact.
         */
        String description,

        /**
         * Logical cluster identifier used for grouping related artifacts.
         */
        String clusterId,

        /**
         * Names of tasks this artifact is associated with.
         */
        List<String> taskNames,

        /**
         * Arbitrary tags used for search and categorization.
         */
        List<String> tags,

        /**
         * Source language or representation of the artifact
         * (e.g. NureonLang, IR, BinaryModel).
         */
        String language,

        /**
         * Binary payload of the artifact.
         * <p>
         * Typically a serialized NetworkModel, IR snapshot or trained weights.
         */
        byte[] payload

) {}

