package io.github.swampus.alexandra.networkapi.weight;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple test-only implementation of ModelWithMeta.
 *
 * <p>DO NOT move to production modules.</p>
 */
public final class TestModel implements ModelWithMeta {

    private final Map<String, Object> meta = new HashMap<>();

    @Override
    public Map<String, Object> meta() {
        return meta;
    }
}

