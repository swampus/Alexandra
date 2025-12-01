package io.github.swampus.alexandra.dto.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a single parsing or validation error.
 * <p>
 * Used by parsing and compilation responses to report
 * precise source-level issues.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ParseErrorDto(

        /**
         * Human-readable error description.
         */
        String message,

        /**
         * Line number in the source where the error occurred.
         * <p>
         * Line numbering is expected to be 1-based.
         */
        int line

) {

    /**
     * @return {@code true} if this error refers to a concrete source line.
     */
    public boolean hasValidLineReference() {
        return line > 0;
    }
}
