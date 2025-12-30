package io.github.swampus.alexandra.nueronlang.parser.impl.internaldebug;

import io.github.swampus.alexandra.nureonlang.parser.impl.InternalNureonLangServiceImpl;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxCheckResult;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InternalNureonLangServiceImplTest {

    private InternalNureonLangServiceImpl service = new InternalNureonLangServiceImpl();

    @Test
    void testNestedModulesAndMacros() {
        String code = """
               BEGIN
                    DEFINE block(a, b)
                    BEGIN
                        LAYER DENSE name=a size=128
                        LAYER DENSE name=b size=128
                        CONNECT a -> b
                    END

                    MODULE outer
                    BEGIN
                        MODULE inner
                        BEGIN
                            block("foo", "bar")
                        END
                    END
               END    
                """;
        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid(), "Inner makros should pass");
    }

    @Test
    void testMacroCallWithMissingArgument() {
        String code = """
        DEFINE doLayer(x, y)
        BEGIN
            LAYER DENSE name=x size=y
        END

        doLayer("input")
    """;
        SyntaxCheckResult result = service.checkSyntax(code);
        assertFalse(result.isValid(), "Error: no makros argument");
        assertNotNull(result.getErrors());
    }

    @Test
    void testCustomLayerWithParams() {
        String code = """
        BEGIN
            LAYER CUSTOM name=custom_block type="MyLayer" param1=42 param2="abc"
        END
    """;
        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid());
    }

    @Test
    void testForLoopWithComplexExpression() {
        String code = """
                         BEGIN
                             LET n = 5;
                             FOR i FROM 0 TO n * 2 + 1
                             BEGIN
                                 LAYER DENSE name="layer" + i size=32
                             END
                         END
                     """;
        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid(), "FOR with exp should pass");
    }

    @Test
    void testForLoopWithSyntaxError() {
        String code = """
        FOR i FROM TO 10
        BEGIN 3
            LAYER DENSE size=32
        END
    """;
        SyntaxCheckResult result = service.checkSyntax(code);
        assertFalse(result.isValid(), "Syntax error should be found");
    }

}