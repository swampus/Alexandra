package io.github.swampus.alexandra.compiler;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.validator.PostCompileValidationService;
import io.github.swampus.alexandra.compiler.validator.ValidationLevel;
import io.github.swampus.alexandra.ir.model.Instruction;

import java.util.Objects;

/**
 * Facade for compiling an IR instruction tree into a {@link NetworkModel}
 * and running post-compilation validation.
 *
 * <p>Default validation level is {@link ValidationLevel#SHAPES}.</p>
 *
 * @since 0.9.0
 */
public final class NetworkCompilerFacade {

    private final PostCompileValidationService validationService;
    private final ValidationLevel level;

    /** Default: SHAPES level with a new PostCompileValidationService. */
    public NetworkCompilerFacade() {
        this(new PostCompileValidationService(), ValidationLevel.SHAPES);
    }

    /** Default service + custom level. */
    public NetworkCompilerFacade(ValidationLevel level) {
        this(new PostCompileValidationService(), Objects.requireNonNull(level, "level"));
    }

    /** Full DI constructor. */
    public NetworkCompilerFacade(PostCompileValidationService validationService, ValidationLevel level) {
        this.validationService = Objects.requireNonNull(validationService, "validationService");
        this.level = Objects.requireNonNull(level, "level");
    }

    /**
     * Compiles the given IR into a {@link NetworkModel} and validates it.
     *
     * @param instructionRoot non-null root instruction
     * @return validated model
     */
    public NetworkModel compile(Instruction instructionRoot) {
        Objects.requireNonNull(instructionRoot, "instructionRoot");
        IRNetworkCompiler compiler = new IRNetworkCompiler();
        NetworkModel model = compiler.compile(instructionRoot);
        validationService.validate(model, level);
        return model;
    }

    public ValidationLevel getValidationLevel() { return level; }
    public PostCompileValidationService getValidationService() { return validationService; }
}
