package io.github.swampus.alexandra.dto.shared.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a request to compile a NureonLang source into
 * an internal Alexandra network representation.
 *
 * <p>This DTO is intended to be used by compiler-facing APIs and services.
 * It contains only high-level user intent and compilation preferences.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record CompileRequestDTO(

        /**
         * Human-readable name of the network being compiled.
         */
        String name,

        /**
         * Raw NureonLang source code.
         */
        String source,

        /**
         * Author or owner of the network definition.
         */
        String author,

        /**
         * Optional textual description of the network purpose.
         */
        String description,

        /**
         * Whether a detailed compilation trace is required.
         * <p>
         * If {@code true}, the compiler may return intermediate steps,
         * IR snapshots and diagnostic information.
         */
        boolean compilationTraceRequired,

        /**
         * Whether the compiler is allowed to apply optimizations.
         * <p>
         * If {@code false}, the compiler must preserve the original
         * structure as closely as possible.
         */
        boolean optimizeAllowed

) {

    /**
     * @return {@code true} if at least one optional advanced flag is enabled.
     */
    public boolean hasAdvancedOptionsEnabled() {
        return compilationTraceRequired || optimizeAllowed;
    }
}
