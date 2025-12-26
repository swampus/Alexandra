package io.github.swampus.alexandra.networkapi.orchestrator.application.usecase;

import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.RegistryArtifact;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.RegistrySearchQuery;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.RegistryPort;
import io.github.swampus.alexandra.dto.shared.response.NetworkSummaryDto;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SearchNetworksUseCase {

    private final RegistryPort registry;

    public List<NetworkSummaryDto> search(RegistrySearchQuery query) {

        List<RegistryArtifact> found = registry.search(query);

        return found.stream()
                .map(this::toSummary)
                .toList();
    }

    private NetworkSummaryDto toSummary(RegistryArtifact artifact) {
        return new NetworkSummaryDto(
                artifact.artifactId(),
                artifact.name(),
                artifact.version(),
                artifact.language(),
                artifact.state(),
                artifact.createdAt(),
                artifact.taskNames(),
                artifact.tags()
        );

    }
}
