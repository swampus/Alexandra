package io.github.swampus.alexandra.networkapi.compiler.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.swampus.alexandra.compiler.validator.ValidationLevel;
import io.github.swampus.alexandra.dto.shared.ParseErrorDto;
import io.github.swampus.alexandra.dto.shared.request.CompileRequestDTO;
import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.dto.shared.response.ParseResponseDto;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.CompileNetworkUseCase;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.ParseNueronLangSourceUseCase;
import io.github.swampus.alexandra.networkapi.compiler.web.controller.mapper.CompileResponseMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompileController.class)
@Import(CompileControllerTest.TestConfig.class)
class CompileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompileNetworkUseCase compileUseCase;

    @Autowired
    private ParseNueronLangSourceUseCase parseUseCase;

    @Autowired
    private CompileResponseMapper responseMapper;

    // ===================== PARSE OK =====================

    @Test
    void parse_validSource_returns200_andEmptyErrors() throws Exception {
        var request = validRequest("VALID_CODE");

        var useCaseResult = ParseNueronLangSourceUseCase.ParseResult.valid();
        var responseDto = new ParseResponseDto(true, List.of());

        Mockito.when(parseUseCase.parse("VALID_CODE", false))
                .thenReturn(useCaseResult);
        Mockito.when(responseMapper.toParseDto(useCaseResult))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/compiler/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    // ===================== PARSE FAIL =====================

    @Test
    void parse_invalidSource_returns400_andStructuredErrors() throws Exception {
        var request = validRequest("BAD_CODE");

        var parseError = new ParseErrorDto("Unexpected token", 3);
        var responseDto = new ParseResponseDto(false, List.of(parseError));

        var useCaseResult =
                ParseNueronLangSourceUseCase.ParseResult.invalid(List.of());

        Mockito.when(parseUseCase.parse("BAD_CODE", false))
                .thenReturn(useCaseResult);
        Mockito.when(responseMapper.toParseDto(useCaseResult))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/compiler/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors[0].message").value("Unexpected token"))
                .andExpect(jsonPath("$.errors[0].line").value(3));
    }

    // ===================== COMPILE OK =====================

    @Test
    void compile_validSource_returns200() throws Exception {
        var request = validRequest("VALID_CODE");

        var compileResult =
                Mockito.mock(CompileNetworkUseCase.CompileResult.class);
        var responseDto =
                Mockito.mock(CompileResponseDto.class);

        Mockito.when(
                compileUseCase.compile(
                        Mockito.eq("VALID_CODE"),
                        Mockito.eq(false),
                        Mockito.eq(ValidationLevel.STRUCTURAL)
                )
        ).thenReturn(compileResult);

        Mockito.when(responseMapper.toCompileDto(compileResult))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ===================== HELPERS =====================

    private CompileRequestDTO validRequest(String source) {
        return new CompileRequestDTO(
                "test-name",      // name
                source,           // source ← ВАЖНО
                "tester",         // author
                "desc",           // description
                false,            // compilationTraceRequired
                false             // optimizeAllowed
        );
    }


    // ===================== TEST CONFIG =====================

    @TestConfiguration
    static class TestConfig {

        @Bean
        CompileNetworkUseCase compileUseCase() {
            return Mockito.mock(CompileNetworkUseCase.class);
        }

        @Bean
        ParseNueronLangSourceUseCase parseUseCase() {
            return Mockito.mock(ParseNueronLangSourceUseCase.class);
        }

        @Bean
        CompileResponseMapper responseMapper() {
            return Mockito.mock(CompileResponseMapper.class);
        }
    }
}
