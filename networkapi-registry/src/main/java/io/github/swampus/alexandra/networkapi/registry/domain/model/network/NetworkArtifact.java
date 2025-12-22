package io.github.swampus.alexandra.networkapi.registry.domain.model.network;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable registry artifact representing a compiled neural network.
 *
 * <p>This object is a long-term, stable persistence model intended for registry storage
 * (S3 / filesystem / database).</p>
 *
 * <p>It is NOT a runtime DTO and NOT a compiler internal model.</p>
 */
@Getter
@Builder(toBuilder = true)
public final class NetworkArtifact {

    /** Unique artifact identifier */
    private final String id;

    /** Human-readable network name */
    private final String name;

    /** Artifact format version (e.g. "artifact-v1") */
    private final String formatVersion;

    /** Serialized compiled IR (stable, versioned representation) */
    private final String compiledIr;

    /** Optional external reference to weights (S3 key, FS path, etc.) */
    private final Optional<String> weightsRef;

    /** Lifecycle state */
    private final NetworkState state;

    /** Classification tags */
    @Singular
    private final Set<String> tags;

    /** Associated task identifiers */
    @Singular
    private final Set<String> taskNames;

    /** Associated ruleset identifiers */
    @Singular
    private final Set<String> rulesets;

    /** Optional cluster binding */
    private final Optional<String> clusterId;

    /** Free-form metadata (compiler version, provenance, etc.) */
    @Singular("metaEntry")
    private final Map<String, Object> meta;

    /** Artifact creation timestamp */
    private final Instant createdAt;

    /** Last update timestamp */
    private final Instant updatedAt;

    /**
     * Factory method for new artifacts.
     * Enforces invariant defaults.
     */
    public static NetworkArtifact createNew(
            String id,
            String name,
            String compiledIr,
            Optional<String> weightsRef
    ) {
        Instant now = Instant.now();

        return NetworkArtifact.builder()
                .id(id)
                .name(name)
                .formatVersion("artifact-v1")
                .compiledIr(compiledIr)
                .weightsRef(weightsRef)
                .state(NetworkState.READY)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}

