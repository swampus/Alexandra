package io.github.swampus.alexandra.networkapi.compiler.application.usecase.result;

import io.github.swampus.alexandra.translator.exception.ParseError;

import java.util.List;

public record ParseOutcome(
        boolean valid,
        List<ParseError> errors,
        FailureReason failureReason
) {
    public static ParseOutcome success() {
        return new ParseOutcome(true, List.of(), null);
    }

    public static ParseOutcome syntaxError(List<ParseError> errors) {
        return new ParseOutcome(false, errors, FailureReason.SYNTAX_ERROR);
    }

    public static ParseOutcome internalError() {
        return new ParseOutcome(false, null, FailureReason.INTERNAL_ERROR);
    }

    public enum FailureReason {
        SYNTAX_ERROR,
        INTERNAL_ERROR
    }
}

