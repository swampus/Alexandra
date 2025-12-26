package io.github.swampus.alexandra.networkapi.orchestrator.application.usecase;

import io.github.swampus.alexandra.dto.shared.response.NetworkSummaryDto;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.RegistryArtifact;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.RegistryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetNetworkByIdUseCase {

    private final RegistryPort registry;

    public NetworkSummaryDto getById(String artifactId) {
        RegistryArtifact artifact = registry.getById(artifactId);

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

