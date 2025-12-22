package io.github.swampus.alexandra.networkapi.compiler.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.github.swampus.alexandra.compiler.IRNetworkCompiler;
import io.github.swampus.alexandra.compiler.NetworkCompilerFacade;
import io.github.swampus.alexandra.networkapi.compiler.application.port.InstructionMapperPort;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkCompilerPort;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkModelMapperPort;
import io.github.swampus.alexandra.networkapi.compiler.infrastructure.adapter.NetworkCompilerAdapter;
import io.github.swampus.alexandra.networkapi.compiler.infrastructure.mapper.InstructionMapper;
import io.github.swampus.alexandra.networkapi.compiler.infrastructure.mapper.NetworkModelMapper;
import io.github.swampus.alexandra.nureonlang.parser.impl.InternalNureonLangServiceImpl;
import io.github.swampus.alexandra.nureonlang.parser.internal.InternalNureonLangService;
import io.github.swampus.alexandra.translator.NureonLangToIRTranslator;
import io.github.swampus.alexandra.translator.impl.NureonLangToIRTranslatorImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure composition root for the Network Compiler API.
 */
@Configuration
public class NetworkCompilerInfrastructureConfig {

    // ===================== SERIALIZATION =====================

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    // ===================== PARSING =====================

    @Bean
    public InternalNureonLangService internalNureonLangService() {
        return new InternalNureonLangServiceImpl();
    }

    @Bean
    public NureonLangToIRTranslator nueronLangToIRTranslator(
            InternalNureonLangService internalNureonLangService
    ) {
        return new NureonLangToIRTranslatorImpl(internalNureonLangService);
    }

    // ===================== COMPILATION =====================

    @Bean
    public NetworkCompilerFacade networkCompilerFacade() {
        return new NetworkCompilerFacade();
    }

    /**
     * Trace-capable IR compiler.
     */
    @Bean
    public IRNetworkCompiler irNetworkCompiler() {
        return new IRNetworkCompiler();
    }

    /**
     * Adapter exposing compilation capabilities via application port.
     */
    @Bean
    public NetworkCompilerPort networkCompilerPort(
            NetworkCompilerFacade networkCompilerFacade,
            IRNetworkCompiler irNetworkCompiler
    ) {
        return new NetworkCompilerAdapter(
                networkCompilerFacade,
                irNetworkCompiler
        );
    }

    // ===================== MAPPERS =====================

    @Bean
    public InstructionMapperPort instructionMapper() {
        return new InstructionMapper();
    }

    @Bean
    public NetworkModelMapperPort networkModelMapper(
            ObjectMapper objectMapper
    ) {
        return new NetworkModelMapper(objectMapper);
    }
}
