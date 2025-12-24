package io.github.swampus.alexandra.networkapiregistry.application.usecase.crud;

import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactIndexStore;
import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactPayloadStore;

import java.util.Objects;

public final class DeleteArtifactUseCase {

    private final ArtifactPayloadStore payloadStore;
    private final ArtifactIndexStore indexStore;

    public DeleteArtifactUseCase(ArtifactPayloadStore payloadStore, ArtifactIndexStore indexStore) {
        this.payloadStore = Objects.requireNonNull(payloadStore, "payloadStore");
        this.indexStore = Objects.requireNonNull(indexStore, "indexStore");
    }

    public void execute(String artifactId) {
        payloadStore.delete(artifactId);
        indexStore.delete(artifactId);
    }
}
