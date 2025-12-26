package io.github.swampus.alexandra.networkapi.registry.infrastructure.storage.fs;

import io.github.swampus.alexandra.networkapi.registry.domain.model.ArtifactMetadata;
import io.github.swampus.alexandra.networkapi.registry.domain.port.ArtifactIndexStore;
import io.github.swampus.alexandra.networkapi.registry.infrastructure.serde.JsonSerde;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Simple file-based index.
 * Stores each metadata entry as JSON file: <artifactId>.meta.json
 *
 * Fast enough for dev. For prod use DB/Dynamo/etc.
 */
public final class FileSystemArtifactIndexStore implements ArtifactIndexStore {

    private final Path root;
    private final Map<String, ArtifactMetadata> cache = new ConcurrentHashMap<>();
    private volatile boolean loaded = false;

    public FileSystemArtifactIndexStore(Path root) {
        this.root = root;
    }

    @Override
    public void upsert(ArtifactMetadata metadata) {
        ensureLoaded();
        cache.put(metadata.getArtifactId(), metadata);
        persist(metadata);
    }

    @Override
    public Optional<ArtifactMetadata> findById(String artifactId) {
        ensureLoaded();
        return Optional.ofNullable(cache.get(artifactId));
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
        ensureLoaded();
        cache.remove(artifactId);
        try {
            Files.deleteIfExists(metaPath(artifactId));
        } catch (IOException e) {
            throw new IllegalStateException("FS index delete failed for " + artifactId, e);
        }
    }

    private List<ArtifactMetadata> filterAndSort(java.util.function.Predicate<ArtifactMetadata> predicate) {
        ensureLoaded();
        return cache.values().stream()
                .filter(predicate)
                .sorted(Comparator.comparing(ArtifactMetadata::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    private void ensureLoaded() {
        if (loaded) return;
        synchronized (this) {
            if (loaded) return;
            loadAll();
            loaded = true;
        }
    }

    private void loadAll() {
        try {
            if (!Files.exists(root)) return;
            try (var stream = Files.list(root)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".meta.json"))
                        .forEach(p -> {
                            try {
                                var bytes = Files.readAllBytes(p);
                                var meta = JsonSerde.fromBytes(bytes, ArtifactMetadata.class);
                                cache.put(meta.getArtifactId(), meta);
                            } catch (Exception ignored) {
                                // best-effort load
                            }
                        });
            }
        } catch (IOException e) {
            throw new IllegalStateException("FS index load failed", e);
        }
    }

    private void persist(ArtifactMetadata metadata) {
        try {
            Files.createDirectories(root);
            Files.write(metaPath(metadata.getArtifactId()), JsonSerde.toBytes(metadata));
        } catch (IOException e) {
            throw new IllegalStateException("FS index persist failed for " + metadata.getArtifactId(), e);
        }
    }

    private Path metaPath(String artifactId) {
        return root.resolve(artifactId + ".meta.json");
    }
}
