package io.github.swampus.alexandra.dto.shared.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents the result of a source parsing or validation operation.
 * <p>
 * Used to indicate whether the provided input is syntactically valid
 * and to return a high-level error message if it is not.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ParseResponseDto(

        /**
         * Indicates whether the input source is valid.
         */
        boolean valid,

        /**
         * Optional high-level error message if parsing failed.
         */
        String error

) {

    /**
     * @return {@code true} if parsing failed and an error message is present.
     */
    public boolean hasError() {
        return !valid && error != null && !error.isEmpty();
    }
}
