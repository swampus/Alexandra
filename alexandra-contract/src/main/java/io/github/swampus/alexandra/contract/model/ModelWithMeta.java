package io.github.swampus.alexandra.contract.model;

import java.util.Map;

/**
 * Minimal boundary contract for models carrying metadata.
 *
 * No behavior. No framework dependencies.
 */
public interface ModelWithMeta {

    Map<String, Object> meta();
}
