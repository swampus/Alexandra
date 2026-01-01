package io.github.swampus.alexandra.networkapi.weight.application.usecase;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;

import java.util.Map;

/**
 * Replaces current weights entirely.
 */
public interface SetWeightsUseCase {

    void set(ModelWithMeta model, SetWeightsCommand command);

    record SetWeightsCommand(
            Map<String, double[]> weights,
            String version
    ) {}
}
