package io.github.swampus.alexandra.networkapi.weight.domain.model;

import java.time.Instant;

/** Metadata describing a stored weights snapshot. */
public record WeightsManifest(
        String networkId,
        WeightsVersion version,
        String initMode,
        long seed,
        String specHash,
        Instant createdAt
) {}
