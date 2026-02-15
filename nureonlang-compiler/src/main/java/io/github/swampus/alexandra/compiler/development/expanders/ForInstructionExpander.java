package io.github.swampus.alexandra.compiler.development.expanders;

import io.github.swampus.alexandra.compiler.development.DevelopmentContext;
import io.github.swampus.alexandra.compiler.development.InstructionExpander;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForInstructionExpander implements InstructionExpander {

    @Override
    public boolean supports(OpCode op) {
        return op == OpCode.FOR;
    }

    @Override
    public List<Instruction> expand(
            Instruction instr,
            DevelopmentContext ctx
    ) {

        String var = instr.getVar();

        int from = (instr.getFromVal() instanceof Number nFrom)
                ? nFrom.intValue()
                : 0;

        int to = (instr.getToVal() instanceof Number nTo)
                ? nTo.intValue()
                : 0;

        List<Instruction> result = new ArrayList<>();

        if (instr.getBody() == null) {
            return result;
        }

        for (int i = from; i <= to; i++) {

            ctx.set(var, i);

            for (Instruction bodyInstr : instr.getBody()) {

                Instruction clone =
                        bodyInstr.deepCloneWithMultipleReplace(
                                Map.of(var, i)
                        );

                result.add(clone);
            }
        }

        return result;
    }
}

