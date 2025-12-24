package io.github.swampus.alexandra.networkapiregistry.web.controller;

import io.github.swampus.alexandra.dto.shared.request.registry.SearchArtifactRequestDTO;
import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.SearchArtifactsUseCase;
import io.github.swampus.alexandra.networkapiregistry.domain.model.ArtifactMetadata;
import io.github.swampus.alexandra.networkapiregistry.infrastructure.storage.memory.InMemoryArtifactIndexStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchArtifactsUseCaseTest {

    private InMemoryArtifactIndexStore indexStore;
    private SearchArtifactsUseCase useCase;

    @BeforeEach
    void setUp() {
        indexStore = new InMemoryArtifactIndexStore();
        useCase = new SearchArtifactsUseCase(indexStore);
    }

    @Test
    void shouldFindByArtifactId() {
        ArtifactMetadata meta = sample("a1", "c1", "t1");
        indexStore.upsert(meta);

        SearchArtifactRequestDTO req =
                new SearchArtifactRequestDTO("a1", null, null, null, null, null, null);

        List<ArtifactMetadata> result = useCase.execute(req);

        assertEquals(1, result.size());
        assertEquals("a1", result.get(0).getArtifactId());
    }

    @Test
    void shouldFindByCluster() {
        indexStore.upsert(sample("a1", "c1", "t1"));
        indexStore.upsert(sample("a2", "c1", "t2"));

        SearchArtifactRequestDTO req =
                new SearchArtifactRequestDTO(null, "c1", null, null, null, null, null);

        assertEquals(2, useCase.execute(req).size());
    }

    private ArtifactMetadata sample(String id, String cluster, String task) {
        return new ArtifactMetadata(
                id, cluster, task, "v1", "IR", Instant.now()
        );
    }
}
