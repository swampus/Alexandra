package io.github.swampus.alexandra.networkapiregistry.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable, index-oriented metadata describing a stored network artifact.
 *
 * <p>This class represents the <b>searchable projection</b> of an artifact
 * and is intentionally decoupled from the artifact payload itself.</p>
 *
 * <p><b>Design notes:</b>
 * <ul>
 *     <li>This class is immutable and thread-safe.</li>
 *     <li>It is used exclusively for indexing and search.</li>
 *     <li>It must not contain heavy objects or runtime-specific structures.</li>
 *     <li>All fields are expected to be pre-normalized by higher layers.</li>
 * </ul>
 *
 * <p>The registry treats this metadata as authoritative for search operations
 * and never inspects the artifact payload.</p>
 */
@Getter
@EqualsAndHashCode
@ToString
public final class ArtifactMetadata {

    /**
     * Globally unique identifier of the artifact.
     * <p>
     * Acts as the primary lookup key within the registry.
     */
    private final String artifactId;

    /**
     * Logical cluster identifier used for grouping related artifacts.
     */
    private final String clusterId;

    /**
     * Logical task identifier associated with this artifact.
     * <p>
     * This value is expected to be extracted from higher-level domain objects
     * (e.g. DTOs) prior to persistence.
     */
    private final String taskId;

    /**
     * Logical version identifier of the artifact.
     * <p>
     * Used for traceability and evolution tracking.
     */
    private final String version;

    /**
     * Source language or representation of the artifact
     * (e.g. "NureonLang", "IR", "RuntimeModel").
     */
    private final String language;

    /**
     * Timestamp indicating when this artifact version was created.
     */
    private final Instant createdAt;

    /**
     * Creates a new immutable {@link ArtifactMetadata} instance.
     *
     * @param artifactId globally unique artifact identifier
     * @param clusterId logical cluster identifier
     * @param taskId logical task identifier
     * @param version artifact version
     * @param language artifact source representation
     * @param createdAt creation timestamp
     */
    public ArtifactMetadata(
            String artifactId,
            String clusterId,
            String taskId,
            String version,
            String language,
            Instant createdAt
    ) {
        this.artifactId = requireNonBlank(artifactId, "artifactId");
        this.clusterId = requireNonBlank(clusterId, "clusterId");
        this.taskId = requireNonBlank(taskId, "taskId");
        this.version = requireNonBlank(version, "version");
        this.language = requireNonBlank(language, "language");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must be non-blank");
        }
        return value;
    }
}
