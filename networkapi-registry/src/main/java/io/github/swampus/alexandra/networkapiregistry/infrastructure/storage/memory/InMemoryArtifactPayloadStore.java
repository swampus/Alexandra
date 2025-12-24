package io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.memory;

import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactPayloadStore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryArtifactPayloadStore implements ArtifactPayloadStore {

    private final Map<String, byte[]> store = new ConcurrentHashMap<>();

    @Override
    public void put(String artifactId, byte[] payload) {
        store.put(artifactId, payload);
    }

    @Override
    public Optional<byte[]> get(String artifactId) {
        return Optional.ofNullable(store.get(artifactId));
    }

    @Override
    public void delete(String artifactId) {
        store.remove(artifactId);
    }
}
