package io.github.swampus.alexandra.networkapiregistry.domain.port;

import java.util.Optional;

public interface ArtifactPayloadStore {

    void put(String artifactId, byte[] payload);

    Optional<byte[]> get(String artifactId);

    void delete(String artifactId);
}
