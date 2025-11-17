package io.github.swampus.alexandra.nureonlang.parser.impl;

public class SyntaxError {
    private final int line;
    private final int charPositionInLine;
    private final String message;

    public SyntaxError(int line, int charPositionInLine, String message) {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.message = message;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Line " + line + ":" + charPositionInLine + " " + message;
    }
}