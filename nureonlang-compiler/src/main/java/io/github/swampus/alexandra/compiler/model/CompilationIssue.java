package io.github.swampus.alexandra.compiler.model;

public record CompilationIssue(
        Severity severity,
        String message
) {
    public enum Severity {
        WARNING,
        ERROR
    }
}

