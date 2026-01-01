package io.github.swampus.alexandra.networkapi.weight.application.usecase.impl;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.port.ShapeSpecProviderPort;
import io.github.swampus.alexandra.networkapi.weight.application.port.WeightsCodecPort;
import io.github.swampus.alexandra.networkapi.weight.application.usecase.ImportWeightsUseCase;
import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;
import io.github.swampus.alexandra.networkapi.weight.domain.service.WeightsValidator;
import io.github.swampus.alexandra.networkapi.weight.infrastructure.meta.NetworkModelMetaKeys;

import java.util.List;
import java.util.Objects;

/** Imports weights payload into the model (replace) after strict validation. */
public final class ImportWeightsUseCaseImpl implements ImportWeightsUseCase {

    private final List<WeightsCodecPort> codecs;
    private final WeightsValidator validator;
    private final ShapeSpecProviderPort<ModelWithMeta> specProvider;

    public ImportWeightsUseCaseImpl(List<WeightsCodecPort> codecs, WeightsValidator validator, ShapeSpecProviderPort<ModelWithMeta> specProvider) {
        this.codecs = Objects.requireNonNull(codecs, "codecs");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.specProvider = Objects.requireNonNull(specProvider, "specProvider");
    }

    @Override
    public void importWeights(ModelWithMeta model, ImportWeightsCommand command) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(command.payload(), "payload");
        Objects.requireNonNull(command.version(), "version");

        WeightsCodecPort codec = findCodec(command.format());
        Weights decoded = codec.decode(command.payload());

        validator.validateExact(specProvider.paramShapes(model), decoded);

        model.meta().put(NetworkModelMetaKeys.WEIGHTS_CURRENT, decoded);
        model.meta().put(NetworkModelMetaKeys.WEIGHTS_VERSION, command.version());
    }

    private WeightsCodecPort findCodec(String format) {
        if (format == null || format.isBlank()) throw new IllegalArgumentException("format must not be blank");
        return codecs.stream()
                .filter(c -> format.equalsIgnoreCase(c.format()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported weights format: " + format));
    }
}
