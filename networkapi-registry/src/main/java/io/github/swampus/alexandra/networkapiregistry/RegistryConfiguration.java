package io.github.swampus.alexandra.networkapiregistry;

import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.DeleteArtifactUseCase;
import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.GetArtifactUseCase;
import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.SaveArtifactUseCase;
import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.SearchArtifactsUseCase;
import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactIndexStore;
import io.github.swampus.alexandra.networkapiregistry.domain.port.ArtifactPayloadStore;
import io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.fs.FileSystemArtifactIndexStore;
import io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.fs.FileSystemArtifactPayloadStore;
import io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.memory.InMemoryArtifactIndexStore;
import io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.memory.InMemoryArtifactPayloadStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class RegistryConfiguration {

    @Bean
    public ArtifactPayloadStore artifactPayloadStore(
            @Value("${networkapi.registry.storage:memory}") String storage,
            @Value("${networkapi.registry.fs.root:./storage/artifacts}") String fsRoot
    ) {
        return switch (storage) {
            case "fs" -> new FileSystemArtifactPayloadStore(Path.of(fsRoot));
            case "memory" -> new InMemoryArtifactPayloadStore();
            case "s3" -> throw new UnsupportedOperationException("S3 backend not implemented yet");
            default -> throw new IllegalArgumentException("Unknown storage backend: " + storage);
        };
    }

    @Bean
    public ArtifactIndexStore artifactIndexStore(
            @Value("${networkapi.registry.storage:memory}") String storage,
            @Value("${networkapi.registry.fs.root:./storage/artifacts}") String fsRoot
    ) {
        return switch (storage) {
            case "fs" -> new FileSystemArtifactIndexStore(Path.of(fsRoot));
            case "memory" -> new InMemoryArtifactIndexStore();
            case "s3" -> throw new UnsupportedOperationException("S3 backend not implemented yet");
            default -> throw new IllegalArgumentException("Unknown storage backend: " + storage);
        };
    }

    @Bean
    public SaveArtifactUseCase saveArtifactUseCase(ArtifactPayloadStore payloadStore, ArtifactIndexStore indexStore) {
        return new SaveArtifactUseCase(payloadStore, indexStore);
    }

    @Bean
    public GetArtifactUseCase getArtifactUseCase(ArtifactPayloadStore payloadStore, ArtifactIndexStore indexStore) {
        return new GetArtifactUseCase(payloadStore, indexStore);
    }

    @Bean
    public SearchArtifactsUseCase searchArtifactsUseCase(ArtifactIndexStore indexStore) {
        return new SearchArtifactsUseCase(indexStore);
    }

    @Bean
    public DeleteArtifactUseCase deleteArtifactUseCase(ArtifactPayloadStore payloadStore, ArtifactIndexStore indexStore) {
        return new DeleteArtifactUseCase(payloadStore, indexStore);
    }
}
