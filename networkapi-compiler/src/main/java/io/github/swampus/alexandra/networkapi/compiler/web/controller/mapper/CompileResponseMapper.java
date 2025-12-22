package io.github.swampus.alexandra.networkapi.compiler.web.controller.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.swampus.alexandra.dto.shared.ParseErrorDto;
import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.dto.shared.response.ParseResponseDto;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.CompileNetworkUseCase;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.ParseNueronLangSourceUseCase;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
                serializeSafely(result.getModel()),
                result.getError(),
                result.getTrace(),
                result.getParseErrors(),
                result.getModel()
        );
    }

    // ===================== PARSE =====================

    public ParseResponseDto toParseDto(ParseNueronLangSourceUseCase.ParseResult result) {
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

    private String buildParseErrorMessage(
            ParseNueronLangSourceUseCase.ParseResult result
    ) {
        if (result.getFailureReason() != null) {
            return switch (result.getFailureReason()) {
                case INTERNAL_ERROR -> "Internal parser error";
                case SYNTAX_ERROR -> formatSyntaxErrors(result.getErrors());
            };
        }

        return "Unknown parse error";
    }

    private String formatSyntaxErrors(List<SyntaxError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Syntax error";
        }

        return errors.stream()
                .map(SyntaxError::toString)
                .collect(Collectors.joining("\n"));
    }

    // ===================== UTIL =====================

    private String serializeSafely(Object model) {
        if (model == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(model);
        } catch (Exception e) {
            log.debug(
                    "Failed to serialize compiled network model to JSON. " +
                            "This does not affect compilation result.", e
            );
            return null;
        }
    }
}
