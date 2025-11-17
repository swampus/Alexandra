package io.github.swampus.alexandra.nureonlang.parser.impl;

import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.nureonlang.parser.exception.NureonLangParseException;
import io.github.swampus.alexandra.nureonlang.parser.exception.listener.CollectingErrorParserListener;
import io.github.swampus.alexandra.nureonlang.parser.internal.InternalNureonLangService;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class InternalNureonLangServiceImpl implements InternalNureonLangService {

    private static final Logger log = LoggerFactory.getLogger(InternalNureonLangServiceImpl.class);

    @Override
    public NureonLangParser.ProgramContext parse(String code) {
        try {
            if (code == null) code = "";
            log.debug("Parsing code ({} chars)", code.length());

            NureonLangLexer lexer = new NureonLangLexer(CharStreams.fromString(code));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            NureonLangParser parser = new NureonLangParser(tokens);

            // Optional: SLL->LL fallback in parse() as well (kept simple here).
            return parser.program();
        } catch (Exception e) {
            throw new NureonLangParseException("Parsing error!", e);
        }
    }

    @Override
    public SyntaxCheckResult checkSyntax(String code) {
        if (code == null) code = "";
        log.debug("InternalNureonLangServiceImpl: input size={} chars", code.length());

        CollectingErrorParserListener listener = new CollectingErrorParserListener();

        NureonLangLexer lexer = new NureonLangLexer(CharStreams.fromString(code));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        // Two-phase parse: fast SLL with bail, then fallback to LL if needed
        NureonLangParser.ProgramContext tree;
        try {
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            parser.setErrorHandler(new BailErrorStrategy());
            tree = parser.program();
        } catch (RuntimeException ex) {
            tokens.seek(0);
            parser.reset();
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            parser.setErrorHandler(new DefaultErrorStrategy());
            tree = parser.program();
        }

        boolean valid = listener.getErrors().isEmpty() && parser.getNumberOfSyntaxErrors() == 0;

        // Structural sanity checks
        boolean hasStatements = tree.statement() != null && !tree.statement().isEmpty();
        boolean hasFileRef = tree.fileRef() != null;

        if (!hasStatements && !hasFileRef) {
            valid = false;
            listener.getErrors().add(
                    new SyntaxError(
                            1,
                            0,
                            "No valid statements found in source (invalid NeuroLang input)"
                    )
            );
        }

        // ---- SEMANTIC CHECK: MODULE CALLS ----
        if (hasStatements) {
            // Collect declared module names deterministically
            Set<String> declaredModules = new LinkedHashSet<>();
            for (var stmt : tree.statement()) {
                if (stmt.moduleDecl() != null) {
                    declaredModules.add(stmt.moduleDecl().ID().getText());
                }
            }

            // Validate each moduleCall against declarations
            for (var stmt : tree.statement()) {
                if (stmt.moduleCall() != null) {
                    String name = stmt.moduleCall().ID().getText();
                    if (!declaredModules.contains(name)) {
                        listener.getErrors().add(
                                new SyntaxError(
                                        stmt.getStart().getLine(),
                                        stmt.getStart().getCharPositionInLine(),
                                        "Unknown module called: '" + name + "'. No such module declared."
                                )
                        );
                        valid = false;
                    }
                }
            }

            // Guard: program cannot be a single moduleCall
            if (tree.statement().size() == 1) {
                var only = tree.statement(0);
                if (only.moduleCall() != null) {
                    listener.getErrors().add(
                            new SyntaxError(
                                    only.getStart().getLine(),
                                    only.getStart().getCharPositionInLine(),
                                    "Program cannot consist of a single module call."
                            )
                    );
                    valid = false;
                }
            }
        }

        // ---- SEMANTIC CHECK: MACRO ARGUMENT COUNT ----
        if (valid) {
            Map<String, Integer> macroParams = new HashMap<>();
            collectMacroDefinitions(tree, macroParams);

            for (var macroCall : collectMacroCalls(tree)) {
                String macroName = macroCall.ID().getText();
                int givenArgs = macroCall.argList() == null ? 0 : macroCall.argList().expr().size();
                Integer expected = macroParams.get(macroName);

                if (expected != null && givenArgs != expected) {
                    listener.getErrors().add(
                            new SyntaxError(
                                    macroCall.getStart().getLine(),
                                    macroCall.getStart().getCharPositionInLine(),
                                    String.format("Macro '%s' expects %d arguments, but got %d",
                                            macroName, expected, givenArgs)
                            )
                    );
                    valid = false;
                }
            }
        }

        log.debug("is valid: {}", valid);
        return new SyntaxCheckResult(valid, listener.getErrors());
    }

    /* ===================================================================== */
    /* ====================== Helper tree collectors ======================== */
    /* ===================================================================== */

    private void collectMacroDefinitions(ParseTree tree, Map<String, Integer> macroParams) {
        if (tree instanceof NureonLangParser.MacroDeclContext macroDecl) {
            String name = macroDecl.ID().getText();
            int paramCount = macroDecl.paramList() == null ? 0 : macroDecl.paramList().ID().size();
            macroParams.put(name, paramCount);
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            collectMacroDefinitions(tree.getChild(i), macroParams);
        }
    }

    private List<NureonLangParser.MacroCallContext> collectMacroCalls(ParseTree tree) {
        List<NureonLangParser.MacroCallContext> res = new ArrayList<>();
        if (tree instanceof NureonLangParser.MacroCallContext call) {
            res.add(call);
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            res.addAll(collectMacroCalls(tree.getChild(i)));
        }
        return res;
    }
}