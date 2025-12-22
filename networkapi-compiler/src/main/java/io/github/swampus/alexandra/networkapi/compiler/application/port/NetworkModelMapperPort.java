package io.github.swampus.alexandra.networkapi.compiler.application.port;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.dto.shared.instruction.InstructionDto;
import io.github.swampus.alexandra.dto.shared.network.NNetworkDto;

public interface NetworkModelMapperPort {
    NNetworkDto mapModelToDto(NetworkModel model, String source, InstructionDto irDto);
}
