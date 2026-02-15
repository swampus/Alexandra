package io.github.swampus.alexandra.ir.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Instruction {
    private OpCode op;
    private String type;
    private String name;
    private String from;
    private String to;
    private List<String> inputs;
    private List<String> outputs;
    private String activation;
    private Object shape;
    private Integer size;
    private String expr;
    private Integer dim;
    private Integer depth;
    private Boolean attention;
    private Double dropout;
    private Integer heads;
    private String group;
    private String space;
    private Map<String, Object> params;
    private List<Instruction> body;
    private String var;
    private Object fromVal;
    private Object toVal;
    private Instruction cond;
    private String path;
    private List<String> tags;
    @Builder.Default
    private Map<String, Object> meta = new HashMap<>();
    private String target;
    @Builder.Default
    private List<Double> weights = null;

    /**
     * Creates a deep clone and replaces occurrences of variables
     * according to the provided map.
     *
     * <p>This is used during development-phase expansion of FOR/IF/MACRO
     * constructs where loop variables must be substituted into names,
     * expressions, parameters, etc.</p>
     *
     * Example:
     * dense[i] -> dense1   (i=1)
     */
    public Instruction deepCloneWithMultipleReplace(Map<String, ?> replacements) {

        Instruction copy = this.deepClone();

        copy.name = replace(copy.name, replacements);
        copy.from = replace(copy.from, replacements);
        copy.to = replace(copy.to, replacements);
        copy.expr = replace(copy.expr, replacements);
        copy.group = replace(copy.group, replacements);
        copy.space = replace(copy.space, replacements);
        copy.target = replace(copy.target, replacements);

        if (copy.inputs != null) {
            copy.inputs = copy.inputs.stream()
                    .map(s -> replace(s, replacements))
                    .toList();
        }

        if (copy.outputs != null) {
            copy.outputs = copy.outputs.stream()
                    .map(s -> replace(s, replacements))
                    .toList();
        }

        if (copy.params != null) {
            Map<String,Object> newParams = new HashMap<>();
            for (var e : copy.params.entrySet()) {
                Object v = e.getValue();
                if (v instanceof String s) {
                    newParams.put(e.getKey(), replace(s, replacements));
                } else {
                    newParams.put(e.getKey(), v);
                }
            }
            copy.params = newParams;
        }

        if (copy.body != null) {
            List<Instruction> newBody = new java.util.ArrayList<>();
            for (Instruction child : copy.body) {
                newBody.add(child.deepCloneWithMultipleReplace(replacements));
            }
            copy.body = newBody;
        }

        if (copy.cond != null) {
            copy.cond = copy.cond.deepCloneWithMultipleReplace(replacements);
        }

        return copy;
    }

    private static String replace(String value, Map<String, ?> repl) {
        if (value == null) return null;

        String result = value;

        for (var e : repl.entrySet()) {
            String key = e.getKey();
            String val = String.valueOf(e.getValue());

            // replace [i] -> 1
            result = result.replace("[" + key + "]", val);

            // replace bare i (safe-ish, depends on DSL)
            result = result.replace("${" + key + "}", val);
        }

        return result;
    }

    /**
     * Creates a deep copy of this instruction.
     *
     * <p>All mutable collections are copied recursively.
     * Nested instructions in {@code body} are also cloned.</p>
     */
    public Instruction deepClone() {

        Instruction copy = Instruction.builder()
                .op(op)
                .type(type)
                .name(name)
                .from(from)
                .to(to)
                .inputs(inputs == null ? null : List.copyOf(inputs))
                .outputs(outputs == null ? null : List.copyOf(outputs))
                .activation(activation)
                .shape(shape)
                .size(size)
                .expr(expr)
                .dim(dim)
                .depth(depth)
                .attention(attention)
                .dropout(dropout)
                .heads(heads)
                .group(group)
                .space(space)
                .params(params == null ? null : new HashMap<>(params))
                .body(cloneBody(body))
                .var(var)
                .fromVal(fromVal)
                .toVal(toVal)
                .cond(cond == null ? null : cond.deepClone())
                .path(path)
                .tags(tags == null ? null : List.copyOf(tags))
                .meta(meta == null ? null : new HashMap<>(meta))
                .target(target)
                .weights(weights == null ? null : List.copyOf(weights))
                .build();

        return copy;
    }

    private static List<Instruction> cloneBody(List<Instruction> body) {
        if (body == null) return null;
        List<Instruction> result = new java.util.ArrayList<>(body.size());
        for (Instruction i : body) {
            result.add(i.deepClone());
        }
        return result;
    }


}