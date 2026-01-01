package io.github.swampus.alexandra.networkapi.weight.application.usecase;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;

/** Loads weights attached to the model (active version). */
public interface GetWeightsUseCase {

    Weights get(ModelWithMeta model);
}
