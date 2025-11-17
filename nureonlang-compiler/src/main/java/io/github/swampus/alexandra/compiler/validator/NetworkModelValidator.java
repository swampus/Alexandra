package io.github.swampus.alexandra.compiler.validator;


import io.github.swampus.alexandra.compiler.extensions.InvalidNetworkException;
import io.github.swampus.alexandra.compiler.model.NetworkModel;

public interface NetworkModelValidator {
    void validate(NetworkModel model) throws InvalidNetworkException;
}
