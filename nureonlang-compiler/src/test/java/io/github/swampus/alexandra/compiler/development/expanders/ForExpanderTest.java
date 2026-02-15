package io.github.swampus.alexandra.compiler.development.expanders;

import io.github.swampus.alexandra.compiler.development.DevelopmentContext;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForExpanderTest {

    @Test
    void shouldExpandSimpleLoop() {

        Instruction layer = Instruction.builder()
                .op(OpCode.LAYER)
                .name("dense[i]")
                .size(32)
                .build();

        Instruction loop = Instruction.builder()
                .op(OpCode.FOR)
                .var("i")
                .fromVal(1)
                .toVal(3)
                .body(List.of(layer))
                .build();

        DevelopmentContext ctx = new DevelopmentContext();

        ForExpander expander = new ForExpander();

        List<Instruction> expanded = expander.expand(loop, ctx);

        assertEquals(3, expanded.size());

        assertEquals("dense1", expanded.get(0).getName());
        assertEquals("dense2", expanded.get(1).getName());
        assertEquals("dense3", expanded.get(2).getName());
    }

    @Test
    void shouldReturnEmptyWhenBodyNull() {

        Instruction loop = Instruction.builder()
                .op(OpCode.FOR)
                .var("i")
                .fromVal(1)
                .toVal(3)
                .body(null)
                .build();

        DevelopmentContext ctx = new DevelopmentContext();
        ForExpander expander = new ForExpander();

        List<Instruction> expanded = expander.expand(loop, ctx);

        assertTrue(expanded.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenFromGreaterThanTo() {

        Instruction layer = Instruction.builder()
                .op(OpCode.LAYER)
                .name("dense[i]")
                .build();

        Instruction loop = Instruction.builder()
                .op(OpCode.FOR)
                .var("i")
                .fromVal(5)
                .toVal(2)
                .body(List.of(layer))
                .build();

        DevelopmentContext ctx = new DevelopmentContext();
        ForExpander expander = new ForExpander();

        List<Instruction> expanded = expander.expand(loop, ctx);

        assertTrue(expanded.isEmpty());
    }
}
