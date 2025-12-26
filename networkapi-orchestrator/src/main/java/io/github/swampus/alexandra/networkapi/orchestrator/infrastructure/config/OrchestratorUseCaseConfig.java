package io.github.swampus.alexandra.networkapi.orchestrator.infrastructure.config;

import io.github.swampus.alexandra.networkapi.orchestrator.application.port.CompilerPort;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.RegistryPort;
import io.github.swampus.alexandra.networkapi.orchestrator.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrchestratorUseCaseConfig {

    // -----------------------------------------
    // VALIDATE
    // -----------------------------------------

    @Bean
    public ValidateNetworkUseCase validateNetworkUseCase(
            CompilerPort compilerPort
    ) {
        return new ValidateNetworkUseCase(compilerPort);
    }

    // -----------------------------------------
    // COMPILE (NO SAVE)
    // -----------------------------------------

    @Bean
    public CompileNetworkUseCase compileNetworkUseCase(
            CompilerPort compilerPort
    ) {
        return new CompileNetworkUseCase(compilerPort);
    }

    // -----------------------------------------
    // COMPILE + SAVE
    // -----------------------------------------

    @Bean
    public CompileAndSaveNetworkUseCase compileAndSaveNetworkUseCase(
            CompilerPort compilerPort,
            RegistryPort registryPort
    ) {
        return new CompileAndSaveNetworkUseCase(
                compilerPort,
                registryPort
        );
    }

    // -----------------------------------------
    // SEARCH
    // -----------------------------------------

    @Bean
    public SearchNetworksUseCase searchNetworksUseCase(
            RegistryPort registryPort
    ) {
        return new SearchNetworksUseCase(registryPort);
    }

    // -----------------------------------------
    // GET BY ID
    // -----------------------------------------

    @Bean
    public GetNetworkByIdUseCase getNetworkByIdUseCase(
            RegistryPort registryPort
    ) {
        return new GetNetworkByIdUseCase(registryPort);
    }

    // -----------------------------------------
    // DELETE
    // -----------------------------------------

    @Bean
    public DeleteNetworkUseCase deleteNetworkUseCase(
            RegistryPort registryPort
    ) {
        return new DeleteNetworkUseCase(registryPort);
    }
}
