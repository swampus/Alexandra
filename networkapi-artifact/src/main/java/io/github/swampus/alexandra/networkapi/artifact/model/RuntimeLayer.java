package io.github.swampus.alexandra.networkapi.artifact.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Getter
@Builder
@Jacksonized
public class RuntimeLayer {

    private final String name;
    private final String type;
    private final Map<String, Object> params;
}
