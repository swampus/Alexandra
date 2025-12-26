package io.github.swampus.alexandra.networkapi.orchestrator.application.usecase;

import io.github.swampus.alexandra.dto.shared.response.ParseResponseDto;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.CompilerPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidateNetworkUseCase {

    private final CompilerPort compiler;

    public ParseResponseDto validate(String source, boolean trace) {
        return compiler.parse(source, trace);
    }
}
