package io.github.swampus.alexandra.translator.impl;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.nureonlang.parser.internal.InternalNureonLangService;
import io.github.swampus.alexandra.translator.NureonLangToIRTranslator;
import io.github.swampus.alexandra.translator.exception.NureonLangTranslateException;
import io.github.swampus.alexandra.translator.exception.ParseError;
import io.github.swampus.alexandra.visitor.NureonLangToIRVisitor;
import org.antlr.v4.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Translates NeuroLang source code into an intermediate representation (IR).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Invoke ANTLR parser</li>
 *   <li>Run IR visitor</li>
 *   <li>Normalize parsing and translation errors</li>
 * </ul>
 */
public final class NureonLangToIRTranslatorImpl implements NureonLangToIRTranslator {

    private static final Logger log =
            LoggerFactory.getLogger(NureonLangToIRTranslatorImpl.class);

    private final InternalNureonLangService parserService;

    public NureonLangToIRTranslatorImpl(
            InternalNureonLangService parserService
    ) {
        this.parserService = parserService;
    }

    @Override
    public Instruction translate(String code) {
        log.debug("Starting NeuroLang translation (source length={} chars)",
                code == null ? 0 : code.length());

        try {
            var programContext = parse(code);
            var result = visit(programContext);

            log.info("NeuroLang translation completed successfully");
            return result;

        } catch (NureonLangTranslateException e) {
            // Expected user-facing error: syntax or semantic
            log.warn("NeuroLang translation failed with {} error(s)",
                    e.getErrors().size());
            throw e;

        } catch (Exception e) {
            // Unexpected internal failure
            log.error("Unexpected internal error during NeuroLang translation", e);
            throw wrapUnexpectedError(e);
        }
    }

    // ===================== INTERNAL STEPS =====================

    private NureonLangParser.ProgramContext parse(String code) {
        log.debug("Parsing NeuroLang source");
        return parserService.parse(code);
    }

    private Instruction visit(NureonLangParser.ProgramContext ctx) {
        log.debug("Visiting AST and building IR");

        var visitor = new NureonLangToIRVisitor();
        var result = visitor.visitProgram(ctx);

        if (!visitor.getErrors().isEmpty()) {
            log.warn("IR visitor reported {} semantic error(s)",
                    visitor.getErrors().size());
            throw new NureonLangTranslateException(visitor.getErrors());
        }

        return result;
    }

    // ===================== ERROR HANDLING =====================

    private NureonLangTranslateException wrapUnexpectedError(Exception e) {
        return new NureonLangTranslateException(
                List.of(new ParseError(
                        buildSafeMessage(e),
                        extractLine(e),
                        extractCharPosition(e)
                ))
        );
    }

    private String buildSafeMessage(Throwable e) {
        if (e == null) {
            return "Unknown internal error";
        }

        var message = e.getMessage();
        return (message == null || message.isBlank())
                ? e.getClass().getSimpleName()
                : message;
    }

    private int extractLine(Throwable e) {
        if (e instanceof RecognitionException rex
                && rex.getOffendingToken() != null) {
            return rex.getOffendingToken().getLine();
        }
        return -1;
    }

    private int extractCharPosition(Throwable e) {
        if (e instanceof RecognitionException rex
                && rex.getOffendingToken() != null) {
            return rex.getOffendingToken().getCharPositionInLine();
        }
        return -1;
    }
}
