package io.github.swampus.alexandra.infrastructure.parser;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.visitor.NureonLangToIRVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * High-level service that parses NureonLang source code into IR {@link Instruction}s.
 *
 * <p>This class is a thin wrapper around the ANTLR4-generated lexer/parser and
 * a custom {@link NureonLangToIRVisitor}. It does not perform additional
 * validation or error recovery — syntax errors are handled by the underlying
 * ANTLR machinery / visitor.</p>
 */
public class InstructionParserService {

    private static final Logger log = LoggerFactory.getLogger(InstructionParserService.class);

    /**
     * Parses the given NureonLang source code into a root {@link Instruction}.
     *
     * @param code NureonLang source code, must not be {@code null}
     * @return root IR instruction (typically representing the whole program)
     */
    public Instruction parse(String code) {
        Objects.requireNonNull(code, "code must not be null");

        if (log.isDebugEnabled()) {
            log.debug("Parsing NureonLang source ({} chars)", code.length());
        }

        NureonLangLexer lexer = new NureonLangLexer(CharStreams.fromString(code));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);

        // NOTE: error listeners / custom error strategy can be attached here in future.
        NureonLangToIRVisitor visitor = new NureonLangToIRVisitor();
        return visitor.visit(parser.program());
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO (1): Attach a custom error listener to NureonLangParser
    //           that converts syntax errors into NureonLangTranslateException
    //           with precise line/column info.
    //
    // TODO (2): Implement a configurable error strategy:
    //           - fail-fast (current behavior),
    //           - or "collect all errors" mode for IDE integration.
    //
    // TODO (3): Add performance metrics:
    //           - measure parse time,
    //           - count tokens, rules visited, etc.
    //
    // TODO (4): Add a pre-parse sanity check:
    //           - empty input,
    //           - size limits for extremely large sources.
    //
    // TODO (5): Add unit tests for:
    //           - valid programs,
    //           - minimal snippets,
    //           - syntax errors (ensuring they are surfaced as expected).
}
