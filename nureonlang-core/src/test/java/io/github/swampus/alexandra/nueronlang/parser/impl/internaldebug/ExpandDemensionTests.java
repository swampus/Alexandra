package io.github.swampus.alexandra.nureonlang.parser.impl.internaldebug;

import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.nureonlang.parser.impl.InternalNureonLangServiceImpl;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxCheckResult;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxError;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpandDemensionTests {

    private InternalNureonLangServiceImpl service = new InternalNureonLangServiceImpl();

    @Test
    void debugTokens() throws Exception {
        String code = """
                BEGIN
                    LAYER DENSE x size=32
                END
                """;
        CharStream input = CharStreams.fromString(code);
        io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer lexer = new io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer(input);

        Token token;
        while ((token = lexer.nextToken()).getType() != Token.EOF) {
            System.out.println(token.getText() + " | " + lexer.getVocabulary().getSymbolicName(token.getType()));
        }
    }

    @Test
    void debugParseTree() throws Exception {
        String code = "BEGIN\nLAYER DENSE x size=32\nEND";
        CharStream input = CharStreams.fromString(code);
        NureonLangLexer lexer = new NureonLangLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);

        parser.addParseListener(new ParseTreeListener() {
            @Override
            public void visitTerminal(TerminalNode node) {
                System.out.println("TERMINAL: " + node.getText());
            }
            @Override
            public void visitErrorNode(ErrorNode node) {
                System.out.println("ERROR: " + node.getText());
            }
            @Override
            public void enterEveryRule(ParserRuleContext ctx) {
                System.out.println("ENTER: " + parser.getRuleNames()[ctx.getRuleIndex()]);
            }
            @Override
            public void exitEveryRule(ParserRuleContext ctx) {
                System.out.println("EXIT: " + parser.getRuleNames()[ctx.getRuleIndex()]);
            }
        });

        parser.program(); // or parser.layerDecl(), etc.
    }
    @Test
    void testExpandIntoNDSpaces() {
        String code = """
                BEGIN
                    EXPAND SPACE = 3D
                    BEGIN
                        LAYER DENSE x size = 32
                        LAYER DENSE y size = 32
                        LAYER DENSE z size = 32
                        CONNECT x -> y
                        CONNECT y -> z
                    END
                END
                """;


        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid(), "Should accept expansion into ND space");
    }

    @Test
    void testExpandWithCustomGroup() {
        String code = """
                    BEGIN
                        EXPAND GROUP = myGroup
                        BEGIN
                            LAYER DENSE name="a" size=64
                            LAYER DENSE name="b" size=64
                            CONNECT a -> b
                        END
                    END
                """;
        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid(), "Should accept expansion with custom group");
    }

    @Test
    void testExpandWithWithGroupSyntax() {
        String code = """
                    BEGIN
                        EXPAND WITH GROUP myCustomSymmetry
                        BEGIN
                            LAYER DENSE name="sym_node" size=16
                        END
                    END
                """;
        SyntaxCheckResult result = service.checkSyntax(code);
        for (SyntaxError error : result.getErrors()) {
            System.out.println(error);
        }
        assertTrue(result.isValid(), "Should accept EXPAND WITH GROUP");
    }

    @Test
    void testExpandWithInvalidGroupSyntax() {
        String code = """
                    BEGIN
                        EXPAND GROUP
                        BEGIN
                            LAYER DENSE name="err" size=8
                        END
                    END
                """;
        SyntaxCheckResult result = service.checkSyntax(code);
        assertFalse(result.isValid(), "Should reject EXPAND GROUP without group name");
    }
}
