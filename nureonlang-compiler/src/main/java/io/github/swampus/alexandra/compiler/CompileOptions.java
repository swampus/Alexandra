package io.github.swampus.alexandra.compiler;

public class CompileOptions {

    private final CompileMode mode;

    public CompileOptions(CompileMode mode) {
        this.mode = mode;
    }

    public CompileMode getMode() {
        return mode;
    }

    public static CompileOptions direct() {
        return new CompileOptions(CompileMode.DIRECT);
    }

    public static CompileOptions development() {
        return new CompileOptions(CompileMode.DEVELOPMENT);
    }
}

