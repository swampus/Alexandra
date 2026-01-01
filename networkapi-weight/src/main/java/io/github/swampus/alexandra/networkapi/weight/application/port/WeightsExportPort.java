package io.github.swampus.alexandra.networkapi.weight.application.port;

import io.github.swampus.alexandra.networkapi.weight.domain.model.*;

public interface WeightsExportPort {
    byte[] export(Weights weights, WeightsManifest manifest);
}
