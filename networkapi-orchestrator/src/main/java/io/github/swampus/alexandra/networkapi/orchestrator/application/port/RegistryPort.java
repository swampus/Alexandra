package io.github.swampus.alexandra.networkapi.orchestrator.application.port;

import io.github.swampus.alexandra.dto.shared.request.registry.SearchArtifactRequestDTO;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.CompiledArtifact;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.RegistryArtifact;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.RegistrySearchQuery;

import java.util.List;

public interface RegistryPort {

    void save(CompiledArtifact artifact);

    RegistryArtifact getById(String artifactId);

    void delete(String artifactId);

    List<RegistryArtifact> search(RegistrySearchQuery query);
}
