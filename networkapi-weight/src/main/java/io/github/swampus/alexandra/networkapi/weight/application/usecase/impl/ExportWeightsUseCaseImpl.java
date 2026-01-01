package io.github.swampus.alexandra.networkapi.weight.application.usecase.impl;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.port.WeightsCodecPort;
import io.github.swampus.alexandra.networkapi.weight.application.usecase.ExportWeightsUseCase;
import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;

import java.util.List;
import java.util.Objects;

/** Exports current weights using the selected codec. */
public final class ExportWeightsUseCaseImpl implements ExportWeightsUseCase {

    private final List<WeightsCodecPort> codecs;

    public ExportWeightsUseCaseImpl(List<WeightsCodecPort> codecs) {
        this.codecs = Objects.requireNonNull(codecs, "codecs");
    }

    @Override
    public byte[] export(ModelWithMeta model, ExportWeightsCommand command) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(command, "command");
        Weights weights = WeightsUseCaseSupport.requireCurrentWeights(model);
        WeightsCodecPort codec = findCodec(command.format());
        return codec.encode(weights);
    }

    private WeightsCodecPort findCodec(String format) {
        if (format == null || format.isBlank()) throw new IllegalArgumentException("format must not be blank");
        return codecs.stream()
                .filter(c -> format.equalsIgnoreCase(c.format()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported weights format: " + format));
    }
}
