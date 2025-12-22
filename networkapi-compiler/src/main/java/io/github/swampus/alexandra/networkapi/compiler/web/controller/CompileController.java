package io.github.swampus.alexandra.networkapi.compiler.web.controller;

import io.github.swampus.alexandra.compiler.validator.ValidationLevel;
import io.github.swampus.alexandra.dto.shared.request.CompileRequestDTO;
import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import io.github.swampus.alexandra.dto.shared.response.ParseResponseDto;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.CompileNetworkUseCase;
import io.github.swampus.alexandra.networkapi.compiler.application.usecase.ParseNueronLangSourceUseCase;
import io.github.swampus.alexandra.networkapi.compiler.web.controller.mapper.CompileResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing compilation and parsing endpoints for neural network sources.
 *
 * <p>This controller acts as a thin HTTP adapter and delegates all business logic
 * to application-level use cases.</p>
 *
 * <p>The controller is intentionally stateless and deterministic:
 * identical requests produce identical responses.</p>
 */
@Tag(
        name = "Neural Network Compiler",
        description = "Compilation and parsing of NureonLang neural network definitions"
)
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/compiler")
public class CompileController {

    private final CompileNetworkUseCase compileNetworkUseCase;
    private final ParseNueronLangSourceUseCase parseNueronLangSourceUseCase;
    private final CompileResponseMapper responseMapper;

    @Operation(
            summary = "Compile neural network source",
            description = """
                Compiles a neural network described in NureonLang into an internal network model.

                The compilation pipeline includes:
                - syntax parsing
                - semantic and structural validation
                - macro expansion
                - conditional resolution
                - network graph construction

                The operation is deterministic and stateless.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Compilation successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompileResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Compilation failed due to syntax or semantic errors",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompileResponseDto.class)
                    )
            )
    })
    @PostMapping("/compile")
    public ResponseEntity<CompileResponseDto> compile(
            @RequestBody CompileRequestDTO request
    ) {
        var result = compileNetworkUseCase.compile(
                request.source(),
                request.compilationTraceRequired(),
                ValidationLevel.STRUCTURAL
        );

        return ResponseEntity.ok(responseMapper.toCompileDto(result));
    }

    @Operation(
            summary = "Parse neural network source",
            description = """
                Parses a NureonLang neural network definition without performing full compilation.

                This endpoint is intended for:
                - syntax validation
                - early error detection
                - IDE or UI integration
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Source is syntactically valid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ParseResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Syntax errors detected during parsing",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ParseResponseDto.class)
                    )
            )
    })
    @PostMapping("/parse")
    public ResponseEntity<ParseResponseDto> parse(
            @RequestBody CompileRequestDTO request
    ) {
        var result = parseNueronLangSourceUseCase.parse(
                request.source(),
                request.compilationTraceRequired()
        );

        return result.isValid()
                ? ResponseEntity.ok(responseMapper.toParseDto(result))
                : ResponseEntity.badRequest().body(responseMapper.toParseDto(result));
    }
}
