package io.github.swampus.alexandra.networkapi.orchestrator.web.dto;

import java.util.List;

public record CompileAndSaveRequestDTO(
        String artifactId,
        String source,
        List<String> taskNames,
        String language,
        boolean compilationTraceRequired
) {}

