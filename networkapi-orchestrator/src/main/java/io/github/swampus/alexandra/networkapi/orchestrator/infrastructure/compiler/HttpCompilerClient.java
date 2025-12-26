package io.github.swampus.alexandra.networkapi.orchestrator.infrastructure.compiler;

import io.github.swampus.alexandra.dto.shared.request.CompileRequestDTO;
import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.dto.shared.response.ParseResponseDto;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.CompilerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class HttpCompilerClient implements CompilerPort {

    private final RestTemplate restTemplate;

    @Override
    public ParseResponseDto parse(String source, boolean trace) {
        return restTemplate.postForObject(
                "/api/v1/compiler/parse",
                new CompileRequestDTO(source, trace),
                ParseResponseDto.class
        );
    }

    @Override
    public CompileResponseDto compile(String source, boolean trace) {
        return restTemplate.postForObject(
                "/api/v1/compiler/compile",
                new CompileRequestDTO(source, trace),
                CompileResponseDto.class
        );
    }
}
