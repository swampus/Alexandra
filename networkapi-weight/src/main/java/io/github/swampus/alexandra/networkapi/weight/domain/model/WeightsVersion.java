package io.github.swampus.alexandra.networkapi.weight.domain.model;

import java.util.Objects;

/**
 * Weights version label (human-readable, stable identifier).
 * Examples: "init@2026-01-01T00:00:00Z", "trained@epoch10".
 */
public record WeightsVersion(String value) {

    public WeightsVersion {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("WeightsVersion must not be blank");
        }
    }

    public static WeightsVersion of(String value) {
        return new WeightsVersion(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
