package io.github.swampus.alexandra.dto.shared.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.swampus.alexandra.dto.shared.ParseErrorDto;
import io.github.swampus.alexandra.dto.shared.network.NNetworkDto;

import java.util.List;

/**
 * Represents the result of a compilation request.
 * <p>
 * This response combines:
 * <ul>
 *     <li>Serialized model representation (JSON).</li>
 *     <li>High-level error message, if any.</li>
 *     <li>Optional detailed compilation trace.</li>
 *     <li>Optional list of parsing errors.</li>
 *     <li>Optional compiled network model.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record CompileResponseDto(

        /**
         * Serialized internal model representation as JSON.
         * <p>
         * This is typically a stable IR that can be persisted or
         * transferred between services.
         */
        String modelJson,

        /**
         * High-level error message, if compilation failed.
         * <p>
         * For detailed parsing issues, see {@link #parseErrors}.
         */
        String error,

        /**
         * Optional human-readable compilation trace.
         * <p>
         * May include intermediate steps, decisions and diagnostics.
         */
        String compilationTrace,

        /**
         * List of parsing or validation errors detected during compilation.
         * <p>
         * If non-empty, compilation may have failed or produced a
         * partial result depending on the error severity.
         */
        List<ParseErrorDto> parseErrors,

        /**
         * Compiled network model, if compilation succeeded
         * or produced a usable partial result.
         */
        NNetworkDto model
) {

    /**
     * Canonical constructor with defensive copying of collections.
     */
    public CompileResponseDto {
        parseErrors = copyOrNull(parseErrors);
    }

    private static <T> List<T> copyOrNull(List<T> source) {
        return source == null ? null : List.copyOf(source);
    }

    /**
     * @return {@code true} if compilation completed without a top-level error
     * and no parse errors were reported.
     */
    public boolean isSuccessful() {
        return error == null && (parseErrors == null || parseErrors.isEmpty());
    }

    /**
     * @return {@code true} if a compilation trace is present.
     */
    public boolean hasTrace() {
        return compilationTrace != null && !compilationTrace.isEmpty();
    }
}
