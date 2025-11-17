package io.github.swampus.alexandra.listner;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

public class CollectingErrorListener extends BaseErrorListener {
    private final List<String> syntaxErrors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        syntaxErrors.add("line " + line + ":" + charPositionInLine + " " + msg);
    }

    public List<String> getSyntaxErrors() {
        return syntaxErrors;
    }
}
