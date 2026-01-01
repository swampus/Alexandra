package io.github.swampus.alexandra.networkapi.weight.application.usecase.impl;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.port.ShapeSpecProviderPort;
import io.github.swampus.alexandra.networkapi.weight.application.usecase.UpdateWeightsUseCase;
import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;
import io.github.swampus.alexandra.networkapi.weight.domain.service.WeightsValidator;
import io.github.swampus.alexandra.networkapi.weight.infrastructure.meta.NetworkModelMetaKeys;

import java.util.Objects;

/** Merges patch into existing weights and validates resulting snapshot. */
public final class UpdateWeightsUseCaseImpl implements UpdateWeightsUseCase {

    private final WeightsValidator validator;
    private final ShapeSpecProviderPort<ModelWithMeta> specProvider;

    public UpdateWeightsUseCaseImpl(WeightsValidator validator, ShapeSpecProviderPort<ModelWithMeta> specProvider) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.specProvider = Objects.requireNonNull(specProvider, "specProvider");
    }

    @Override
    public void update(ModelWithMeta model, UpdateWeightsCommand command) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(command.patch(), "patch");
        Objects.requireNonNull(command.newVersion(), "newVersion");

        validator.validateMergePatch(specProvider.paramShapes(model), command.patch());

        Weights base = WeightsUseCaseSupport.requireCurrentWeights(model);
        Weights merged = base.mergedWith(command.patch());

        validator.validateExact(specProvider.paramShapes(model), merged);

        model.meta().put(NetworkModelMetaKeys.WEIGHTS_CURRENT, merged);
        model.meta().put(NetworkModelMetaKeys.WEIGHTS_VERSION, command.newVersion());
    }
}
