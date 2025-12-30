package io.github.swampus.alexandra.networkapi.compiler.web.controller.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests for CompileController.
 *
 * Contract:
 *  - /parse  -> syntax only, may return 400
 *  - /compile -> always 200, errors reported in body
 */
@SpringBootTest
@AutoConfigureMockMvc
class CompileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ===================== PARSE =====================

    @Test
    @DisplayName("PARSE: valid source should return valid=true")
    void parse_validSource_shouldReturnValidTrue() throws Exception {

        String source = """
            LAYER Input size=4
            LAYER Dense size=8
            CONNECT Input -> Dense
        """;

        mockMvc.perform(post("/api/v1/compiler/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(source)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    @DisplayName("PARSE: invalid syntax should return 400 with errors")
    void parse_invalidSource_shouldReturnErrors() throws Exception {

        String source = """
            LAYER Dense units
            CONNECT A -> B
        """;

        mockMvc.perform(post("/api/v1/compiler/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(source)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(greaterThanOrEqualTo(1)));
    }

    // ===================== COMPILE =====================

    @Test
    @DisplayName("COMPILE: valid model should compile successfully")
    void compile_validSource_shouldReturnModel() throws Exception {

        String source = """
            LAYER Input size=4
            LAYER Dense size=8
            CONNECT Input -> Dense
        """;

        mockMvc.perform(post("/api/v1/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(source)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(true))
                .andExpect(jsonPath("$.error").doesNotExist())
                .andExpect(jsonPath("$.model").exists());
    }

    @Test
    @DisplayName("COMPILE: semantic error should return unsuccessful result")
    void compile_semanticError_shouldReturnError() throws Exception {

        String source = """
            LAYER Input size=4
            LAYER Dense units=8
            CONNECT Input -> Dense
        """;

        mockMvc.perform(post("/api/v1/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(source)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.parseErrors").isArray());
    }

    @Test
    @DisplayName("COMPILE: logical error (CONNECT to missing layer) should fail")
    void compile_logicalError_shouldReturnError() throws Exception {

        String source = """
            LAYER Input size=4
            CONNECT Input -> Missing
        """;

        mockMvc.perform(post("/api/v1/compiler/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(source)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    // ===================== UTIL =====================

    private static String json(String source) {
        return """
            {
              "source": "%s"
            }
        """.formatted(
                source
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
        );
    }
}
