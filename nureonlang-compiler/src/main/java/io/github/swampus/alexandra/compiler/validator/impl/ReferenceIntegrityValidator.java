package io.github.swampus.alexandra.compiler.validator.impl;

import io.github.swampus.alexandra.compiler.extensions.InvalidNetworkException;
import io.github.swampus.alexandra.compiler.model.CompilationIssue;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.validator.NetworkModelValidator;

public final class ReferenceIntegrityValidator implements NetworkModelValidator {

    @Override
    public void validate(NetworkModel model) throws InvalidNetworkException {

        for (CompilationIssue issue : model.getIssues()) {
            if (issue.severity() == CompilationIssue.Severity.ERROR) {
                throw new InvalidNetworkException(issue.message());
            }
        }
    }
}

