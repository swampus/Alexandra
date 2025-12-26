package io.github.swampus.alexandra.networkapi.registry.infrastructure.storage.fs;

import io.github.swampus.alexandra.networkapi.registry.domain.port.ArtifactPayloadStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class FileSystemArtifactPayloadStore implements ArtifactPayloadStore {

    private final Path root;

    public FileSystemArtifactPayloadStore(Path root) {
        this.root = root;
    }

    @Override
    public void put(String artifactId, byte[] payload) {
        try {
            Files.createDirectories(root);
            Files.write(root.resolve(artifactId + ".bin"), payload);
        } catch (IOException e) {
            throw new IllegalStateException("FS put failed for " + artifactId, e);
        }
    }

    @Override
    public Optional<byte[]> get(String artifactId) {
        try {
            var p = root.resolve(artifactId + ".bin");
            if (!Files.exists(p)) return Optional.empty();
            return Optional.of(Files.readAllBytes(p));
        } catch (IOException e) {
            throw new IllegalStateException("FS get failed for " + artifactId, e);
        }
    }

    @Override
    public void delete(String artifactId) {
        try {
            Files.deleteIfExists(root.resolve(artifactId + ".bin"));
        } catch (IOException e) {
            throw new IllegalStateException("FS delete failed for " + artifactId, e);
        }
    }
}
