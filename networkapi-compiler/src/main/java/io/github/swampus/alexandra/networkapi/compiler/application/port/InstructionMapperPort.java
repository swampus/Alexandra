package io.github.swampus.alexandra.networkapi.compiler.application.port;

import io.github.swampus.alexandra.dto.shared.instruction.InstructionDto;
import io.github.swampus.alexandra.ir.model.Instruction;

public interface InstructionMapperPort {
    InstructionDto toDto(Instruction instruction);

    Instruction fromDto(InstructionDto dto);
}
