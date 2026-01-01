package io.github.swampus.alexandra.networkapi.weight.application.usecase.impl;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.usecase.GetWeightsUseCase;
import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;

import java.util.Objects;

/** Returns current weights from model meta. */
public final class GetWeightsUseCaseImpl implements GetWeightsUseCase {

    @Override
    public Weights get(ModelWithMeta model) {
        Objects.requireNonNull(model, "model");
        return WeightsUseCaseSupport.requireCurrentWeights(model);
    }
}
