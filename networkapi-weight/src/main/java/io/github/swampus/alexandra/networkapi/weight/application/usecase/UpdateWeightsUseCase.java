package io.github.swampus.alexandra.networkapi.weight.application.usecase;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;

import java.util.Map;

/** Merges provided patch with existing weights. */
public interface UpdateWeightsUseCase {

    void update(ModelWithMeta model, UpdateWeightsCommand command);

    record UpdateWeightsCommand(
            Map<String, double[]> patch,
            String newVersion
    ) {}
}
