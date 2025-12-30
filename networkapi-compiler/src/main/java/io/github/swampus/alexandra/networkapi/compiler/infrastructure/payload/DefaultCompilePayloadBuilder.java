package io.github.swampus.alexandra.networkapi.compiler.infrastructure.payload;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.networkapi.compiler.application.port.CompilePayloadPort;

public class DefaultCompilePayloadBuilder implements CompilePayloadPort {

    @Override
    public byte[] buildPayload(
            NetworkModel model,
            Instruction ir,
            String source
    ) {
        //TODO: later protobuff ?
        return source.getBytes();
    }
}

