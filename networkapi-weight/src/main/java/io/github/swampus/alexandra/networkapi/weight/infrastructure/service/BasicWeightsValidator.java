package io.github.swampus.alexandra.networkapi.weight.infrastructure.service;

import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;
import io.github.swampus.alexandra.networkapi.weight.domain.service.WeightsValidator;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Strict validator:
 *  - validates exact weights map matches expected keys and lengths
 *  - validates patch keys/lengths for merge operations
 */
public final class BasicWeightsValidator implements WeightsValidator {

    @Override
    public void validateExact(Map<String, int[]> expectedShapes, Weights weights) {
        Objects.requireNonNull(expectedShapes, "expectedShapes");
        Objects.requireNonNull(weights, "weights");

        if (expectedShapes.isEmpty()) {
            throw new IllegalStateException("No parameter shapes provided by ShapeSpecProviderPort");
        }

        Map<String, double[]> flat = weights.flat();

        for (var e : expectedShapes.entrySet()) {
            String key = e.getKey();
            int expectedLen = product(e.getValue());
            double[] arr = flat.get(key);
            if (arr == null) {
                throw new NoSuchElementException("Missing weights for key: " + key);
            }
            if (arr.length != expectedLen) {
                throw new IllegalArgumentException("Bad weights length for key=" + key
                        + ": " + arr.length + " != " + expectedLen);
            }
        }

        for (String key : flat.keySet()) {
            if (!expectedShapes.containsKey(key)) {
                throw new IllegalArgumentException("Unknown weights key: " + key);
            }
        }
    }

    @Override
    public void validateMergePatch(Map<String, int[]> expectedShapes, Map<String, double[]> patch) {
        Objects.requireNonNull(expectedShapes, "expectedShapes");
        Objects.requireNonNull(patch, "patch");

        if (expectedShapes.isEmpty()) {
            throw new IllegalStateException("No parameter shapes provided by ShapeSpecProviderPort");
        }

        for (var e : patch.entrySet()) {
            String key = e.getKey();
            if (!expectedShapes.containsKey(key)) {
                throw new IllegalArgumentException("Unknown weights key in patch: " + key);
            }
            double[] arr = e.getValue();
            if (arr == null) continue;
            int expectedLen = product(expectedShapes.get(key));
            if (arr.length != expectedLen) {
                throw new IllegalArgumentException("Bad patch length for key=" + key
                        + ": " + arr.length + " != " + expectedLen);
            }
        }
    }

    private static int product(int[] dims) {
        if (dims == null || dims.length == 0) return 0;
        long p = 1;
        for (int d : dims) {
            if (d <= 0) throw new IllegalArgumentException("Invalid dimension: " + d);
            p *= d;
            if (p > Integer.MAX_VALUE) throw new IllegalArgumentException("Tensor too large: " + p);
        }
        return (int) p;
    }
}
