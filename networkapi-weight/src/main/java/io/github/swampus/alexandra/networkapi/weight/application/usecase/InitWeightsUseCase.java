package io.github.swampus.alexandra.networkapi.weight.application.usecase;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.domain.init.InitMode;

/** Initializes weights for a compiled NetworkModel. */
public interface InitWeightsUseCase {

    void init(ModelWithMeta model, InitWeightsCommand command);
    record InitWeightsCommand(
            InitMode mode,
            long seed,
            String version,
            Double constantValue
    ) {}
}
