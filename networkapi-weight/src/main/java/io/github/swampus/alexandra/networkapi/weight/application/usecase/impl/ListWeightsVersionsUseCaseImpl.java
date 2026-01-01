package io.github.swampus.alexandra.networkapi.weight.application.usecase.impl;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.usecase.ListWeightsVersionsUseCase;
import io.github.swampus.alexandra.networkapi.weight.infrastructure.meta.NetworkModelMetaKeys;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Lists versions known to the model (if snapshots map exists), otherwise returns the active version only. */
public final class ListWeightsVersionsUseCaseImpl implements ListWeightsVersionsUseCase {

    @Override
    @SuppressWarnings("unchecked")
    public List<String> list(ModelWithMeta model) {
        Objects.requireNonNull(model, "model");
        Object snapshots = model.meta().get(NetworkModelMetaKeys.WEIGHTS_SNAPSHOTS);
        if (snapshots instanceof Map<?, ?> map) {
            return map.keySet().stream().map(String::valueOf).toList();
        }
        Object active = model.meta().get(NetworkModelMetaKeys.WEIGHTS_VERSION);
        return active == null ? List.of() : List.of(String.valueOf(active));
    }
}
