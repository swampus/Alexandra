package io.github.swampus.alexandra.compiler.handlers.providers;

import io.github.swampus.alexandra.ir.model.Instruction;

public interface InstructionProvider {
    Instruction getInstructionByName(String name);
}
