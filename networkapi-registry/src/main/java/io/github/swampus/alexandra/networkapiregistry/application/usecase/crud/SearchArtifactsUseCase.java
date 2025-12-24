package io.github.swampus.alexandra.networkapiregistry.application.usecase.crud;

import io.github.swampus.alexandra.dto.shared.request.registry.SearchArtifactRequestDTO;
import io.github.swampus.alexandra.networkapiregistry.domain.model.ArtifactMetadata;
import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactIndexStore;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Executes artifact search operations against the registry index.
 *
 * <p>This use case interprets search intent provided via
 * {@link SearchArtifactRequestDTO} and delegates actual lookup
 * operations to the underlying {@link ArtifactIndexStore}.</p>
 *
 * <p>The registry index is treated as read-only and opaque.</p>
 */
public final class SearchArtifactsUseCase {

    private final ArtifactIndexStore indexStore;

    public SearchArtifactsUseCase(ArtifactIndexStore indexStore) {
        this.indexStore = Objects.requireNonNull(indexStore, "indexStore");
    }

    /**
     * Executes a search based on provided criteria.
     *
     * @param request search request descriptor
     * @return list of matching artifact metadata entries
     */
    public List<ArtifactMetadata> execute(SearchArtifactRequestDTO request) {
        Objects.requireNonNull(request, "request");

        // Highest priority: exact artifact lookup
        if (request.artifactId() != null) {
            return indexStore.findById(request.artifactId())
                    .map(List::of)
                    .orElseGet(Collections::emptyList);
        }

        // Search by cluster
        if (request.clusterId() != null) {
            return indexStore.findByClusterId(request.clusterId());
        }

        // Search by task
        if (request.taskId() != null) {
            return indexStore.findByTaskId(request.taskId());
        }

        // Optional: search by version
        if (request.version() != null) {
            return indexStore.findByVersion(request.version());
        }

        // Optional: search by language
        if (request.language() != null) {
            return indexStore.findByLanguage(request.language());
        }

        return Collections.emptyList();
    }
}
