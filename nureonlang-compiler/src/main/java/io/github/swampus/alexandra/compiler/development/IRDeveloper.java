package io.github.swampus.alexandra.compiler.development;

import io.github.swampus.alexandra.ir.model.Instruction;

import java.util.ArrayList;
import java.util.List;

public class IRDeveloper {

    private final List<InstructionExpander> expanders;

    public IRDeveloper(List<InstructionExpander> expanders) {
        this.expanders = expanders;
    }

    public List<Instruction> develop(List<Instruction> program) {

        DevelopmentContext ctx = new DevelopmentContext();
        List<Instruction> result = new ArrayList<>();

        for (Instruction instr : program) {
            result.addAll(developInstruction(instr, ctx));
        }

        return result;
    }

    private List<Instruction> developInstruction(
            Instruction instr,
            DevelopmentContext ctx
    ) {

        for (InstructionExpander e : expanders) {
            if (e.supports(instr.getOp())) {
                List<Instruction> expanded = e.expand(instr, ctx);

                List<Instruction> finalList = new ArrayList<>();
                for (Instruction i : expanded) {
                    finalList.addAll(developInstruction(i, ctx));
                }
                return finalList;
            }
        }

        return List.of(instr);
    }
}
