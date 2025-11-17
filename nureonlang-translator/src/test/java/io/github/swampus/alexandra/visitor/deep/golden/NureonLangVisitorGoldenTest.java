package io.github.swampus.alexandra.visitor.deep.golden;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.visitor.NureonLangToIRVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NureonLangVisitorGoldenTest {

    Instruction parseAndVisit(String code) {
        NureonLangLexer lexer = new NureonLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);
        NureonLangParser.ProgramContext ctx = parser.program();
        return new NureonLangToIRVisitor().visit(ctx);
    }

    @Test
    void goldenTest_DeepMadGodHypernetwork() {
        String code = """
        MODULE madgod_dim
        BEGIN
            FOR d FROM 0 TO 12
            BEGIN
                LAYER DENSE name=dimension_[d] size=666 activation=madness
                CONNECT transcend_[(d+1)*13] -> dimension_[d]
            END
        END

        BEGIN
            LAYER INPUT name=universe_in shape=(66, 66, 66)
            LET i = 0;
            FOR i FROM 0 TO 999
            BEGIN
                LAYER DENSE name=transcend_[i] size=2048 activation=chaos
                CONNECT universe_in -> transcend_[i]
            END

            EXPAND GROUP = Cyclic20X
            BEGIN
                madgod_dim
            END

            LAYER OUTPUT name=divine_utterance size=1 activation=apocalypse
            CONNECT dimension_12 -> divine_utterance
        END
        FILE "madgod_weights.bin"
    """;

        Instruction ir = parseAndVisit(code);

        // Топ-левел: [MODULE_DEF, BLOCK, FILE]
        assertEquals(OpCode.PROGRAM, ir.getOp());
        assertEquals(3, ir.getBody().size());

        Instruction module = ir.getBody().stream().filter(i -> i.getOp() == OpCode.MODULE_DEF).findFirst().orElseThrow();
        Instruction block  = ir.getBody().stream().filter(i -> i.getOp() == OpCode.BLOCK).findFirst().orElseThrow();
        Instruction file   = ir.getBody().stream().filter(i -> i.getOp() == OpCode.FILE).findFirst().orElseThrow();

        // --- MODULE_DEF madgod_dim ---
        assertEquals("madgod_dim", module.getName());
        // Внутри модуля блок с FOR по d
        Instruction moduleBlock = module.getBody().get(0);
        Instruction moduleFor = moduleBlock.getBody().stream().filter(i -> i.getOp() == OpCode.FOR).findFirst().orElseThrow();
        assertEquals("d", moduleFor.getVar());
        assertEquals("0", moduleFor.getFromVal());
        assertEquals("12", moduleFor.getToVal());
        assertAny(moduleFor, i -> i.getOp() == OpCode.LAYER && i.getName().contains("dimension_[d]"));
        assertAny(moduleFor, i -> i.getOp() == OpCode.CONNECT && i.getFrom().contains("transcend_[(d+1)*13]"));

        // --- FILE ---
        assertEquals(OpCode.FILE, file.getOp());
        assertTrue(file.getPath().contains("madgod_weights.bin"));

        // --- BLOCK ---
        // INPUT layer
        assertAny(block, i -> i.getOp() == OpCode.LAYER && "universe_in".equals(i.getName()));
        // LET i = 0
        assertAny(block, i -> i.getOp() == OpCode.LET && "i".equals(i.getVar()) && "0".equals(i.getExpr()));
        // FOR i FROM 0 TO 999 c LAYER transcend_[i] и CONNECT
        assertAny(block, i ->
                i.getOp() == OpCode.FOR &&
                        "i".equals(i.getVar()) &&
                        "0".equals(String.valueOf(i.getFromVal())) &&
                        "999".equals(String.valueOf(i.getToVal())) &&
                        i.getBody() != null &&
                        // Ищем во вложенном BLOCK
                        i.getBody().stream().anyMatch(b ->
                                b.getOp() == OpCode.BLOCK &&
                                        b.getBody() != null &&
                                        b.getBody().stream().anyMatch(nested ->
                                                nested.getOp() == OpCode.LAYER && nested.getName().contains("transcend_[i]")
                                        )
                        )
        );
        assertAny(block, i ->
                        i.getOp() == OpCode.FOR &&
                                i.getBody() != null &&
                                i.getBody().stream().anyMatch(b ->
                                        b.getOp() == OpCode.BLOCK &&
                                                b.getBody() != null &&
                                                b.getBody().stream().anyMatch(conn ->
                                                        conn.getOp() == OpCode.CONNECT &&
                                                                conn.getFrom() != null &&
                                                                conn.getFrom().contains("universe_in")
                                                )
                                ));

        // --- EXPAND GROUP = Cyclic20X ---
        Instruction expand = block.getBody().stream().filter(i -> i.getOp() == OpCode.EXPAND).findFirst().orElseThrow();
        assertTrue(expand.getTarget().contains("Cyclic20X") || expand.getTarget().contains("GROUP=Cyclic20X"));
        assertNotNull(expand.getBody());
        Instruction expandBlock = expand.getBody().get(0);

        // Внутри EXPAND — MODULE_CALL madgod_dim
        assertAny(expandBlock, i -> i.getOp() == OpCode.MODULE_CALL && "madgod_dim".equals(i.getName()));

        // LAYER OUTPUT и CONNECT dimension_12 -> divine_utterance
        assertAny(block, i -> i.getOp() == OpCode.LAYER && "divine_utterance".equals(i.getName()));
        assertAny(block, i -> i.getOp() == OpCode.CONNECT && "dimension_12".equals(i.getFrom()) && "divine_utterance".equals(i.getTo()));
    }

    // --- Вспомогательный метод для поиска любого совпадения в глубину ---
    private void assertAny(Instruction instr, java.util.function.Predicate<Instruction> predicate) {
        if (instr == null) fail("Instruction is null");
        if (predicate.test(instr)) return;
        if (instr.getBody() != null) {
            for (Instruction c : instr.getBody()) {
                try {
                    assertAny(c, predicate);
                    return;
                } catch (AssertionError e) { /* continue */ }
            }
        }
        throw new AssertionError("Did not find expected node in IR: " + predicate);
    }

    private boolean hasDeepLayer(Instruction instr, String nameSubstr) {
        if (instr.getOp() == OpCode.LAYER && instr.getName().contains(nameSubstr)) return true;
        if (instr.getBody() != null) for (Instruction i : instr.getBody())
            if (hasDeepLayer(i, nameSubstr)) return true;
        return false;
    }

}
