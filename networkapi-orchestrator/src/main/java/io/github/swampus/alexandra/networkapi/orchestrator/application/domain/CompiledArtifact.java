package io.github.swampus.alexandra.networkapi.orchestrator.application.domain;

import java.util.List;

public record CompiledArtifact(
        String artifactId,
        String version,
        String name,
        String author,
        List<String> taskNames,
        List<String> tags,
        String language,
        byte[] payload
) {}

