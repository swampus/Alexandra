package io.github.swampus.alexandra.translator.impl;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.nureonlang.parser.internal.InternalNureonLangService;
import io.github.swampus.alexandra.translator.NureonLangToIRTranslator;
import io.github.swampus.alexandra.translator.exception.NureonLangTranslateException;
import io.github.swampus.alexandra.translator.exception.ParseError;
import io.github.swampus.alexandra.visitor.NureonLangToIRVisitor;

import java.util.List;

public class NureonLangToIRTranslatorImpl implements NureonLangToIRTranslator {
    private final InternalNureonLangService parserService;

    public NureonLangToIRTranslatorImpl(InternalNureonLangService parserService) {
        this.parserService = parserService;
    }

    @Override
    public Instruction translate(String code) {
        System.out.println("\n CODE COME: " + code);
        try {
            NureonLangParser.ProgramContext ctx = parserService.parse(code);

            NureonLangToIRVisitor visitor = new NureonLangToIRVisitor();
            Instruction result = visitor.visitProgram(ctx);

            if (!visitor.getErrors().isEmpty()) {
                throw new NureonLangTranslateException(visitor.getErrors());
            }

            return result;
        } catch (NureonLangTranslateException e) {
            throw e;
        } catch (Exception e) {
            ParseError err = new ParseError(
                    "Internal parser/translation error: " + safeMsg(e),
                    findLine(e),
                    findCharPos(e)
            );
            throw new NureonLangTranslateException(List.of(err));
        }
    }

    private String safeMsg(Throwable e) {
        if (e == null) return "(null)";
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
        return msg;
    }

    private int findLine(Throwable e) {
        if (e instanceof org.antlr.v4.runtime.RecognitionException rex && rex.getOffendingToken() != null) {
            return rex.getOffendingToken().getLine();
        }
        return -1;
    }
    private int findCharPos(Throwable e) {
        if (e instanceof org.antlr.v4.runtime.RecognitionException rex && rex.getOffendingToken() != null) {
            return rex.getOffendingToken().getCharPositionInLine();
        }
        return -1;
    }
}
