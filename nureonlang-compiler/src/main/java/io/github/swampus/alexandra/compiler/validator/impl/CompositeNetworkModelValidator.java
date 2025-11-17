package io.github.swampus.alexandra.compiler.validator.impl;

import io.github.swampus.alexandra.compiler.extensions.InvalidNetworkException;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.validator.NetworkModelValidator;

import java.util.List;

public class CompositeNetworkModelValidator implements NetworkModelValidator {
    private final List<NetworkModelValidator> validators;

    public CompositeNetworkModelValidator(List<NetworkModelValidator> validators) {
        this.validators = validators;
    }

    @Override
    public void validate(NetworkModel model) throws InvalidNetworkException {
        for (NetworkModelValidator v : validators) {
            v.validate(model);
        }
    }
}
