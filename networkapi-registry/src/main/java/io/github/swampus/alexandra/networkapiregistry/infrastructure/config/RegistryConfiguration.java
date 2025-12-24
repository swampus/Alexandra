package io.github.swampus.alexandra.networkapiregistry.infrastructure.config;

import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.SaveArtifactUseCase;
import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.SearchArtifactsUseCase;
import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactIndexStore;
import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactPayloadStore;
import io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.fs.FileSystemArtifactIndexStore;
import io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.memory.InMemoryArtifactIndexStore;
import io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.fs.FileSystemArtifactPayloadStore;
import io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.memory.InMemoryArtifactPayloadStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Infrastructure-level configuration for registry module.
 *
 * <p>This class acts as the composition root for registry use cases
 * and storage implementations.</p>
 */
@Configuration
public class RegistryConfiguration {

    @Value("${registry.storage.type:memory}")
    private String storageType;

    @Value("${registry.storage.path:./registry-data}")
    private String storagePath;

    // ----------- Index Store -----------

    @Bean
    public ArtifactIndexStore artifactIndexStore() {
        return switch (storageType) {
            case "fs" -> new FileSystemArtifactIndexStore(Path.of(storagePath));
            case "memory" -> new InMemoryArtifactIndexStore();
            default -> throw new IllegalStateException(
                    "Unknown registry.storage.type: " + storageType);
        };
    }

    // ----------- Payload Store -----------

    @Bean
    public ArtifactPayloadStore artifactPayloadStore() {
        return switch (storageType) {
            case "fs" -> new FileSystemArtifactPayloadStore(Path.of(storagePath));
            case "memory" -> new InMemoryArtifactPayloadStore();
            default -> throw new IllegalStateException(
                    "Unknown registry.storage.type: " + storageType);
        };
    }

    // ----------- Use Cases -----------

    @Bean
    public SaveArtifactUseCase saveArtifactUseCase(
            ArtifactPayloadStore payloadStore,
            ArtifactIndexStore indexStore
    ) {
        return new SaveArtifactUseCase(payloadStore, indexStore);
    }

    @Bean
    public SearchArtifactsUseCase searchArtifactsUseCase(
            ArtifactIndexStore indexStore
    ) {
        return new SearchArtifactsUseCase(indexStore);
    }
}
