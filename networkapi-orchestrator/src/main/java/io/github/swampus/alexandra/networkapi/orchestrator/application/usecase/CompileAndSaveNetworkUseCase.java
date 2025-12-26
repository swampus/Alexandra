package io.github.swampus.alexandra.networkapi.orchestrator.application.usecase;

import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.CompiledArtifact;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.CompilerPort;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.RegistryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CompileAndSaveNetworkUseCase {

    private static final String DEFAULT_VERSION = "v1";
    private static final String DEFAULT_AUTHOR = "system";

    private final CompilerPort compiler;
    private final RegistryPort registry;

    public CompileResponseDto execute(
            String artifactId,
            String source,
            List<String> taskNames,
            String language,
            boolean trace
    ) {
        CompileResponseDto result = compiler.compile(source, trace);

        if (result == null || !result.isSuccessful()) {
            return result;
        }

        CompiledArtifact artifact = new CompiledArtifact(
                artifactId,
                DEFAULT_VERSION,
                artifactId,
                DEFAULT_AUTHOR,
                taskNames == null ? List.of() : List.copyOf(taskNames),
                List.of(),
                language,
                result.payload()
        );

        registry.save(artifact);

        return result;
    }
}
