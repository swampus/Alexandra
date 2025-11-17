package io.github.swampus.alexandra.nureonlang.parser.api;

import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxCheckResult;

public interface NureonLangService {
    SyntaxCheckResult checkSyntax(String code);
}
