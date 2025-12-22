package io.github.swampus.alexandra.networkapi.compiler.infrastructure.mapper;

import io.github.swampus.alexandra.dto.shared.instruction.InstructionDto;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import io.github.swampus.alexandra.networkapi.compiler.application.port.InstructionMapperPort;

import java.util.List;

/**
 * Infrastructure-level mapper converting between internal IR instructions
 * and transport-layer {@link InstructionDto} representations.
 *
 * <p>This mapper is intentionally recursive and performs a structural
 * transformation without any semantic interpretation.</p>
 *
 * <p><strong>Responsibilities:</strong>
 * <ul>
 *   <li>Convert IR {@link Instruction} to immutable DTO</li>
 *   <li>Convert DTO back to IR instruction</li>
 *   <li>Preserve full instruction tree structure</li>
 * </ul>
 *
 * <p><strong>Non-responsibilities:</strong>
 * <ul>
 *   <li>No validation</li>
 *   <li>No compilation logic</li>
 *   <li>No execution semantics</li>
 * </ul>
 */
public class InstructionMapper implements InstructionMapperPort {

    @Override
    public InstructionDto toDto(Instruction instruction) {
        if (instruction == null) {
            return null;
        }

        return new InstructionDto(
                instruction.getOp().name(),
                instruction.getType(),
                instruction.getName(),
                instruction.getFrom(),
                instruction.getTo(),
                instruction.getInputs(),
                instruction.getOutputs(),
                instruction.getActivation(),
                instruction.getShape(),
                instruction.getSize(),
                instruction.getExpr(),
                instruction.getDim(),
                instruction.getDepth(),
                instruction.getAttention(),
                instruction.getDropout(),
                instruction.getHeads(),
                instruction.getGroup(),
                instruction.getSpace(),
                instruction.getParams(),
                mapBodyToDto(instruction.getBody()),
                instruction.getVar(),
                instruction.getFromVal(),
                instruction.getToVal(),
                toDto(instruction.getCond()),
                instruction.getPath(),
                instruction.getTags(),
                instruction.getMeta(),
                instruction.getTarget(),
                instruction.getWeights()
        );
    }

    @Override
    public Instruction fromDto(InstructionDto dto) {
        if (dto == null) {
            return null;
        }

        return Instruction.builder()
                .op(OpCode.valueOf(dto.op()))
                .type(dto.type())
                .name(dto.name())
                .from(dto.from())
                .to(dto.to())
                .inputs(dto.inputs())
                .outputs(dto.outputs())
                .activation(dto.activation())
                .shape(dto.shape())
                .size(dto.size())
                .expr(dto.expr())
                .dim(dto.dim())
                .depth(dto.depth())
                .attention(dto.attention())
                .dropout(dto.dropout())
                .heads(dto.heads())
                .group(dto.group())
                .space(dto.space())
                .params(dto.params())
                .body(mapBodyFromDto(dto.body()))
                .var(dto.var())
                .fromVal(dto.fromVal())
                .toVal(dto.toVal())
                .cond(fromDto(dto.cond()))
                .path(dto.path())
                .tags(dto.tags())
                .meta(dto.meta())
                .target(dto.target())
                .weights(dto.weights())
                .build();
    }

    // ===================== INTERNAL HELPERS =====================

    private List<InstructionDto> mapBodyToDto(List<Instruction> body) {
        return body == null
                ? null
                : body.stream().map(this::toDto).toList();
    }

    private List<Instruction> mapBodyFromDto(List<InstructionDto> body) {
        return body == null
                ? null
                : body.stream().map(this::fromDto).toList();
    }
}
