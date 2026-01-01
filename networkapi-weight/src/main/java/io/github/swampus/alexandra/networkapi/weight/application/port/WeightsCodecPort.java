package io.github.swampus.alexandra.networkapi.weight.application.port;

import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;

/** Encodes/decodes weights for import/export. */
public interface WeightsCodecPort {
    byte[] encode(Weights weights);
    Weights decode(byte[] payload);
    String format();
}
