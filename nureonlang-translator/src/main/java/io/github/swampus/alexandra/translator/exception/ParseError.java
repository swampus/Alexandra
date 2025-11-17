package io.github.swampus.alexandra.translator.exception;


import lombok.Data;

@Data
public class ParseError {
    private final String message;
    private final int line;
    private final int column;
}
