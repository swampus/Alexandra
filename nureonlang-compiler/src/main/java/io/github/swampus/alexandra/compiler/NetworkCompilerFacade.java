package io.github.swampus.alexandra.compiler;

import io.github.swampus.alexandra.compiler.development.IRDeveloper;
import io.github.swampus.alexandra.compiler.development.expanders.ForExpander;
import io.github.swampus.alexandra.compiler.development.expanders.IfExpander;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.validator.PostCompileValidationService;
import io.github.swampus.alexandra.compiler.validator.ValidationLevel;
import io.github.swampus.alexandra.ir.model.Instruction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * Facade for compiling an IR instruction tree into a {@link NetworkModel}
 * and running post-compilation validation.
 *
 * <p>Supports optional DEVELOPMENT mode that expands generative constructs
 * (FOR/IF/macros/etc.) before compilation.</p>
 *
 * @since 0.9.0
 */
@Slf4j
public final class NetworkCompilerFacade {

    private final PostCompileValidationService validationService;
    private final ValidationLevel level;

    public NetworkCompilerFacade() {
        this(new PostCompileValidationService(), ValidationLevel.SHAPES);
    }

    public NetworkCompilerFacade(ValidationLevel level) {
        this(new PostCompileValidationService(), Objects.requireNonNull(level, "level"));
    }

    public NetworkCompilerFacade(PostCompileValidationService validationService, ValidationLevel level) {
        this.validationService = Objects.requireNonNull(validationService, "validationService");
        this.level = Objects.requireNonNull(level, "level");
    }

    /** Backward-compatible compile (DIRECT mode). */
    public NetworkModel compile(Instruction instructionRoot) {
        return compile(instructionRoot, CompileMode.DIRECT);
    }

    /**
     * Compile with explicit mode.
     *
     * @param instructionRoot root IR instruction
     * @param mode DIRECT or DEVELOPMENT
     */
    public NetworkModel compile(Instruction instructionRoot, CompileMode mode) {

        Objects.requireNonNull(instructionRoot, "instructionRoot");
        Objects.requireNonNull(mode, "mode");

        Instruction rootToCompile = instructionRoot;

        // 🧬 Development phase
        if (mode == CompileMode.DEVELOPMENT) {

            IRDeveloper developer = new IRDeveloper(List.of(
                    new ForExpander(),
                    new IfExpander()
            ));

            List<Instruction> developed =
                    developer.develop(List.of(instructionRoot));

            log.info("Development phase applied: {} instructions", developed.size());

            if (!developed.isEmpty()) {
                // assume single-root programs; adjust if multi-root
                rootToCompile = developed.get(0);
            }
        }

        IRNetworkCompiler compiler = new IRNetworkCompiler();
        NetworkModel model = compiler.compile(rootToCompile);

        validationService.validate(model, level);

        return model;
    }

    public ValidationLevel getValidationLevel() { return level; }
    public PostCompileValidationService getValidationService() { return validationService; }
}
