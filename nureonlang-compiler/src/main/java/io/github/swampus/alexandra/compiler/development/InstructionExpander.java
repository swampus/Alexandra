package io.github.swampus.alexandra.compiler.development;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;

import java.util.List;

public interface InstructionExpander {
    boolean supports(OpCode op);
    List<Instruction> expand(Instruction instr, DevelopmentContext ctx);
}

