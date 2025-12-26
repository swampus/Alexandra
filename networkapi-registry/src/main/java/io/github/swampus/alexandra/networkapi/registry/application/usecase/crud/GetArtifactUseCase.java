package io.github.swampus.alexandra.networkapi.registry.application.usecase.crud;

import io.github.swampus.alexandra.networkapi.registry.domain.port.ArtifactIndexStore;
import io.github.swampus.alexandra.networkapi.registry.domain.port.ArtifactPayloadStore;
import io.github.swampus.alexandra.networkapi.registry.domain.model.StoredArtifact;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class GetArtifactUseCase {

    private final ArtifactPayloadStore payloadStore;
    private final ArtifactIndexStore indexStore;

    public GetArtifactUseCase(ArtifactPayloadStore payloadStore, ArtifactIndexStore indexStore) {
        this.payloadStore = Objects.requireNonNull(payloadStore, "payloadStore");
        this.indexStore = Objects.requireNonNull(indexStore, "indexStore");
    }

    public StoredArtifact execute(String artifactId) {
        var metadata = indexStore.findById(artifactId)
                .orElseThrow(() -> new NoSuchElementException("Artifact metadata not found: " + artifactId));

        var payload = payloadStore.get(artifactId)
                .orElseThrow(() -> new NoSuchElementException("Artifact payload not found: " + artifactId));

        return new StoredArtifact(metadata, payload);
    }
}
