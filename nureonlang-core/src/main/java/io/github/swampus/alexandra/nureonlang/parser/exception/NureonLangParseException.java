package io.github.swampus.alexandra.nureonlang.parser.exception;

public class NureonLangParseException extends RuntimeException {
    public NureonLangParseException(String message) {
        super(message);
    }
    public NureonLangParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
