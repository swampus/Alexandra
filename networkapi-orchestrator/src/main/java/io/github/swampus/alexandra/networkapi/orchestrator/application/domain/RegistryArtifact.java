package io.github.swampus.alexandra.networkapi.orchestrator.application.domain;

import java.time.Instant;
import java.util.List;

public record RegistryArtifact(
        String artifactId,
        String name,
        String version,
        String language,
        String state,
        List<String> taskNames,
        List<String> tags,
        Instant createdAt
) {
}

