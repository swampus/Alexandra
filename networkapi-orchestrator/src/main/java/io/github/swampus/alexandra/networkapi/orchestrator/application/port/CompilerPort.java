package io.github.swampus.alexandra.networkapi.orchestrator.application.port;

import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.dto.shared.response.ParseResponseDto;

public interface CompilerPort {

    ParseResponseDto parse(String source, boolean trace);

    CompileResponseDto compile(String source, boolean trace);
}

