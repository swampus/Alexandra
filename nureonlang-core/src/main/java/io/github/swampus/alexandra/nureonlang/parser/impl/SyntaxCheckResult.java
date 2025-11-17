package io.github.swampus.alexandra.nureonlang.parser.impl;

import java.util.List;


public class SyntaxCheckResult {
    private final boolean valid;
    private final List<SyntaxError> errors;
    public SyntaxCheckResult(boolean valid, List<SyntaxError> errors) {
        this.valid = valid;
        this.errors = errors;
    }
    public boolean isValid() {
        return valid;
    }

    public List<SyntaxError> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "SyntaxCheckResult{" +
                "valid=" + valid +
                "\n, errors=" + errors +
                '}';
    }

}
