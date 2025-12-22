package io.github.swampus.alexandra.networkapi.compiler.application.usecase;

import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxCheckResult;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxError;
import io.github.swampus.alexandra.nureonlang.parser.internal.InternalNureonLangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Application use case responsible for syntactic validation
 * of NureonLang source code.
 *
 * <p>This use case performs syntax checking only and does not
 * compile or transform the source into a network model.</p>
 *
 * <p>No UI-specific formatting or transport concerns are handled here.</p>
 */
public class ParseNueronLangSourceUseCase {

    private static final Logger log =
            LoggerFactory.getLogger(ParseNueronLangSourceUseCase.class);

    private final InternalNureonLangService parser;

    public ParseNueronLangSourceUseCase(InternalNureonLangService parser) {
        this.parser = parser;
    }

    /**
     * Validates the syntax of the provided NureonLang source.
     *
     * @param source NureonLang source code
     * @param traceRequired whether diagnostic trace should be collected
     * @return structured parse result
     */
    public ParseResult parse(String source, boolean traceRequired) {
        try {
            SyntaxCheckResult result = parser.checkSyntax(source);

            if (result.isValid()) {
                return ParseResult.valid();
            }

            return ParseResult.invalid(result.getErrors());

        } catch (Exception e) {
            log.debug("Unexpected exception during syntax validation", e);
            return ParseResult.failure(ParseFailureReason.INTERNAL_ERROR);
        }
    }

    // ===================== RESULT MODEL =====================

    /**
     * Result of syntax validation.
     *
     * <p>This is a pure application-layer result and must not
     * contain UI or transport-specific representations.</p>
     */
    public static final class ParseResult {

        private final boolean valid;
        private final List<SyntaxError> errors;
        private final ParseFailureReason failureReason;

        private ParseResult(
                boolean valid,
                List<SyntaxError> errors,
                ParseFailureReason failureReason
        ) {
            this.valid = valid;
            this.errors = errors == null ? null : List.copyOf(errors);
            this.failureReason = failureReason;
        }

        public static ParseResult valid() {
            return new ParseResult(true, null, null);
        }

        public static ParseResult invalid(List<SyntaxError> errors) {
            return new ParseResult(false, errors, ParseFailureReason.SYNTAX_ERROR);
        }

        public static ParseResult failure(ParseFailureReason reason) {
            return new ParseResult(false, null, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public List<SyntaxError> getErrors() {
            return errors;
        }

        public ParseFailureReason getFailureReason() {
            return failureReason;
        }
    }

    /**
     * High-level classification of parse failures.
     */
    public enum ParseFailureReason {
        SYNTAX_ERROR,
        INTERNAL_ERROR
    }
}
