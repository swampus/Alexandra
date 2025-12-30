package io.github.swampus.alexandra.nueronlang.parser.impl.internaldebug;

import io.github.swampus.alexandra.nureonlang.parser.impl.InternalNureonLangServiceImpl;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxCheckResult;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComplexNureonLngParseTest {

    private InternalNureonLangServiceImpl service = new InternalNureonLangServiceImpl();

    @Test
    void testUnetArchitecture() {
        String code = """
        BEGIN
            LET depth = 4;
            LET filters = 64;

            FOR i FROM 0 TO depth
            BEGIN
                LAYER CONV id=enc[i] filters=filters ^ (2 ^ i)
            END

            FOR i FROM 0 TO depth - 1
            BEGIN
                LAYER CONV id=dec[i] filters=filters ^ (2 ^ (depth - i - 1))
                CONNECT dec[i] -> enc[depth - i - 1]
            END
        END
        """;

        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid());
    }

    @Test
    void testTransformerWithMacros() {
        String code = """
        BEGIN
            DEFINE attentionBlock(input, heads)
            BEGIN
                LAYER CUSTOM type="MultiHeadAttention" input=input heads=heads
                LAYER DENSE input=heads units=512
            END

            LET layers = 6;
            FOR i FROM 1 TO layers
            BEGIN
                attentionBlock("x", 8)
                IF i > 1
                BEGIN
                    CONNECT layer[i-1] -> layer[i]
                END
            END
        END
        """;

        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid());
    }

    @Test
    void testResNetLikeStructure() {
        String code = """
        BEGIN
            DEFINE residualBlock(input)
            BEGIN
                LAYER DENSE id=pre units=128
                LAYER DENSE id=post units=128
                CONNECT pre -> post
                CONNECT input -> post
            END

            LET blocks = 3;
            FOR i FROM 0 TO blocks - 1
            BEGIN
                residualBlock("x" + i)
            END
        END
        """;

        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid());
    }

    @Test
    void testHybridNetworkWithShapes() {
        String code = """
                        BEGIN
                                LAYER INPUT shape=(3, 224, 224)
                                LAYER CONV filters=32 kernel=(3,3)
                                LAYER DENSE units=128
                                LAYER TRANSFORMER heads=8 dim=512
                                LAYER OUTPUT shape=10
                                CONNECT INPUT -> CONV
                                CONNECT CONV -> DENSE
                                CONNECT DENSE -> TRANSFORMER
                                CONNECT TRANSFORMER -> OUTPUT
                            END
                """;

        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid());
    }

    @Test
    void testDeepNestedModules() {
        String code = """
                BEGIN
                    DEFINE convBlock(name)
                    BEGIN
                        LET left = name+"_1";
                        LET right = name+"_2";
                        CONNECT left -> right
                    END

                    MODULE encoder
                    BEGIN
                        FOR i FROM 0 TO 2
                        BEGIN
                            convBlock("enc" + i)
                        END
                    END

                    MODULE decoder
                    BEGIN
                        FOR i FROM 0 TO 2
                        BEGIN
                            convBlock("dec" + i)
                        END
                    END
                END
                """;

        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid());
    }
}
