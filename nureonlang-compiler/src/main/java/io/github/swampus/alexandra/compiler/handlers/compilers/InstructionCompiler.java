package io.github.swampus.alexandra.compiler.handlers.compilers;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.ir.model.Instruction;

import java.util.Map;

public interface InstructionCompiler {
    void compile(Instruction instr, NetworkModel model, Map<String, Layer> layers);
}
