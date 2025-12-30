package io.github.swampus.alexandra.networkapi.orchestrator.web;

import io.github.swampus.alexandra.dto.shared.request.CompileRequestDTO;
import io.github.swampus.alexandra.dto.shared.request.registry.SearchArtifactRequestDTO;
import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.dto.shared.response.NetworkSummaryDto;
import io.github.swampus.alexandra.dto.shared.response.ParseResponseDto;
import io.github.swampus.alexandra.networkapi.orchestrator.application.domain.RegistrySearchQuery;
import io.github.swampus.alexandra.networkapi.orchestrator.application.usecase.*;
import io.github.swampus.alexandra.networkapi.orchestrator.web.dto.CompileAndSaveRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Network Orchestrator",
        description = "High-level orchestration API for compiling, storing and managing neural network artifacts"
)
@RestController
@RequestMapping("/api/v1/orchestrator/networks")
@RequiredArgsConstructor
public class NetworkOrchestratorController {

    private final ValidateNetworkUseCase validateUseCase;
    private final CompileNetworkUseCase compileUseCase;
    private final CompileAndSaveNetworkUseCase compileAndSaveUseCase;
    private final SearchNetworksUseCase searchUseCase;
    private final GetNetworkByIdUseCase getByIdUseCase;
    private final DeleteNetworkUseCase deleteUseCase;

    // -----------------------------------------
    // VALIDATE
    // -----------------------------------------

    @Operation(
            summary = "Validate network source code",
            description = """
                    Performs syntactic and semantic validation of the provided network source code
                    without producing a compiled artifact or persisting any data.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Validation completed successfully",
                            content = @Content(schema = @Schema(implementation = ParseResponseDto.class))
                    )
            }
    )
    @PostMapping("/validate")
    public ParseResponseDto validate(@RequestBody CompileRequestDTO request) {
        return validateUseCase.validate(
                request.source(),
                request.compilationTraceRequired()
        );
    }

    // -----------------------------------------
    // COMPILE (NO SAVE)
    // -----------------------------------------

    @Operation(
            summary = "Compile network source code",
            description = """
                    Compiles the provided network source code into an internal representation
                    without persisting the result in the artifact registry.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Compilation completed",
                            content = @Content(schema = @Schema(implementation = CompileResponseDto.class))
                    )
            }
    )
    @PostMapping("/compile")
    public CompileResponseDto compile(@RequestBody CompileRequestDTO request) {
        return compileUseCase.compile(
                request.source(),
                request.compilationTraceRequired()
        );
    }

    // -----------------------------------------
    // COMPILE + SAVE
    // -----------------------------------------

    @Operation(
            summary = "Compile and persist a network artifact",
            description = """
                    Compiles the provided network source code and persists the resulting artifact
                    in the registry as a new version.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Network successfully compiled and stored",
                            content = @Content(schema = @Schema(implementation = CompileResponseDto.class))
                    )
            }
    )
    @PostMapping("/compile-and-save")
    public CompileResponseDto compileAndSave(
            @RequestBody CompileAndSaveRequestDTO request
    ) {
        return compileAndSaveUseCase.execute(
                request.artifactId(),
                request.source(),
                request.taskNames(),
                request.language(),
                request.compilationTraceRequired()
        );
    }

    // -----------------------------------------
    // SEARCH
    // -----------------------------------------

    @Operation(
            summary = "Search stored network artifacts",
            description = """
                    Searches the artifact registry using the provided query criteria
                    and returns a list of matching network summaries.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Matching networks returned",
                            content = @Content(schema = @Schema(implementation = NetworkSummaryDto.class))
                    )
            }
    )
    @PostMapping("/search")
    public List<NetworkSummaryDto> search(
            @RequestBody SearchArtifactRequestDTO request
    ) {
        RegistrySearchQuery query = new RegistrySearchQuery(
                request.artifactId(),
                request.clusterId(),
                request.taskId(),
                request.version(),
                request.language(),
                request.tags(),
                request.limit(),
                request.offset()
        );

        return searchUseCase.search(query);
    }

    // -----------------------------------------
    // GET BY ID
    // -----------------------------------------

    @Operation(
            summary = "Retrieve a network by artifact ID",
            description = "Returns metadata and summary information for a specific stored network artifact."
    )
    @GetMapping("/{artifactId}")
    public NetworkSummaryDto getById(@PathVariable("artifactId") String artifactId) {
        return getByIdUseCase.getById(artifactId);
    }

    // -----------------------------------------
    // DELETE
    // -----------------------------------------

    @Operation(
            summary = "Delete a stored network artifact",
            description = "Removes the specified network artifact from the registry."
    )
    @DeleteMapping("/{artifactId}")
    public void delete(@PathVariable("artifactId") String artifactId) {
        deleteUseCase.delete(artifactId);
    }
}
