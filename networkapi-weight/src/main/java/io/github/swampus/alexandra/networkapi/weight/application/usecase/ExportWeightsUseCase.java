package io.github.swampus.alexandra.networkapi.weight.application.usecase;


import io.github.swampus.alexandra.contract.model.ModelWithMeta;

/** Exports current weights attached to the model. */
public interface ExportWeightsUseCase {

    byte[] export(ModelWithMeta model, ExportWeightsCommand command);
    record ExportWeightsCommand(
            String format
    ) {}
}
