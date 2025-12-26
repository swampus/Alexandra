package io.github.swampus.alexandra.networkapi.orchestrator.application.domain;

import java.util.List;

public record RegistrySearchQuery(
        String artifactId,
        String clusterId,
        String taskId,
        String version,
        String language,
        List<String> tags,
        Integer limit,
        Integer offset
) {}

