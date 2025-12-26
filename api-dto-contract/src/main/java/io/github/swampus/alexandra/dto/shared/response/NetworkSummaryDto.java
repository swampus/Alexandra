package io.github.swampus.alexandra.dto.shared.response;

import java.time.Instant;
import java.util.List;

/**
 * Lightweight network descriptor used for search results.
 * Does NOT contain network payload, IR or weights.
 */
public record NetworkSummaryDto(

        String artifactId,
        String name,
        String version,
        String language,
        String state,
        Instant createdAt,
        List<String> task,
        List<String> tags
) {}
