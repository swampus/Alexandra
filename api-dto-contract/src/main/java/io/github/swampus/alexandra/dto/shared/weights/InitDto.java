package io.github.swampus.alexandra.dto.shared.weights;

public record InitDto(
        InitMode mode,
        Double min,
        Double max,
        Double mean,
        Double std,
        Double constant
) {}

