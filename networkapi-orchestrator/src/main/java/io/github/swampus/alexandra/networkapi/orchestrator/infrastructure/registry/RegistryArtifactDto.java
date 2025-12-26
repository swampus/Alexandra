package io.github.swampus.alexandra.networkapi.orchestrator.infrastructure.registry;

import java.time.Instant;
import java.util.List;

public record RegistryArtifactDto(
        String artifactId,
        String name,
        String version,
        String language,
        List<String> taskNames,
        List<String> tags,
        Instant createdAt
) {}

