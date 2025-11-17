package io.github.swampus.alexandra.nureonlang.parser.exception.listener;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxError;

import java.util.ArrayList;
import java.util.List;

public class CollectingErrorParserListener extends BaseErrorListener {
    private final List<SyntaxError> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg, RecognitionException e) {
        errors.add(new SyntaxError(line, charPositionInLine, msg));
    }

    public List<SyntaxError> getErrors() {
        return errors;
    }
}

