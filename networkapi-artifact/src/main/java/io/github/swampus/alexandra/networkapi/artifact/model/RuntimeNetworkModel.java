package io.github.swampus.alexandra.networkapi.artifact.model;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@Jacksonized
public class RuntimeNetworkModel implements ModelWithMeta {

    @Singular
    private final List<RuntimeLayer> layers;

    @Singular
    private final List<String> inputLayers;

    @Singular
    private final List<String> outputLayers;

    private final Map<String, Object> meta;

    @Override
    public Map<String, Object> meta() {
        return meta;
    }
}

