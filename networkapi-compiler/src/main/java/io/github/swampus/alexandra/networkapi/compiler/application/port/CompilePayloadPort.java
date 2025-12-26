package io.github.swampus.alexandra.networkapi.compiler.application.port;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.ir.model.Instruction;

public interface CompilePayloadPort {

    /**
     * Builds a canonical binary payload for a compiled network.
     */
    byte[] buildPayload(
            NetworkModel model,
            Instruction ir,
            String source
    );
}

