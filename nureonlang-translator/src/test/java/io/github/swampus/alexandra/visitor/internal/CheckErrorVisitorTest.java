package io.github.swampus.alexandra.visitor.internal;

import io.github.swampus.alexandra.listner.CollectingErrorListener;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.translator.exception.ParseError;
import io.github.swampus.alexandra.visitor.NureonLangToIRVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CheckErrorVisitorTest {

    class ParseResult {
        public final NureonLangToIRVisitor visitor;
        public final List<String> syntaxErrors;

        public ParseResult(NureonLangToIRVisitor visitor, List<String> syntaxErrors) {
            this.visitor = visitor;
            this.syntaxErrors = syntaxErrors;
        }
    }

    ParseResult parseWithErrors(String code) {
        NureonLangLexer lexer = new NureonLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);
        CollectingErrorListener errorListener = new CollectingErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        NureonLangParser.ProgramContext ctx = parser.program();
        NureonLangToIRVisitor visitor = new NureonLangToIRVisitor();
        visitor.visit(ctx);
        return new ParseResult(visitor, errorListener.getSyntaxErrors());
    }
    @Test
    void testAnonLayerMissingSizeError() {
        String code = "LAYER INPUT name=tokens";
        ParseResult res = parseWithErrors(code);
        var errors = res.visitor.getErrors();
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("size")));
        assertTrue(res.syntaxErrors.isEmpty());
    }

    @Test
    void testConnectMissingToError_Syntax() {
        String code = "CONNECT from ->";
        ParseResult res = parseWithErrors(code);
        assertFalse(res.syntaxErrors.isEmpty());
        assertTrue(res.visitor.getErrors().isEmpty());
    }

    @Test
    void testForLoopMissingBodyError_Syntax() {
        String code = "FOR i FROM 1 TO 10";
        ParseResult res = parseWithErrors(code);
        assertFalse(res.syntaxErrors.isEmpty());
        assertTrue(res.visitor.getErrors().isEmpty());
    }

    @Test
    void testMultipleErrors() {
        String code = """
                LAYER DENSE name=tok
                CONNECT -> out
                """;
        ParseResult res = parseWithErrors(code);
        assertTrue(res.visitor.getErrors().stream().anyMatch(e -> e.getMessage().toLowerCase().contains("size")));
        assertTrue(res.syntaxErrors.stream().anyMatch(s -> s.toLowerCase().contains("extraneous input")));
    }


}
