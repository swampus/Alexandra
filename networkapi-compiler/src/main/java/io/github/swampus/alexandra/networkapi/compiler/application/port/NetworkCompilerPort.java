package io.github.swampus.alexandra.networkapi.compiler.application.port;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.ir.model.Instruction;

public interface NetworkCompilerPort {
    CompilationOutput compile(Instruction instruction, boolean traceRequired);

    record CompilationOutput(
            NetworkModel model,
            String trace
    ) {}
}
