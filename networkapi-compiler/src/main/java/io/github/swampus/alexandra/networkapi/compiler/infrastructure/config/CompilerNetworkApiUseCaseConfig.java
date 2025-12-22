package io.github.swampus.alexandra.networkapi.compiler.infrastructure.config;

import io.github.swampus.alexandra.compiler.validator.NetworkModelValidator;
import io.github.swampus.alexandra.networkapi.compiler.application.port.InstructionMapperPort;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkCompilerPort;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkModelMapperPort;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.CompileNetworkUseCase;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.ParseNueronLangSourceUseCase;
import io.github.swampus.alexandra.nureonlang.parser.internal.InternalNureonLangService;
import io.github.swampus.alexandra.translator.NureonLangToIRTranslator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration responsible for wiring application-layer
 * use cases of the Network Compiler API.
 *
 * <p>This configuration defines how infrastructure-level components
 * (parsers, translators, compilers, mappers) are assembled into
 * pure application use cases.</p>
 *
 * <p><strong>Design principles:</strong>
 * <ul>
 *   <li>Use cases remain framework-agnostic</li>
 *   <li>All dependencies are injected via ports</li>
 *   <li>No business logic is placed in configuration</li>
 * </ul>
 * </p>
 *
 * <p>This class belongs to the infrastructure layer and acts as
 * a composition root for the compiler-related application logic.</p>
 */
@Configuration
public class CompilerNetworkApiUseCaseConfig {

    /**
     * Creates a use case for syntactic validation of NureonLang sources.
     *
     * @param internalNureonLangService low-level parser implementation
     * @return parse-only use case
     */
    @Bean
    public ParseNueronLangSourceUseCase parseNueronLangSourceUseCase(
            InternalNureonLangService internalNureonLangService
    ) {
        return new ParseNueronLangSourceUseCase(internalNureonLangService);
    }

    /**
     * Creates a use case responsible for compiling NureonLang sources
     * into executable network models.
     *
     * @param translator            translator from NureonLang to IR
     * @param instructionMapperPort mapper for IR instructions
     * @param modelMapper           mapper from internal models to DTOs
     * @param networkCompilerPort   compiler port hiding infrastructure details
     * @param validator             syntax validator
     * @return compilation use case
     */
    @Bean
    public CompileNetworkUseCase compileNetworkUseCase(
            NureonLangToIRTranslator translator,
            InstructionMapperPort instructionMapperPort,
            NetworkModelMapperPort modelMapper,
            NetworkCompilerPort networkCompilerPort,
            NetworkModelValidator validator
    ) {
        return new CompileNetworkUseCase(
                translator,
                networkCompilerPort,
                instructionMapperPort,
                modelMapper,
                validator
        );
    }
}
