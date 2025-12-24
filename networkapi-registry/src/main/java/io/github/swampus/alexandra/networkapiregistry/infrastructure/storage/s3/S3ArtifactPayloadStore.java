package io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.s3;

import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactPayloadStore;

import java.util.Optional;

/**
 * S3 payload store stub.
 * Intentionally left as TODO to avoid pulling AWS SDK prematurely.
 */
public final class S3ArtifactPayloadStore implements ArtifactPayloadStore {

    @Override
    public void put(String artifactId, byte[] payload) {
        throw new UnsupportedOperationException("S3 backend not implemented yet");
    }

    @Override
    public Optional<byte[]> get(String artifactId) {
        throw new UnsupportedOperationException("S3 backend not implemented yet");
    }

    @Override
    public void delete(String artifactId) {
        throw new UnsupportedOperationException("S3 backend not implemented yet");
    }
}
