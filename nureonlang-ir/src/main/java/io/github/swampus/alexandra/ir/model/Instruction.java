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
}