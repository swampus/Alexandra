package io.github.swampus.alexandra.networkapi.orchestrator.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.swampus.alexandra.dto.shared.request.CompileRequestDTO;
import io.github.swampus.alexandra.dto.shared.request.registry.SearchArtifactRequestDTO;
import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.dto.shared.response.NetworkSummaryDto;
import io.github.swampus.alexandra.dto.shared.response.ParseResponseDto;
import io.github.swampus.alexandra.networkapi.orchestrator.application.usecase.*;
import io.github.swampus.alexandra.networkapi.orchestrator.web.dto.CompileAndSaveRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NetworkOrchestratorController.class)
@ContextConfiguration(classes = {
        NetworkOrchestratorController.class
})
class NetworkOrchestratorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean ValidateNetworkUseCase validateUseCase;
    @MockBean CompileNetworkUseCase compileUseCase;
    @MockBean CompileAndSaveNetworkUseCase compileAndSaveUseCase;
    @MockBean SearchNetworksUseCase searchUseCase;
    @MockBean GetNetworkByIdUseCase getByIdUseCase;
    @MockBean DeleteNetworkUseCase deleteUseCase;

    // -------------------------
    // VALIDATE
    // -------------------------

    @Test
    void validate_shouldCallUseCase() throws Exception {
        ParseResponseDto response = new ParseResponseDto(true, List.of());

        when(validateUseCase.validate(anyString(), anyBoolean()))
                .thenReturn(response);

        CompileRequestDTO request =
                new CompileRequestDTO("source-code", true);

        mockMvc.perform(post("/api/v1/orchestrator/networks/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(validateUseCase)
                .validate("source-code", true);
    }

    // -------------------------
    // COMPILE
    // -------------------------

    @Test
    void compile_shouldCallUseCase() throws Exception {
        CompileResponseDto response = new CompileResponseDto(
                null,          // modelJson
                null,          // error  ← : null = success
                null,          // compilationTrace
                List.of(),     // parseErrors
                null,          // model
                new byte[]{1}  // payload (не null!)
        );


        when(compileUseCase.compile(anyString(), anyBoolean()))
                .thenReturn(response);

        CompileRequestDTO request =
                new CompileRequestDTO("source-code", false);

        mockMvc.perform(post("/api/v1/orchestrator/networks/compile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(compileUseCase)
                .compile("source-code", false);
    }

    // -------------------------
    // COMPILE + SAVE
    // -------------------------

    @Test
    void compileAndSave_shouldCallUseCase() throws Exception {
        CompileResponseDto response = new CompileResponseDto(
                null,          // modelJson
                null,          // error  ← : null = success
                null,          // compilationTrace
                List.of(),     // parseErrors
                null,          // model
                new byte[]{1}  // payload (не null!)
        );


        when(compileAndSaveUseCase.execute(
                anyString(), anyString(), anyList(), anyString(), anyBoolean()
        )).thenReturn(response);

        CompileAndSaveRequestDTO request =
                new CompileAndSaveRequestDTO(
                        "net-1",
                        "source",
                        List.of("taskA"),
                        "NUREON_LANG",
                        true
                );

        mockMvc.perform(post("/api/v1/orchestrator/networks/compile-and-save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(compileAndSaveUseCase).execute(
                "net-1",
                "source",
                List.of("taskA"),
                "NUREON_LANG",
                true
        );
    }

    // -------------------------
    // SEARCH
    // -------------------------

    @Test
    void search_shouldReturnList() throws Exception {
        NetworkSummaryDto summary =
                new NetworkSummaryDto(
                        "id",
                        "name",
                        "v1",
                        "NUREON_LANG",
                        "had",
                        Instant.now(),
                        List.of("task"),
                        List.of("tag")
                );

        when(searchUseCase.search(any()))
                .thenReturn(List.of(summary));

        SearchArtifactRequestDTO request =
                new SearchArtifactRequestDTO(
                        null, null, null, null,
                        "NUREON_LANG",
                        null, null,
                        List.of("tag")
                );

        mockMvc.perform(post("/api/v1/orchestrator/networks/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].artifactId").value("id"));
    }

    // -------------------------
    // GET BY ID
    // -------------------------

    @Test
    void getById_shouldReturnNetwork() throws Exception {
        NetworkSummaryDto summary =
                new NetworkSummaryDto(
                        "id",
                        "name",
                        "v1",
                        "NUREON_LANG",
                        "had",
                        Instant.now(),
                        List.of(),
                        List.of()
                );

        when(getByIdUseCase.getById("id"))
                .thenReturn(summary);

        mockMvc.perform(get("/api/v1/orchestrator/networks/id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artifactId").value("id"));
    }

    // -------------------------
    // DELETE
    // -------------------------

    @Test
    void delete_shouldCallUseCase() throws Exception {
        mockMvc.perform(delete("/api/v1/orchestrator/networks/id"))
                .andExpect(status().isOk());

        verify(deleteUseCase).delete("id");
    }
}
