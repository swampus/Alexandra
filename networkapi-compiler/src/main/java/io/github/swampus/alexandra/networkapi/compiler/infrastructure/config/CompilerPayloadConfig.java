package io.github.swampus.alexandra.networkapi.compiler.infrastructure.config;

import io.github.swampus.alexandra.networkapi.compiler.application.port.CompilePayloadPort;
import io.github.swampus.alexandra.networkapi.compiler.infrastructure.payload.DefaultCompilePayloadBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure configuration for compilation payload building.
 */
@Configuration
public class CompilerPayloadConfig {

    @Bean
    public CompilePayloadPort compilePayloadPort() {
        return new DefaultCompilePayloadBuilder();
    }
}

