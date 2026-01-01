package io.github.swampus.alexandra.networkapi.weight.application.usecase.impl;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;
import io.github.swampus.alexandra.networkapi.weight.infrastructure.meta.NetworkModelMetaKeys;

import java.util.Objects;

final class WeightsUseCaseSupport {

    private WeightsUseCaseSupport() {}

    static Weights requireCurrentWeights(ModelWithMeta model) {
        Objects.requireNonNull(model, "model");
        Object w = model.meta().get(NetworkModelMetaKeys.WEIGHTS_CURRENT);
        if (w == null) {
            throw new IllegalStateException("No current weights found in model.meta[" + NetworkModelMetaKeys.WEIGHTS_CURRENT + "]");
        }
        if (!(w instanceof Weights weights)) {
            throw new IllegalStateException("Invalid weights type in meta: " + w.getClass());
        }
        return weights;
    }
}
