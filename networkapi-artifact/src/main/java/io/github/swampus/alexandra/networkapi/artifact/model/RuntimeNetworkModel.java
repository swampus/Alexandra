package io.github.swampus.alexandra.networkapi.artifact.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@Jacksonized
public class RuntimeNetworkModel {

    @Singular
    private final List<RuntimeLayer> layers;

    @Singular
    private final List<String> inputLayers;

    @Singular
    private final List<String> outputLayers;

    private final Map<String, Object> meta;
}

