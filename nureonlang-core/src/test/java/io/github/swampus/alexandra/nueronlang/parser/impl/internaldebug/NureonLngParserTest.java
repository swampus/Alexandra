package io.github.swampus.alexandra.nureonlang.parser.impl.internaldebug;

import io.github.swampus.alexandra.nureonlang.parser.impl.InternalNureonLangServiceImpl;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxCheckResult;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxError;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;

import static org.junit.jupiter.api.Assertions.*;

class NureonLngParserTest {

    private InternalNureonLangServiceImpl service = new InternalNureonLangServiceImpl();
    @Test
    public void testSimpleFeedforwardNetwork() {
        String code = """
                    BEGIN
                        LAYER INPUT name=input size=784
                        LAYER DENSE name=hidden size=128 activation=relu
                        LAYER OUTPUT name=output size=10 activation=softmax
                        CONNECT input -> hidden
                        CONNECT hidden -> output
                    END
                    FILE "simple_weights.bin"
                """;
        assertParses(code);
    }

    @Test
    public void testWithConditionalBlock() {
        String code = """
                    BEGIN
                        LAYER INPUT name=input size=32
                        IF input_size > 16
                        BEGIN
                            LAYER DENSE name=mid size=64 activation=tanh
                        END
                        LAYER OUTPUT name=out size=4 activation=softmax
                    END
                """;
        assertParses(code);
    }

    @Test
    public void simpleTest(){
        String code = """
                BEGIN
                    LAYER INPUT name = "input"
                    LAYER DENSE name = "hidden"
                    CONNECT input -> hidden
                    CONNECT input[0] -> hidden[1]
                END
                """;
        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid());
    }

    @Test
    public void testExpandWithGroup() {
        String code = """
                    EXPAND WITH GROUP SO3
                    BEGIN
                      LAYER DENSE name=so3_dense size=128 activation=relu
                    END
                                
                """;
        assertParses(code);
    }

    @Test
    public void testWithLet() {
        String code = """
                    LET arr[2] = 5;
                               FOR i FROM 0 TO 2
                               BEGIN
                                   LAYER DENSE name=hidden_[i] size=arr[i] activation=relu
                               END                             
                """;
        assertParses(code);
    }

    @Test
    public void testWithBracets() {
        String code = """
                   LET r = (a^2 + b^2)^0.5;
                   LAYER DENSE name=math size=2 activation=linear param1=3.14 param2=2^8                      
                """;
        assertParses(code);
    }

    @Test
    public void testLoopBlock() {
        String code = """
                    BEGIN
                        LAYER INPUT name=in size=64
                        LAYER DENSE name=hidden1 size=64 activation=relu
                        LAYER DENSE name=hidden2 size=64 activation=relu
                        LAYER DENSE name=hidden3 size=64 activation=relu
                        LAYER OUTPUT name=out size=10 activation=softmax
                    END
                    FILE "loop_model.dat"
                """;
        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid());
    }

    @Test
    public void testUnetArchitecture() {
        String code = """
                    BEGIN
                        LAYER INPUT name=image_in shape=256 x 256
                        LAYER UNET name=unet_block depth=4 attention=true
                        LAYER OUTPUT name=image_out shape=256 x 256
                        CONNECT image_in -> unet_block
                        CONNECT unet_block -> image_out
                    END
                    FILE "unet_weights.h5"
                """;
        assertParses(code);
    }

    @Test
    public void testUnetArchitectureWrong() {
        String code = """
                    BEGIN
                        LAYER INPUT name=image_in shape=256256
                        LAYER UNET name=unet_block depth=4 attention=true
                        LAYER OUTPUT name=image_out shape=256 x 256
                        CONNECT image_in i -> unet_block
                        CONNECT unet_block -> image_out
                    END
                    FILE "broken_weights.h5"
                """;
        assertFails(code);
    }

    private void assertParses(String code) {
        CharStream input = CharStreams.fromString(code);
        NureonLangLexer lexer = new NureonLangLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg, RecognitionException e) {
                fail("Syntax error at " + line + ":" + charPositionInLine + " — " + msg);
            }
        });
        ParseTree tree = parser.program();
        assertNotNull(tree);
    }

    private void assertFails(String code) {
        CharStream input = CharStreams.fromString(code);
        NureonLangLexer lexer = new NureonLangLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new RuntimeException("Syntax error at " + line + ":" + charPositionInLine + " — " + msg);
            }
        });
        assertThrows(RuntimeException.class, parser::program);
    }

}
