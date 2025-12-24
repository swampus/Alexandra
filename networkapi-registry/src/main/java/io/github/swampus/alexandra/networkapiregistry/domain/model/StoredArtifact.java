package io.github.swampus.alexandra.networkapiregistry.domain.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * A stored artifact = metadata (indexed) + payload (bytes).
 */
public final class StoredArtifact {

    private final ArtifactMetadata metadata;
    private final byte[] payload;

    public StoredArtifact(ArtifactMetadata metadata, byte[] payload) {
        this.metadata = Objects.requireNonNull(metadata, "metadata");
        this.payload = Objects.requireNonNull(payload, "payload");
    }

    public ArtifactMetadata getMetadata() { return metadata; }

    public byte[] getPayload() { return payload; }

    public StoredArtifact withPayload(byte[] newPayload) {
        return new StoredArtifact(this.metadata, Objects.requireNonNull(newPayload, "newPayload"));
    }

    public StoredArtifact withMetadata(ArtifactMetadata newMetadata) {
        return new StoredArtifact(Objects.requireNonNull(newMetadata, "newMetadata"), this.payload);
    }

    @Override
    public String toString() {
        return "StoredArtifact{" +
                "metadata=" + metadata.getArtifactId() +
                ", payloadBytes=" + payload.length +
                '}';
    }
}
