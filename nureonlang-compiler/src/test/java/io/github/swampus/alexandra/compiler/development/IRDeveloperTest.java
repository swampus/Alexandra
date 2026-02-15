package io.github.swampus.alexandra.compiler.development;

import io.github.swampus.alexandra.compiler.development.expanders.ForExpander;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IRDeveloperTest {

    @Test
    void shouldDevelopProgram() {

        Instruction layer = Instruction.builder()
                .op(OpCode.LAYER)
                .name("dense[i]")
                .build();

        Instruction loop = Instruction.builder()
                .op(OpCode.FOR)
                .var("i")
                .fromVal(1)
                .toVal(2)
                .body(List.of(layer))
                .build();

        IRDeveloper dev = new IRDeveloper(List.of(new ForExpander()));

        List<Instruction> result = dev.develop(List.of(loop));

        assertEquals(2, result.size());
        assertEquals("dense1", result.get(0).getName());
        assertEquals("dense2", result.get(1).getName());
    }
}
