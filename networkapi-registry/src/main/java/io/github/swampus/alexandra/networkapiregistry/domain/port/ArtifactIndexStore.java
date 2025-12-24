package io.github.swampus.alexandra.networkapiregistry.domain.port;

import io.github.swampus.alexandra.networkapiregistry.domain.model.ArtifactMetadata;

import java.util.List;
import java.util.Optional;

/**
 * Index abstraction for searchable artifact metadata.
 *
 * <p>This port defines all lookup operations supported by the registry.
 * Implementations may use in-memory maps, databases, search engines, etc.</p>
 *
 * <p>The index operates exclusively on {@link ArtifactMetadata}
 * and never inspects artifact payloads.</p>
 */
public interface ArtifactIndexStore {

    /**
     * Inserts or updates metadata entry in the index.
     *
     * @param metadata artifact metadata to index
     */
    void upsert(ArtifactMetadata metadata);

    /**
     * Retrieves artifact metadata by its unique identifier.
     *
     * @param artifactId artifact identifier
     * @return metadata if present
     */
    Optional<ArtifactMetadata> findById(String artifactId);

    /**
     * Finds all artifacts belonging to the given cluster.
     *
     * @param clusterId logical cluster identifier
     * @return matching artifact metadata
     */
    List<ArtifactMetadata> findByClusterId(String clusterId);

    /**
     * Finds all artifacts associated with the given task identifier.
     *
     * @param taskId logical task identifier
     * @return matching artifact metadata
     */
    List<ArtifactMetadata> findByTaskId(String taskId);

    /**
     * Finds all artifacts matching the given version.
     *
     * @param version artifact version
     * @return matching artifact metadata
     */
    List<ArtifactMetadata> findByVersion(String version);

    /**
     * Finds all artifacts stored in the given source language or representation.
     *
     * @param language artifact language (e.g. "NureonLang", "IR")
     * @return matching artifact metadata
     */
    List<ArtifactMetadata> findByLanguage(String language);

    /**
     * Removes artifact metadata from the index.
     *
     * @param artifactId artifact identifier
     */
    void delete(String artifactId);
}
