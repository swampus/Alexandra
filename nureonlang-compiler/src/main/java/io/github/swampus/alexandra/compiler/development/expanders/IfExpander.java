package io.github.swampus.alexandra.compiler.development.expanders;

import io.github.swampus.alexandra.compiler.development.DevelopmentContext;
import io.github.swampus.alexandra.compiler.development.InstructionExpander;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Expands IF instructions during development phase.
 *
 * <p>If the condition evaluates to true, the THEN body is returned.
 * Otherwise the ELSE body is returned (if present).</p>
 *
 * <p>This removes IF nodes from the IR and replaces them with the
 * selected branch instructions.</p>
 */
public class IfExpander implements InstructionExpander {

    @Override
    public boolean supports(OpCode op) {
        return op == OpCode.IF;
    }

    @Override
    public List<Instruction> expand(
            Instruction instr,
            DevelopmentContext ctx
    ) {

        Objects.requireNonNull(instr, "instruction must not be null");

        Instruction cond = instr.getCond();

        if (cond == null) {
            return List.of();
        }

        boolean result = evaluate(cond, ctx);

        if (result) {
            return safe(instr.getBody());
        }

        // ELSE block stored in meta["else"]
        Object elseObj = instr.getMeta() != null
                ? instr.getMeta().get("else")
                : null;

        if (elseObj instanceof List<?> list) {

            List<Instruction> elseBody = new ArrayList<>();

            for (Object o : list) {
                if (o instanceof Instruction i) {
                    elseBody.add(i);
                }
            }

            return elseBody;
        }

        return List.of();
    }

    private List<Instruction> safe(List<Instruction> body) {
        return body == null ? List.of() : body;
    }

    /**
     * Minimal condition evaluator.
     *
     * NOTE: Replace with your real expression engine later.
     */
    private boolean evaluate(Instruction cond, DevelopmentContext ctx) {

        // simplest possible logic first:

        if (cond.getExpr() != null) {
            Object val = ctx.resolve(cond.getExpr());
            if (val instanceof Boolean b) return b;
        }

        // fallback
        return false;
    }
}
