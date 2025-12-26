package io.github.swampus.alexandra.dto.shared.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.swampus.alexandra.dto.shared.ParseErrorDto;
import io.github.swampus.alexandra.dto.shared.network.NNetworkDto;

import java.util.List;

/**
 * Result of a neural network compilation request.
 *
 * <p>This DTO represents the outcome of compiling a network definition
 * (e.g. NureonLang source) into an internal executable or serializable form.
 *
 * <p>The response is intentionally designed to support both successful
 * and failed compilation scenarios without relying on exceptions.
 *
 * <p>Typical usage scenarios:
 * <ul>
 *   <li>Persisting compiled models in an artifact registry.</li>
 *   <li>Returning detailed diagnostics to CLI or UI clients.</li>
 *   <li>Chaining compilation with training or execution stages.</li>
 * </ul>
 *
 * <p>All fields are optional and their presence depends on the compilation
 * outcome and configuration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record CompileResponseDto(

        /**
         * Serialized internal representation of the compiled model.
         *
         * <p>This field typically contains a stable intermediate representation (IR)
         * expressed as JSON. It can be safely persisted, transferred between services
         * or used for reproducible builds.
         *
         * <p>May be {@code null} if compilation failed before producing a valid model.
         */
        String modelJson,

        /**
         * High-level compilation error message.
         *
         * <p>This field is intended for coarse-grained error reporting
         * (e.g. "Syntax error", "Unsupported construct").
         *
         * <p>For fine-grained diagnostics such as line/column errors,
         * refer to {@link #parseErrors}.
         *
         * <p>Must be {@code null} if compilation completed successfully.
         */
        String error,

        /**
         * Optional human-readable compilation trace.
         *
         * <p>This may include intermediate compilation steps, optimization decisions,
         * macro expansions or other diagnostic information useful for debugging
         * and advanced inspection.
         *
         * <p>Typically enabled in debug or verbose modes.
         */
        String compilationTrace,

        /**
         * List of parsing or validation errors detected during compilation.
         *
         * <p>Each entry usually contains precise source location information
         * (line, column) and a detailed error description.
         *
         * <p>If this list is non-empty, compilation is considered unsuccessful,
         * even if a partial model was produced.
         */
        List<ParseErrorDto> parseErrors,

        /**
         * Compiled network model representation.
         *
         * <p>This field contains the fully constructed network object
         * that can be passed to training or execution components.
         *
         * <p>May be {@code null} if compilation failed or was aborted early.
         */
        NNetworkDto model,

        /**
         * Serialized binary payload representing the compiled artifact.
         *
         * <p>This payload is intended for persistent storage in the artifact registry
         * and for transmission between services.
         *
         * <p>The exact format is compiler-defined but must be stable and
         * backward-compatible within the same major version.
         *
         * <p>May be {@code null} if compilation failed.
         */
        byte[] payload

        ) {

    /**
     * Canonical constructor with defensive copying.
     *
     * <p>Ensures immutability and protects internal state from
     * accidental external modification.
     */
    public CompileResponseDto {
        parseErrors = copyOrNull(parseErrors);
        payload = payload == null ? null : payload.clone();
    }

    private static <T> List<T> copyOrNull(List<T> source) {
        return source == null ? null : List.copyOf(source);
    }


    /**
     * Indicates whether compilation completed successfully.
     *
     * <p>Compilation is considered successful if:
     * <ul>
     *   <li>No top-level error message is present.</li>
     *   <li>No parsing or validation errors were reported.</li>
     * </ul>
     *
     * <p>This method is intended as a convenience helper for Java clients.
     * API consumers should not infer success by inspecting individual fields.
     *
     * @return {@code true} if compilation succeeded without errors
     */
    public boolean isSuccessful() {
        return error == null && (parseErrors == null || parseErrors.isEmpty());
    }

    /**
     * Indicates whether a compilation trace is present.
     *
     * @return {@code true} if a non-empty compilation trace was produced
     */
    public boolean hasTrace() {
        return compilationTrace != null && !compilationTrace.isEmpty();
    }
}
