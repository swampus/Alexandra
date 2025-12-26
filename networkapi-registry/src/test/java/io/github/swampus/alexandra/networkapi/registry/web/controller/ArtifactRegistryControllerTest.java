package io.github.swampus.alexandra.networkapi.registry.web.controller;

import io.github.swampus.alexandra.networkapi.registry.application.usecase.crud.DeleteArtifactUseCase;
import io.github.swampus.alexandra.networkapi.registry.application.usecase.crud.GetArtifactUseCase;
import io.github.swampus.alexandra.networkapi.registry.application.usecase.crud.SaveArtifactUseCase;
import io.github.swampus.alexandra.networkapi.registry.application.usecase.crud.SearchArtifactsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ArtifactRegistryController.class)
@ContextConfiguration(classes = ArtifactRegistryController.class)
class ArtifactRegistryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SaveArtifactUseCase save;

    @MockBean
    private GetArtifactUseCase get;

    @MockBean
    private SearchArtifactsUseCase search;

    @MockBean
    private DeleteArtifactUseCase delete;

    @Test
    void shouldSaveArtifact() throws Exception {
        String json = """
        {
          "artifactId": "a1",
          "clusterId": "c1",
          "taskNames": ["t1"],
          "tags": [],
          "language": "IR",
          "version": "v1",
          "payload": "YWJj"
        }
        """;

        mockMvc.perform(post("/api/registry/artifacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artifactId").value("a1"));
    }
}



