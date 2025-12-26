package io.github.swampus.alexandra.networkapi.compiler.web.controller.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.swampus.alexandra.dto.shared.ParseErrorDto;
import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.dto.shared.response.ParseResponseDto;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.CompileNetworkUseCase;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.ParseNueronLangSourceUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps application-layer use case results to REST response DTOs.
 *
 * <p>This mapper contains presentation-level logic only and does not
 * perform any business decisions.</p>
 */
@Component
public class CompileResponseMapper {

    private static final Logger log =
            LoggerFactory.getLogger(CompileResponseMapper.class);

    private final ObjectMapper objectMapper;

    public CompileResponseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ===================== COMPILE =====================

    public CompileResponseDto toCompileDto(
            CompileNetworkUseCase.CompileResult result
    ) {
        return new CompileResponseDto(
                serializeSafely(result.getModel()), // modelJson
                result.getError(),                   // error
                result.getTrace(),                   // compilationTrace
                result.getParseErrors(),             // parseErrors
                result.getModel(),                   // model
                result.getPayload()                  // payload
        );
    }

    // ===================== PARSE =====================

    public ParseResponseDto toParseDto(
            ParseNueronLangSourceUseCase.ParseResult result
    ) {
        if (result.isValid()) {
            return new ParseResponseDto(true, List.of());
        }

        return new ParseResponseDto(
                false,
                result.getErrors().stream()
                        .map(e -> new ParseErrorDto(
                                e.getMessage(),
                                e.getLine()
                        ))
                        .toList()
        );
    }

    // ===================== UTIL =====================

    /**
     * Serializes a model to JSON for presentation or debugging purposes.
     *
     * <p>Serialization failures are intentionally ignored and do not
     * affect the compilation result.</p>
     */
    private String serializeSafely(Object model) {
        if (model == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(model);
        } catch (Exception e) {
            log.debug(
                    "Failed to serialize compiled network model to JSON. " +
                            "This does not affect compilation result.",
                    e
            );
            return null;
        }
    }
}

