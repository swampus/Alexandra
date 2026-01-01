package io.github.swampus.alexandra.networkapi.weight.application.usecase;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;

/** Imports weights payload into the model, replacing current weights. */
public interface ImportWeightsUseCase {

    void importWeights(ModelWithMeta model, ImportWeightsCommand command);

    record ImportWeightsCommand(
            String format,
            byte[] payload,
            String version
    ) {}
}
