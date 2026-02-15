package io.github.swampus.alexandra.compiler.development.expanders;

import io.github.swampus.alexandra.compiler.development.DevelopmentContext;
import io.github.swampus.alexandra.compiler.development.InstructionExpander;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Expands {@link OpCode#FOR} instructions into a flat sequence of instructions
 * during the development phase.
 *
 * <p>This expander performs compile-time loop unrolling. Instead of producing
 * a runtime loop node, it clones the loop body for each iteration and injects
 * the loop variable value into the cloned instructions.</p>
 *
 * <h3>Semantics</h3>
 * <ul>
 *   <li>Bounds are treated as inclusive: {@code from <= i <= to}</li>
 *   <li>If {@code from > to}, the loop produces no instructions</li>
 *   <li>If body is {@code null} or empty, the loop produces no instructions</li>
 *   <li>The loop variable is temporarily bound in {@link DevelopmentContext}</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * FOR i FROM 1 TO 3
 * BEGIN
 *   LAYER dense[i] size=32
 * END
 * </pre>
 *
 * becomes:
 *
 * <pre>
 * LAYER dense1 size=32
 * LAYER dense2 size=32
 * LAYER dense3 size=32
 * </pre>
 *
 * <h3>Thread Safety</h3>
 * Not thread-safe. Intended for single-threaded compilation pipeline usage.
 */
public class ForExpander implements InstructionExpander {

    @Override
    public boolean supports(OpCode op) {
        return op == OpCode.FOR;
    }

    @Override
    public List<Instruction> expand(
            Instruction instr,
            DevelopmentContext ctx
    ) {

        Objects.requireNonNull(instr, "instruction must not be null");
        Objects.requireNonNull(ctx, "development context must not be null");

        // --- Extract loop variable name
        String var = instr.getVar();
        if (var == null || var.isBlank()) {
            throw new IllegalStateException("FOR instruction has no loop variable");
        }

        // --- Extract bounds (based on your existing compiler logic)
        int from = (instr.getFromVal() instanceof Number nFrom)
                ? nFrom.intValue()
                : 0;

        int to = (instr.getToVal() instanceof Number nTo)
                ? nTo.intValue()
                : -1;

        // --- Body safety
        List<Instruction> body = instr.getBody();
        if (body == null || body.isEmpty()) {
            return List.of();
        }

        // --- Preserve previous variable value if exists
        Object previousValue = ctx.has(var) ? ctx.get(var) : null;

        List<Instruction> result = new ArrayList<>();

        try {

            if (from <= to) {
                for (int i = from; i <= to; i++) {

                    ctx.set(var, i);

                    for (Instruction bodyInstr : body) {

                        // Deep clone + replace loop variable occurrences
                        Instruction cloned =
                                bodyInstr.deepCloneWithMultipleReplace(
                                        Map.of(var, i)
                                );

                        result.add(cloned);
                    }
                }
            }

        } finally {
            // Restore previous context state
            if (previousValue != null) {
                ctx.set(var, previousValue);
            } else {
                ctx.remove(var);
            }
        }

        return result;
    }
}
