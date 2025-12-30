package io.github.swampus.alexandra.networkapi.compiler.application.usecase;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.validator.NetworkModelValidator;
import io.github.swampus.alexandra.compiler.validator.ValidationLevel;
import io.github.swampus.alexandra.dto.shared.ParseErrorDto;
import io.github.swampus.alexandra.dto.shared.instruction.InstructionDto;
import io.github.swampus.alexandra.dto.shared.network.NNetworkDto;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.networkapi.compiler.application.port.CompilePayloadPort;
import io.github.swampus.alexandra.networkapi.compiler.application.port.InstructionMapperPort;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkCompilerPort;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkModelMapperPort;
import io.github.swampus.alexandra.translator.NureonLangToIRTranslator;
import io.github.swampus.alexandra.translator.exception.NureonLangTranslateException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Application-level use case responsible for compiling a NureonLang source
 * into a validated, executable network representation.
 *
 * <p>This class orchestrates the full compilation pipeline:
 * <ol>
 *   <li>Translation of source code into an intermediate representation (IR)</li>
 *   <li>Compilation of IR into an internal {@link NetworkModel}</li>
 *   <li>Optional semantic and structural validation</li>
 *   <li>Mapping of the compiled model into transport-friendly DTOs</li>
 *   <li>Construction of a canonical binary payload for persistence</li>
 * </ol>
 *
 * <p>This use case contains no transport or persistence logic and does not
 * expose its result outside the compiler service boundary.
 */
@AllArgsConstructor
public class CompileNetworkUseCase {

    /**
     * Translates NureonLang source code into IR instructions.
     */
    private final NureonLangToIRTranslator translator;

    /**
     * Compiles IR into an internal network model.
     */
    private final NetworkCompilerPort networkCompilerPort;

    /**
     * Maps IR instructions into DTO form for diagnostics and inspection.
     */
    private final InstructionMapperPort instructionMapper;

    /**
     * Maps compiled network models into shared network DTOs.
     */
    private final NetworkModelMapperPort modelMapper;

    /**
     * Performs semantic and structural validation of compiled models.
     */
    private final NetworkModelValidator validator;

    /**
     * Builds a canonical binary payload suitable for persistent storage.
     */
    private final CompilePayloadPort payloadBuilder;

    /**
     * Compiles the provided NureonLang source code into a validated network artifact.
     *
     * <p>This method represents the core compilation workflow and is intended
     * for internal use within the compiler service. It does not throw exceptions
     * for expected compilation failures; all errors are reported via
     * {@link CompileResult}.
     *
     * @param source          NureonLang source code
     * @param traceRequired   whether a detailed compilation trace should be collected
     * @param validationLevel level of post-compilation validation to apply
     * @return structured compilation result containing model, payload and diagnostics
     */
    public CompileResult compile(
            String source,
            boolean traceRequired,
            ValidationLevel validationLevel
    ) {
        final Instruction ir;

        try {
            // 1. Translate source code into IR
            ir = translator.translate(source);

        } catch (NureonLangTranslateException e) {
            // Syntax OR semantic errors
            return CompileResult.failure(
                    "PARSE_ERROR",
                    null,
                    mapParseErrors(e)
            );
        }

        // 2. Compile IR into an internal network model
        NetworkCompilerPort.CompilationOutput output =
                networkCompilerPort.compile(ir, traceRequired);

        NetworkModel model = output.model();

        // 3. Optional validation
        if (validationLevel != ValidationLevel.NONE) {
            validator.validate(model);
        }

        // 4. Map IR and model into DTOs
        InstructionDto irDto = instructionMapper.toDto(ir);
        NNetworkDto networkDto =
                modelMapper.mapModelToDto(model, source, irDto);

        // 5. Build payload
        byte[] payload =
                payloadBuilder.buildPayload(model, ir, source);

        return CompileResult.success(
                networkDto,
                payload,
                output.trace()
        );
    }


    // ====== RESULT TYPE (INNER CLASS) ======

    @Getter
    public static final class CompileResult {

        private final NNetworkDto model;
        private final byte[] payload;
        private final String error;
        private final String trace;
        private final List<ParseErrorDto> parseErrors;

        private CompileResult(
                NNetworkDto model,
                byte[] payload,
                String error,
                String trace,
                List<ParseErrorDto> parseErrors
        ) {
            this.model = model;
            this.payload = payload;
            this.error = error;
            this.trace = trace;
            this.parseErrors = parseErrors;
        }

        public static CompileResult success(
                NNetworkDto model,
                byte[] payload,
                String trace
        ) {
            return new CompileResult(model, payload, null, trace, null);
        }

        public static CompileResult failure(
                String error,
                String trace,
                List<ParseErrorDto> parseErrors
        ) {
            return new CompileResult(null, null, error, trace, parseErrors);
        }

        public boolean isSuccessful() {
            return error == null && (parseErrors == null || parseErrors.isEmpty());
        }
    }

    private List<ParseErrorDto> mapParseErrors(NureonLangTranslateException e) {
        return e.getErrors().stream()
                .map(err -> new ParseErrorDto(
                        err.getMessage(),
                        err.getLine()
                ))
                .toList();
    }

}
