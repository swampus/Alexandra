package io.github.swampus.alexandra.networkapi.compiler.application.usecase;

import io.github.swampus.alexandra.compiler.exception.CompilationException;
import io.github.swampus.alexandra.compiler.extensions.InvalidNetworkException;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.validator.NetworkModelValidator;
import io.github.swampus.alexandra.compiler.validator.ValidationLevel;
import io.github.swampus.alexandra.dto.shared.ParseErrorDto;
import io.github.swampus.alexandra.dto.shared.instruction.InstructionDto;
import io.github.swampus.alexandra.dto.shared.network.NNetworkDto;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.networkapi.compiler.application.port.InstructionMapperPort;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkCompilerPort;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkModelMapperPort;
import io.github.swampus.alexandra.translator.NureonLangToIRTranslator;
import io.github.swampus.alexandra.translator.exception.NureonLangTranslateException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Application use case responsible for compiling NureonLang source
 * into a compiled and validated network representation.
 *
 * <p>This use case orchestrates translation, compilation and validation,
 * enforcing semantic and structural correctness according to ValidationLevel.</p>
 */
@AllArgsConstructor
public class CompileNetworkUseCase {

    private final NureonLangToIRTranslator translator;
    private final NetworkCompilerPort networkCompilerPort;
    private final InstructionMapperPort instructionMapper;
    private final NetworkModelMapperPort modelMapper;
    private final NetworkModelValidator validator;

    /**
     * Compiles the provided source code into a network model.
     *
     * @param source        NureonLang source
     * @param traceRequired whether compilation trace should be collected
     * @param validationLevel validation strictness after compilation
     * @return structured compilation result
     */
    public CompileResult compile(
            String source,
            boolean traceRequired,
            ValidationLevel validationLevel
    ) {
        try {
            // 1. Parse + translate source into IR
            Instruction ir = translator.translate(source);

            // 2. Compile IR into NetworkModel
            NetworkCompilerPort.CompilationOutput output =
                    networkCompilerPort.compile(ir, traceRequired);

            NetworkModel model = output.model();

            // 3. Validate compiled model (semantic / structural)
            if (validationLevel != ValidationLevel.NONE) {
                validator.validate(model);
            }

            // 4. Map results to DTOs
            InstructionDto irDto = instructionMapper.toDto(ir);
            NNetworkDto dto =
                    modelMapper.mapModelToDto(model, source, irDto);

            return CompileResult.success(dto, output.trace());

        } catch (NureonLangTranslateException e) {
            return CompileResult.failure(
                    "PARSE_ERROR",
                    null,
                    mapParseErrors(e)
            );

        } catch (InvalidNetworkException e) {
            // Semantic or structural validation error
            return CompileResult.failure(
                    "INVALID_NETWORK",
                    null,
                    List.of(new ParseErrorDto(e.getMessage(), -1))
            );

        } catch (CompilationException e) {
            return CompileResult.failure("COMPILATION_ERROR", null, null);

        } catch (Exception e) {
            return CompileResult.failure("INTERNAL_ERROR", null, null);
        }
    }

    private List<ParseErrorDto> mapParseErrors(NureonLangTranslateException e) {
        return e.getErrors().stream()
                .map(err -> new ParseErrorDto(err.getMessage(), err.getLine()))
                .toList();
    }

    // =========================================================

    @Getter
    public static class CompileResult {

        private final NNetworkDto model;
        private final String error;
        private final String trace;
        private final List<ParseErrorDto> parseErrors;

        private CompileResult(
                NNetworkDto model,
                String error,
                String trace,
                List<ParseErrorDto> parseErrors
        ) {
            this.model = model;
            this.error = error;
            this.trace = trace;
            this.parseErrors = parseErrors;
        }

        public static CompileResult success(NNetworkDto model, String trace) {
            return new CompileResult(model, null, trace, null);
        }

        public static CompileResult failure(
                String error,
                String trace,
                List<ParseErrorDto> parseErrors
        ) {
            return new CompileResult(null, error, trace, parseErrors);
        }

        public boolean isSuccessful() {
            return error == null && (parseErrors == null || parseErrors.isEmpty());
        }
    }
}
