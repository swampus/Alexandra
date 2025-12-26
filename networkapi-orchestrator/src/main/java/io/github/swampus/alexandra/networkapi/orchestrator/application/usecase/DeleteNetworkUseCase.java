package io.github.swampus.alexandra.networkapi.orchestrator.application.usecase;

import io.github.swampus.alexandra.networkapi.orchestrator.application.port.RegistryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteNetworkUseCase {

    private final RegistryPort registry;

    public void delete(String artifactId) {
        registry.delete(artifactId);
    }
}
