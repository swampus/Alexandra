package io.github.swampus.alexandra.networkapiregistry.application.usecase.crud;

import io.github.swampus.alexandra.networkapiregistry.domain.model.StoredArtifact;
import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactIndexStore;
import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactPayloadStore;

import java.util.Objects;

/**
 * Saves payload + upserts metadata into index.
 */
public final class SaveArtifactUseCase {

    private final ArtifactPayloadStore payloadStore;
    private final ArtifactIndexStore indexStore;

    public SaveArtifactUseCase(ArtifactPayloadStore payloadStore, ArtifactIndexStore indexStore) {
        this.payloadStore = Objects.requireNonNull(payloadStore, "payloadStore");
        this.indexStore = Objects.requireNonNull(indexStore, "indexStore");
    }

    public void execute(StoredArtifact artifact) {
        Objects.requireNonNull(artifact, "artifact");
        var metadata = artifact.getMetadata();
        payloadStore.put(metadata.getArtifactId(), artifact.getPayload());
        indexStore.upsert(metadata);
    }
}
