package io.github.swampus.alexandra.networkapi.orchestrator.infrastructure.registry;

import io.github.swampus.alexandra.dto.shared.request.registry.SaveArtifactRequestDTO;
import io.github.swampus.alexandra.dto.shared.request.registry.SearchArtifactRequestDTO;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.CompiledArtifact;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.RegistryArtifact;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.RegistrySearchQuery;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.RegistryPort;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class RegistryRestAdapter implements RegistryPort {

    private final HttpRegistryClient client;

    @Override
    public void save(CompiledArtifact artifact) {

        SaveArtifactRequestDTO dto = new SaveArtifactRequestDTO(
                artifact.artifactId(),
                artifact.version(),
                artifact.name(),
                artifact.author(),
                null,
                null,
                artifact.taskNames(),
                artifact.tags(),
                artifact.language(),
                artifact.payload()
        );

        client.save(dto);
    }

    @Override
    public RegistryArtifact getById(String artifactId) {
        RegistryArtifactDto dto = client.getById(artifactId);
        return toDomain(dto);
    }

    @Override
    public void delete(String artifactId) {
        client.delete(artifactId);
    }

    @Override
    public List<RegistryArtifact> search(RegistrySearchQuery query) {

        SearchArtifactRequestDTO dto =
                new SearchArtifactRequestDTO(
                        query.artifactId(),
                        query.clusterId(),
                        query.taskId(),
                        query.version(),
                        query.language(),
                        query.limit(),
                        query.offset(),
                        query.tags()
                );

        return client.search(dto)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private RegistryArtifact toDomain(RegistryArtifactDto dto) {
        return new RegistryArtifact(
                dto.artifactId(),
                dto.name(),
                dto.version(),
                dto.language(),
                "state",
                dto.taskNames(),
                dto.tags(),
                dto.createdAt()
        );
    }

}
