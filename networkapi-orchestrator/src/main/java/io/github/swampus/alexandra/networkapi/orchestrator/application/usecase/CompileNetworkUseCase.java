package io.github.swampus.alexandra.networkapi.orchestrator.application.usecase;

import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.CompilerPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompileNetworkUseCase {

    private final CompilerPort compiler;

    public CompileResponseDto compile(String source, boolean trace) {
        return compiler.compile(source, trace);
    }
}

