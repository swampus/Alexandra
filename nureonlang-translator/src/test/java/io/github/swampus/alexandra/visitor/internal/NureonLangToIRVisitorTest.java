package io.github.swampus.alexandra.visitor.internal;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.visitor.NureonLangToIRVisitor;
import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NureonLangToIRVisitorTest {
    Instruction parseAndVisit(String code) {
        NureonLangLexer lexer = new NureonLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);
        NureonLangParser.ProgramContext ctx = parser.program();
        return new NureonLangToIRVisitor().visit(ctx);
    }

    @Test
    void testLayerAnon() {
        String code = "LAYER INPUT name=tokens size=512";
        Instruction ir = parseAndVisit(code);
        assertEquals(OpCode.LAYER, ir.getBody().get(0).getOp());
        assertEquals("INPUT", ir.getBody().get(0).getType());
        assertEquals("tokens", ir.getBody().get(0).getName());
        assertEquals(512, ir.getBody().get(0).getSize());
    }

    @Test
    void testFullProgram() {
        String code = """
        BEGIN
            LAYER INPUT name=tokens size=512
            LAYER OUTPUT name=probs size=10 activation=softmax
            CONNECT tokens -> probs
        END
        FILE "weights.bin"
        """;
        Instruction programIR = parseAndVisit(code);
        assertEquals(OpCode.PROGRAM, programIR.getOp());
        assertFalse(programIR.getBody().isEmpty());
    }

    @Test
    void testBlockWithLayers() {
        String code = """
        BEGIN
            LAYER INPUT name=tok size=128
            LAYER DENSE name=hidden size=64 activation=relu
            LAYER OUTPUT name=out size=10
        END
    """;
        Instruction program = parseAndVisit(code);
        Instruction block = program.getBody().get(0);
        assertEquals(OpCode.BLOCK, block.getOp());
        assertEquals(3, block.getBody().size());

        Instruction first = block.getBody().get(0);
        assertEquals(OpCode.LAYER, first.getOp());
        assertEquals("INPUT", first.getType());
        assertEquals("tok", first.getName());
        assertEquals(128, first.getSize());
    }

    @Test
    void testConnect() {
        String code = """
        LAYER INPUT name=tok size=32
        LAYER DENSE name=hid size=8
        LAYER OUTPUT name=out size=2
        CONNECT tok -> hid
        CONNECT hid -> out
    """;
        Instruction program = parseAndVisit(code);
        Instruction connect1 = program.getBody().get(3);
        assertEquals(OpCode.CONNECT, connect1.getOp());
        assertEquals("tok", connect1.getFrom());
        assertEquals("hid", connect1.getTo());
    }

    @Test
    void testIfElse() {
        String code = """
        IF x > 0
        BEGIN
            LAYER POS name=pos size=1
        END
        ELSE
        BEGIN
            LAYER NEG name=neg size=1
        END
    """;
        Instruction program = parseAndVisit(code);
        Instruction ifInstr = program.getBody().get(0);
        assertEquals(OpCode.IF, ifInstr.getOp());
        assertEquals(OpCode.CONDITION, ifInstr.getCond().getOp());

        assertEquals(OpCode.CONDITION, ifInstr.getCond().getOp());
        assertTrue(String.valueOf(ifInstr.getCond().getExpr()).contains("x>0"));
        assertEquals(OpCode.BLOCK, ifInstr.getBody().get(0).getOp());
        assertEquals(OpCode.BLOCK, ifInstr.getBody().get(1).getOp());
    }

    @Test
    void testForLoop() {
        String code = """
        FOR i FROM 1 TO 5
        BEGIN
            LAYER DENSE name=block size=16
        END
    """;
        Instruction program = parseAndVisit(code);
        Instruction forInstr = program.getBody().get(0);
        assertEquals(OpCode.FOR, forInstr.getOp());
        assertEquals("i", forInstr.getVar());
        assertEquals("1", forInstr.getFromVal());
        assertEquals("5", forInstr.getToVal());
        assertEquals(OpCode.BLOCK, forInstr.getBody().get(0).getOp());
    }

    @Test
    void testModuleDefAndCall() {
        String code = """
        MODULE Foo
        BEGIN
            LAYER DENSE name=m size=8
        END
        Foo
    """;
        Instruction program = parseAndVisit(code);
        Instruction module = program.getBody().get(0);
        assertEquals(OpCode.MODULE_DEF, module.getOp());
        assertEquals("Foo", module.getName());
        Instruction call = program.getBody().get(1);
        assertEquals(OpCode.MODULE_CALL, call.getOp());
        assertEquals("Foo", call.getName());
    }

    @Test
    void testMacroDefAndCall() {
        String code = """
        DEFINE Mac(x, y)
        BEGIN
            LAYER DENSE name=mac size=x
        END
        Mac(16, 32)
    """;
        Instruction program = parseAndVisit(code);
        Instruction macro = program.getBody().get(0);
        assertEquals(OpCode.MACRO_DEF, macro.getOp());
        assertEquals("Mac", macro.getName());
        assertTrue(((List<?>)macro.getMeta().get("params")).contains("x"));
        Instruction call = program.getBody().get(1);
        assertEquals(OpCode.MACRO_CALL, call.getOp());
        assertEquals("Mac", call.getName());
        assertTrue(((List<?>)call.getMeta().get("args")).contains("16"));
    }

    @Test
    void testLetStmt() {
        String code = "LET x = 1234;";
        Instruction program = parseAndVisit(code);
        Instruction let = program.getBody().get(0);
        assertEquals(OpCode.LET, let.getOp());
        assertEquals("x", let.getVar());
        assertEquals("1234", let.getExpr());
    }

    @Test
    void testExpandStmt() {
        String code = """
        EXPAND GROUP = Cyclic20X
        BEGIN
            LAYER DENSE name=proj size=20
        END
    """;
        Instruction program = parseAndVisit(code);
        Instruction expand = program.getBody().get(0);
        assertEquals(OpCode.EXPAND, expand.getOp());
        assertTrue(expand.getTarget().contains("Cyclic20X"));
        assertEquals(OpCode.BLOCK, expand.getBody().get(0).getOp());
    }

    @Test
    void testFileRef() {
        String code = """
    LAYER INPUT name=stub size=1
    FILE "my_weights.bin"
    """;
        Instruction program = parseAndVisit(code);
        Instruction layer = program.getBody().get(0);
        Instruction file = program.getBody().get(1);
        assertEquals(OpCode.LAYER, layer.getOp());
        assertEquals(OpCode.FILE, file.getOp());
        assertEquals("my_weights.bin", file.getPath());
    }


    @Test
    void testMiniModel() {
        String code = """
        BEGIN
            LAYER INPUT name=tokens size=512
            LAYER DENSE name=block size=128 activation=relu
            CONNECT tokens -> block
        END
        FILE "weights_gpt.bin"
    """;
        Instruction program = parseAndVisit(code);
        assertEquals(OpCode.PROGRAM, program.getOp());
        assertEquals(OpCode.BLOCK, program.getBody().get(0).getOp());
        assertEquals(OpCode.FILE, program.getBody().get(1).getOp());
    }
}