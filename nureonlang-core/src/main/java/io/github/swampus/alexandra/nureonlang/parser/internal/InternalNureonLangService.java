package io.github.swampus.alexandra.nureonlang.parser.internal;

import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxCheckResult;

public interface InternalNureonLangService {
    NureonLangParser.ProgramContext parse(String code);

    SyntaxCheckResult checkSyntax(String code);

}
