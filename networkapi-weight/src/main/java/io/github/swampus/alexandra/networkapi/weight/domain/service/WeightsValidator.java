package io.github.swampus.alexandra.networkapi.weight.domain.service;

import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;

import java.util.Map;

/** Validates weights against expected parameter shapes. */
public interface WeightsValidator {

    void validateExact(Map<String, int[]> expectedShapes, Weights weights);

    void validateMergePatch(Map<String, int[]> expectedShapes, Map<String, double[]> patch);
}
