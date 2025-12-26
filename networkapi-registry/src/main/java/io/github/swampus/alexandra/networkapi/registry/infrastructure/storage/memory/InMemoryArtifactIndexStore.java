package io.github.swampus.alexandra.networkapi.registry.infrastructure.storage.memory;

import io.github.swampus.alexandra.networkapi.registry.domain.port.ArtifactIndexStore;
import io.github.swampus.alexandra.networkapi.registry.domain.model.ArtifactMetadata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InMemoryArtifactIndexStore implements ArtifactIndexStore {

    private final Map<String, ArtifactMetadata> byId = new ConcurrentHashMap<>();

    @Override
    public void upsert(ArtifactMetadata metadata) {
        byId.put(metadata.getArtifactId(), metadata);
    }

    @Override
    public Optional<ArtifactMetadata> findById(String artifactId) {
        return Optional.ofNullable(byId.get(artifactId));
    }

    @Override
    public List<ArtifactMetadata> findByClusterId(String clusterId) {
        return filterAndSort(m -> m.getClusterId().equals(clusterId));
    }

    @Override
    public List<ArtifactMetadata> findByTaskId(String taskId) {
        return filterAndSort(m -> m.getTaskId().equals(taskId));
    }

    @Override
    public List<ArtifactMetadata> findByVersion(String version) {
        return filterAndSort(m -> m.getVersion().equals(version));
    }

    @Override
    public List<ArtifactMetadata> findByLanguage(String language) {
        return filterAndSort(m -> m.getLanguage().equals(language));
    }

    @Override
    public void delete(String artifactId) {
        byId.remove(artifactId);
    }

    private List<ArtifactMetadata> filterAndSort(java.util.function.Predicate<ArtifactMetadata> predicate) {
        return byId.values().stream()
                .filter(predicate)
                .sorted(Comparator.comparing(ArtifactMetadata::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
}
