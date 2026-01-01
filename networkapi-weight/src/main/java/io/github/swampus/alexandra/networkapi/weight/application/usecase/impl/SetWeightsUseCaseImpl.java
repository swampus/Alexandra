package io.github.swampus.alexandra.networkapi.weight.application.usecase.impl;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.usecase.SetWeightsUseCase;

import java.util.Map;
import java.util.Objects;

public final class SetWeightsUseCaseImpl implements SetWeightsUseCase {

    public static final String META_WEIGHTS = "weights.current";
    public static final String META_VERSION = "weights.version";

    @Override
    public void set(ModelWithMeta model, SetWeightsCommand command) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(command, "command");

        Map<String, Object> meta = model.meta();
        meta.put(META_WEIGHTS, command.weights());
        meta.put(META_VERSION, command.version());
    }
}
