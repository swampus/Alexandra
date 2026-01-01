package io.github.swampus.alexandra.networkapi.weight.domain.service;

import java.util.Map;

/** Hashes parameter shape specifications to detect architecture/weights mismatch. */
public interface WeightsHasher {
    String hashShapes(Map<String, int[]> shapes);
}
