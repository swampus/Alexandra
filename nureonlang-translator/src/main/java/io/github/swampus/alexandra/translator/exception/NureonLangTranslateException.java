package io.github.swampus.alexandra.translator.exception;

import lombok.Getter;

import java.util.List;

public class NureonLangTranslateException extends RuntimeException {

    @Getter
    private final List<ParseError> errors;

    public NureonLangTranslateException(List<ParseError> errors) {
        super(errors.isEmpty() ? "Unknown parse error" : errors.get(0).getMessage());
        this.errors = errors;
    }

}
