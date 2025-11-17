package io.github.swampus.alexandra.mainflow;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import io.github.swampus.alexandra.visitor.NureonLangToIRVisitor;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class NureonLangVisitorMainflowTest {

    private Instruction parseAndVisit(String code) {
        CharStream input = CharStreams.fromString(code);
        NureonLangLexer lexer = new NureonLangLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);
        ParseTree tree = parser.program();
        NureonLangToIRVisitor visitor = new NureonLangToIRVisitor();
        return visitor.visit(tree);
    }

    @Test
    void testAdditionNetwork() {
        String code = """
            BEGIN
                LAYER INPUT name=a size=1
                LAYER INPUT name=b size=1
                LAYER DENSE name=add size=1 activation=linear
                LAYER OUTPUT name=out size=1
                CONNECT a -> add
                CONNECT b -> add
                CONNECT add -> out
            END
            FILE "add_weights.bin"
            """;
        Instruction program = parseAndVisit(code);

        assertEquals(OpCode.PROGRAM, program.getOp());
        assertEquals(2, program.getBody().size());
        Instruction block = program.getBody().get(0);
        assertEquals(OpCode.BLOCK, block.getOp());
        assertTrue(block.getBody().stream().anyMatch(i -> "add".equals(i.getName())));
        assertEquals(OpCode.FILE, program.getBody().get(1).getOp());
    }

    @Test
    void testQuadraticSolver() {
        String code = """
            BEGIN
                LAYER INPUT name=a size=1
                LAYER INPUT name=b size=1
                LAYER INPUT name=c size=1
                LAYER DENSE name=features size=4 activation=relu
                LAYER DENSE name=roots size=2 activation=linear
                LAYER OUTPUT name=output size=2
                CONNECT a -> features
                CONNECT b -> features
                CONNECT c -> features
                CONNECT features -> roots
                CONNECT roots -> output
            END
            FILE "quadratic_weights.bin"
            """;
        Instruction program = parseAndVisit(code);

        assertEquals(OpCode.PROGRAM, program.getOp());
        assertEquals(2, program.getBody().size());
        Instruction block = program.getBody().get(0);
        assertEquals(OpCode.BLOCK, block.getOp());
        assertEquals(OpCode.FILE, program.getBody().get(1).getOp());
    }

    @Test
    void testSequencePredictor() {
        String code = """
            BEGIN
                LAYER INPUT name=seq_in size=5
                LET i = 0;
                FOR i FROM 0 TO 3
                BEGIN
                    LAYER DENSE name=hidden_[i] size=8 activation=tanh
                    CONNECT seq_in[i] -> hidden_[i]
                END
                LAYER DENSE name=final_hidden size=8 activation=relu
                LAYER OUTPUT name=out size=1
                CONNECT hidden_0 -> final_hidden
                CONNECT hidden_1 -> final_hidden
                CONNECT hidden_2 -> final_hidden
                CONNECT hidden_3 -> final_hidden
                CONNECT final_hidden -> out
            END
            FILE "seq_pred_weights.bin"
            """;
        Instruction program = parseAndVisit(code);
        assertEquals(OpCode.PROGRAM, program.getOp());
        Instruction block = program.getBody().get(0);
        assertEquals(OpCode.BLOCK, block.getOp());
        assertTrue(block.getBody().stream().anyMatch(i -> i.getOp() == OpCode.FOR));
        assertEquals(OpCode.FILE, program.getBody().get(1).getOp());
    }

    @Test
    void testImageClassifier() {
        String code = """
            DEFINE norm_block(input_name)
            BEGIN
                LAYER DENSE name=normed size=128 activation=relu
                CONNECT input_name -> normed
            END

            MODULE preprocess
            BEGIN
                LAYER INPUT name=img_in shape=28 x 28
                norm_block(img_in)
            END

            BEGIN
                preprocess
                LAYER DENSE name=features size=64 activation=relu
                LAYER OUTPUT name=class_out size=10 activation=softmax
                CONNECT normed -> features
                CONNECT features -> class_out
            END
            FILE "mnist_weights.dat"
            """;
        Instruction program = parseAndVisit(code);
        List<Instruction> body = program.getBody();
        assertTrue(body.stream().anyMatch(i -> i.getOp() == OpCode.MACRO_DEF));
        assertTrue(body.stream().anyMatch(i -> i.getOp() == OpCode.MODULE_DEF));
        assertTrue(body.stream().anyMatch(i -> i.getOp() == OpCode.BLOCK));
        assertTrue(body.stream().anyMatch(i -> i.getOp() == OpCode.FILE));
    }

    @Test
    void testChatGPTNetwork() {
        String code = """
            BEGIN
                LAYER INPUT name=text_in size=256
                LAYER TRANSFORMER name=block depth=12 heads=8 dim=256
                LAYER OUTPUT name=word_probs size=50257 activation=softmax
                CONNECT text_in -> block
                CONNECT block -> word_probs
            END
            FILE "chatgpt_weights.pt"
            """;
        Instruction program = parseAndVisit(code);
        Instruction block = program.getBody().get(0);
        assertEquals(OpCode.BLOCK, block.getOp());
        assertTrue(block.getBody().stream().anyMatch(i -> "TRANSFORMER".equals(i.getType())));
    }

    @Test
    void testDimensionalExpand() {
        String code = """
            BEGIN
                LAYER INPUT name=tokens size=512
                LAYER TRANSFORMER name=transform3d depth=24 heads=16 dim=512
                LAYER OUTPUT name=embeddings shape=3 x 512
                CONNECT tokens -> transform3d
                CONNECT transform3d -> embeddings
                EXPAND SPACE = 3D
                BEGIN
                    LAYER DENSE name=project3d size=512 activation=relu
                    CONNECT embeddings -> project3d
                END
            END
            FILE "3dchatgpt_weights.pt"
            """;
        Instruction program = parseAndVisit(code);
        Instruction block = program.getBody().get(0);
        assertTrue(block.getBody().stream().anyMatch(i -> i.getOp() == OpCode.EXPAND));
    }

    @Test
    void testSymbolicLogicEvaluator() {
        String code = """
            DEFINE logic_block(input_name, output_name)
            BEGIN
                LAYER DENSE name=logic_1 size=8 activation=relu
                LAYER DENSE name=logic_2 size=4 activation=relu
                LAYER OUTPUT name=output_name size=1 activation=sigmoid
                CONNECT input_name -> logic_1
                CONNECT logic_1 -> logic_2
                CONNECT logic_2 -> output_name
            END

            BEGIN
                LAYER INPUT name=logic_in size=4
                IF logic_in[0] == 1
                BEGIN
                    logic_block(logic_in, logic_out)
                END
                ELSE
                BEGIN
                    LAYER OUTPUT name=logic_out size=1 activation=linear
                    CONNECT logic_in -> logic_out
                END
            END
            FILE "logic_weights.bin"
            """;
        Instruction program = parseAndVisit(code);
        assertTrue(program.getBody().stream().anyMatch(i -> i.getOp() == OpCode.MACRO_DEF));
        assertTrue(program.getBody().stream().anyMatch(i -> i.getOp() == OpCode.BLOCK));

        Instruction block = program.getBody().stream().filter(i -> i.getOp() == OpCode.BLOCK).findFirst().get();
        assertTrue(block.getBody().stream().anyMatch(i -> i.getOp() == OpCode.IF));
    }

    @Test
    void testMadGodHypernetwork() {
        String code = """
            DEFINE transcend_block(layer_idx)
            BEGIN
                LAYER DENSE name=transcend_[layer_idx] size=2048 activation=chaos
                CONNECT universe_in -> transcend_[layer_idx]
            END

            BEGIN
                LAYER INPUT name=universe_in shape=(66, 66, 66)
                LET i = 0;
                FOR i FROM 0 TO 999
                BEGIN
                    transcend_block(i)
                END

                EXPAND SPACE = 13D
                BEGIN
                    FOR d FROM 0 TO 12
                    BEGIN
                        LAYER DENSE name=dimension_[d] size=666 activation=madness
                        CONNECT transcend_[(d+1)*13] -> dimension_[d]
                    END
                END

                LAYER OUTPUT name=divine_utterance size=1 activation=apocalypse
                CONNECT dimension_12 -> divine_utterance
            END
            FILE "madgod66d_model.bin"
            """;
        Instruction program = parseAndVisit(code);
        assertTrue(program.getBody().stream().anyMatch(i -> i.getOp() == OpCode.MACRO_DEF));
        Instruction block = program.getBody().stream().filter(i -> i.getOp() == OpCode.BLOCK).findFirst().get();
        assertTrue(block.getBody().stream().anyMatch(i -> i.getOp() == OpCode.EXPAND));
        assertTrue(block.getBody().stream().anyMatch(i -> i.getOp() == OpCode.FOR));
    }

    @Test
    void testNeurysteriaEmotionalNetwork() {
        String code = """
            DEFINE calm_behavior(input_name, output_name)
            BEGIN
                LAYER DENSE name=calm1 size=16 activation=relu
                LAYER DENSE name=calm2 size=8 activation=relu
                LAYER OUTPUT name=output_name size=1 activation=sigmoid
                CONNECT input_name -> calm1
                CONNECT calm1 -> calm2
                CONNECT calm2 -> output_name
            END

            DEFINE angry_behavior(input_name, output_name)
            BEGIN
                LAYER DENSE name=angry1 size=32 activation=leaky_relu
                LAYER DENSE name=angry2 size=16 activation=relu
                LAYER OUTPUT name=output_name size=1 activation=sigmoid
                CONNECT input_name -> angry1
                CONNECT angry1 -> angry2
                CONNECT angry2 -> output_name
            END

            DEFINE hysterical_behavior(input_name, output_name)
            BEGIN
                LAYER DENSE name=hyst1 size=64 activation=elu
                LAYER DENSE name=hyst2 size=32 activation=relu
                LAYER OUTPUT name=output_name size=1 activation=sigmoid
                CONNECT input_name -> hyst1
                CONNECT hyst1 -> hyst2
                CONNECT hyst2 -> output_name
            END

            BEGIN
                LAYER INPUT name=stimulus size=4
                LAYER INPUT name=rage_level size=1

                IF rage_level < 3
                BEGIN
                    calm_behavior(stimulus, mood_out)
                END
                ELSE
                BEGIN
                    IF rage_level < 7
                    BEGIN
                        angry_behavior(stimulus, mood_out)
                    END
                    ELSE
                    BEGIN
                        hysterical_behavior(stimulus, mood_out)
                    END
                END
            END
            FILE "neurysteria_emotion.bin"
            """;
        Instruction program = parseAndVisit(code);
        assertTrue(program.getBody().stream().anyMatch(i -> i.getOp() == OpCode.MACRO_DEF));
        assertTrue(program.getBody().stream().anyMatch(i -> i.getOp() == OpCode.BLOCK));
        Instruction block = program.getBody().stream().filter(i -> i.getOp() == OpCode.BLOCK).findFirst().get();
        assertTrue(block.getBody().stream().anyMatch(i -> i.getOp() == OpCode.IF));
    }

    @Test
    void testExpandChatGptWithCustomGroup() {
        String code = """
            BEGIN
                LAYER INPUT name=tokens size=512
                LAYER TRANSFORMER name=block depth=12 heads=8 dim=512
                LAYER OUTPUT name=word_probs size=50257 activation=softmax
                CONNECT tokens -> block
                CONNECT block -> word_probs
                EXPAND GROUP = Cyclic20X
                BEGIN
                    LAYER DENSE name=grouped_project size=20 activation=relu
                    CONNECT block -> grouped_project
                    LAYER OUTPUT name=words_grouped size=50257 activation=softmax
                    CONNECT grouped_project -> words_grouped
                END
            END
            FILE "chatgpt_grouped_weights.bin"
            """;
        Instruction program = parseAndVisit(code);
        Instruction block = program.getBody().get(0);
        assertTrue(block.getBody().stream().anyMatch(i -> i.getOp() == OpCode.EXPAND));
        Instruction expand = block.getBody().stream().filter(i -> i.getOp() == OpCode.EXPAND).findFirst().get();
        assertEquals("Cyclic20X", expand.getTarget());
    }
}

