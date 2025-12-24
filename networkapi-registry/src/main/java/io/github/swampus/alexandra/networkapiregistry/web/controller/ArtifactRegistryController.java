package io.github.swampus.alexandra.networkapiregistry.web.controller;

import io.github.swampus.alexandra.dto.shared.request.registry.SaveArtifactRequestDTO;
import io.github.swampus.alexandra.dto.shared.request.registry.SearchArtifactRequestDTO;
import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.DeleteArtifactUseCase;
import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.GetArtifactUseCase;
import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.SaveArtifactUseCase;
import io.github.swampus.alexandra.networkapiregistry.application.usecase.crud.SearchArtifactsUseCase;
import io.github.swampus.alexandra.networkapiregistry.domain.model.ArtifactMetadata;
import io.github.swampus.alexandra.networkapiregistry.domain.model.StoredArtifact;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/registry")
@Tag(
        name = "Artifact Registry",
        description = "Low-level API for storing and retrieving compiled network artifacts"
)
@RequiredArgsConstructor
public class ArtifactRegistryController {

    private final SaveArtifactUseCase save;
    private final GetArtifactUseCase get;
    private final SearchArtifactsUseCase search;
    private final DeleteArtifactUseCase delete;

    // ---------- SAVE ----------

    @Operation(summary = "Store or replace an artifact")
    @PostMapping("/artifacts")
    public Map<String, Object> save(
            @RequestBody SaveArtifactRequestDTO req
    ) {
        ArtifactMetadata metadata = new ArtifactMetadata(
                req.artifactId(),
                req.clusterId(),
                extractSingleTask(req.taskNames()),
                req.version(),
                req.language(),
                Instant.now()
        );

        save.execute(
                new StoredArtifact(metadata, req.payload())
        );

        return Map.of(
                "status", "ok",
                "artifactId", metadata.getArtifactId()
        );
    }

    // ---------- GET ----------

    @Operation(summary = "Get artifact by ID")
    @GetMapping("/artifacts/{artifactId}")
    public Map<String, Object> get(
            @PathVariable String artifactId
    ) {
        var artifact = get.execute(artifactId);

        return Map.of(
                "metadata", artifact.getMetadata(),
                "payloadBase64",
                Base64.getEncoder().encodeToString(artifact.getPayload())
        );
    }

    // ---------- SEARCH ----------

    @Operation(summary = "Search artifacts")
    @PostMapping("/artifacts/search")
    public List<ArtifactMetadata> search(
            @RequestBody SearchArtifactRequestDTO request
    ) {
        return search.execute(request);
    }

    // ---------- DELETE ----------

    @Operation(summary = "Delete artifact")
    @DeleteMapping("/artifacts/{artifactId}")
    public Map<String, Object> delete(
            @PathVariable String artifactId
    ) {
        delete.execute(artifactId);
        return Map.of("status", "ok");
    }

    // ---------- helpers ----------

    private static String extractSingleTask(List<String> taskNames) {
        if (taskNames == null || taskNames.isEmpty()) {
            return "unknown";
        }
        if (taskNames.size() > 1) {
            throw new IllegalArgumentException(
                    "Registry currently supports single taskId per artifact");
        }
        return taskNames.get(0);
    }
}

