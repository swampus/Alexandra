package io.github.swampus.alexandra.compiler;

import io.github.swampus.alexandra.compiler.exception.CompilationException;
import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.handlers.compilers.impl.*;
import io.github.swampus.alexandra.compiler.handlers.providers.InstructionProvider;
import io.github.swampus.alexandra.compiler.handlers.providers.impl.InMemoryInstructionProvider;
import io.github.swampus.alexandra.compiler.model.CompilationIssue;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.*;
import io.github.swampus.alexandra.compiler.model.layer.ModuleLayer;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Compiles IR {@link Instruction} trees into a {@link NetworkModel}.
 *
 * <p><b>Thread-safety:</b> Not thread-safe. Instances keep mutable state
 * (instruction/inputs cache, pending connects, trace) across a compilation run.</p>
 *
 * <p><b>Behavior:</b> This implementation is functionally identical to the original
 * (logging, comments, and minor internal polish only).</p>
 *
 * @since 0.9.0
 */
// TODO: Refactor compiler internals in a separate pass (no behavior changes here).
public final class IRNetworkCompiler {

    private static final Logger log = LoggerFactory.getLogger(IRNetworkCompiler.class);

    // Precompiled patterns to avoid re-compilation overhead in loops
    private static final Pattern SQUARE_EXPR_OR_TOKEN = Pattern.compile("\\[(\\([^\\]]+\\)|[^\\]]+)\\]");
    private static final Pattern VAR_IN_PARENS_EXPR = Pattern.compile("\\[\\(([^\\]]+)\\)\\]");
    private static final Pattern BARE_VAR_TOKEN = Pattern.compile("\\[([a-zA-Z_][a-zA-Z0-9_]*)\\]");
    private static final Pattern IDENTIFIERS_IN_EXPR = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
    private static final Pattern SUFFIX_STRIPPER = Pattern.compile("__.*$");
    private static final Pattern TRAILING_DIGITS = Pattern.compile("\\d+$");

    /**
     * Internal semantic pair for deferred connections; clearer than raw Map.Entry.
     */
    private record Connect(String from, String to) {}

    private final Map<OpCode, InstructionCompiler> handlers;
    private final Set<InstructionProvider> providers;

    private final Map<String, Instruction> instructionMap = new HashMap<>();
    private final Map<String, Layer> globalInputLayers = new HashMap<>();
    private final List<Connect> pendingConnects = new ArrayList<>();
    private final Map<String, Layer> layerMap = new HashMap<>();
    private final List<String> trace = new ArrayList<>();

    private void addTrace(String msg) { trace.add(msg); }
    public List<String> getTrace() { return trace; }
    public String getTraceAsString() { return String.join("\n", trace); }

    public IRNetworkCompiler() {
        handlers = new HashMap<>();
        handlers.put(OpCode.LAYER, new LayerInstructionCompiler());
        handlers.put(OpCode.CONNECT, new ConnectInstructionCompiler());
        handlers.put(OpCode.MODULE_DEF, new ModuleDefInstructionCompiler());
        handlers.put(OpCode.FOR, new ForInstructionCompiler());
        handlers.put(OpCode.IF, new IfInstructionCompiler());
        handlers.put(OpCode.MACRO_DEF, new MacroDefInstructionCompiler());
        handlers.put(OpCode.MODULE_CALL, new NotImplementedInstructionCompiler("MODULE_CALL"));
        handlers.put(OpCode.MACRO_CALL, new MacroCallInstructionCompiler(this));
        handlers.put(OpCode.LET, new NotImplementedInstructionCompiler("LET"));
        handlers.put(OpCode.EXPAND, new ExpandInstructionCompiler(this));
        handlers.put(OpCode.FILE, new NotImplementedInstructionCompiler("FILE"));
        handlers.put(OpCode.ELSE, new NotImplementedInstructionCompiler("ELSE"));

        providers = new HashSet<>();
        providers.add(new InMemoryInstructionProvider(instructionMap));
    }

    /**
     * Compiles the IR into a {@link NetworkModel}.
     *
     * @param ir non-null IR root
     * @return compiled model
     * @throws CompilationException on compilation errors
     */
    public NetworkModel compile(Instruction ir) {
        Objects.requireNonNull(ir, "ir");
        trace.clear();
        addTrace("== Compilation :: begin ==");

        NetworkModel model = new NetworkModel();
        Map<String, Layer> layers = new HashMap<>();
        compileInstruction(ir, model, layers);
        bindAllConnects(model, layers); // must run after traversal

        if (log.isDebugEnabled()) {
            // Layers
            log.debug("\n=== LAYERS ===");
            for (Layer l : model.getAllLayers()) {
                log.debug("  {}  ({})", l.getName(), l.getClass().getSimpleName());
            }
            // Edges
            log.debug("\n=== EDGES ===");
            for (Layer l : model.getAllLayers()) {
                for (Layer to : l.getOutputs()) {
                    log.debug("  {} -> {}", l.getName(), to.getName());
                }
            }
            // Inputs per layer
            log.debug("\n=== INPUTS per layer ===");
            for (Layer l : model.getAllLayers()) {
                StringBuilder sb = new StringBuilder().append(l.getName()).append(" <- ");
                for (Layer in : l.getInputs()) sb.append(in.getName()).append(' ');
                log.debug(sb.toString());
            }
        }
        return model;
    }

    private void bindAllConnects(NetworkModel model, Map<String, Layer> layers) {
        // Build a lookup for all layers, both in model and local map
        Map<String, Layer> allLayers = new HashMap<>();
        for (Layer l : model.getAllLayers()) allLayers.put(l.getName(), l);
        allLayers.putAll(layers);

        for (Connect c : pendingConnects) {
            Layer fromLayer = allLayers.get(c.from());
            Layer toLayer = allLayers.get(c.to());
            if (fromLayer == null || toLayer == null) continue;
            if (!toLayer.getInputs().contains(fromLayer)) toLayer.addInput(fromLayer);
            if (!fromLayer.getOutputs().contains(toLayer)) fromLayer.addOutput(toLayer);
        }
    }

    @SuppressWarnings("unused")
    private void collectConnectsRecursively(List<Instruction> instrs, List<Instruction> connects) {
        for (Instruction instr : instrs) {
            if (instr.getOp() == OpCode.CONNECT) connects.add(instr);
            if (instr.getBody() != null) collectConnectsRecursively(instr.getBody(), connects);
        }
    }

    public void registerInstruction(Instruction instr) {
        instructionMap.putIfAbsent(instr.getName(), instr);
    }

    public void compileInstruction(Instruction instr, NetworkModel model, Map<String, Layer> layers) {
        log.debug("[COMPILE] op={} name={}", instr.getOp(), instr.getName());

        if (instr.getName() != null
                && (instr.getOp() == OpCode.LAYER
                || instr.getOp() == OpCode.MODULE_DEF
                || instr.getOp() == OpCode.MACRO_DEF)) {
            registerInstruction(instr);
        }

        // If an input/output layer already exists among arguments, do not recreate it
        if (instr.getOp() == OpCode.LAYER
                && (("input".equalsIgnoreCase(instr.getType()) || "output".equalsIgnoreCase(instr.getType())))
                && layers.containsKey(instr.getName())) {
            return;
        }

        switch (instr.getOp()) {
            case LAYER: {
                log.error("DEBUG LAYER: name={}, type={}", instr.getName(), instr.getType());
                String name = instr.getName();
                String type = instr.getType();

                addTrace("[LAYER] created: " + name + " type=" + type);

                if ("input".equalsIgnoreCase(type)) {
                    if (globalInputLayers.containsKey(name)) {
                        layers.put(name, globalInputLayers.get(name));
                        return;
                    }
                }
                Layer layer = createLayerByType(instr);
                if ("input".equalsIgnoreCase(type)) globalInputLayers.put(name, layer);
                layers.put(name, layer);
                layerMap.put(name, layer);
                model.addLayer(layer);
                return;
            }

            case CONNECT: {
                String from = instr.getFrom();
                String to = instr.getTo();

                addTrace("[CONNECT] " + from + " -> " + to);
                pendingConnects.add(new Connect(from, to));

                // Auto-input: create virtual input (e.g., in1N) when needed
                if (!layers.containsKey(from)) {
                    if (from != null && from.matches("in1\\d+")) {
                        Layer baseInput = layers.get("in1");
                        if (baseInput instanceof InputLayer) {
                            InputLayer fakeInput = new InputLayer(from, 1);
                            layers.put(from, fakeInput);
                            model.addLayer(fakeInput);
                            log.debug("[AUTO-INPUT] created: {}", from);
                        }
                    }
                }

                if (!layers.containsKey(from) && instructionMap.containsKey(from)) {
                    lazyCompile(from, model, layers);
                }
                if (!layers.containsKey(to) && instructionMap.containsKey(to)) {
                    lazyCompile(to, model, layers);
                }

                Layer fromLayer = findLayer(layers, model, from);
                Layer toLayer = findLayer(layers, model, to);

                if (fromLayer == null || toLayer == null) {
                    log.warn("CONNECT skipped: from={}, to={}, fromLayer={}, toLayer={}",
                            from, to,
                            (fromLayer != null ? fromLayer.getName() : "null"),
                            (toLayer != null ? toLayer.getName() : "null"));

                    model.addIssue(new CompilationIssue(
                            CompilationIssue.Severity.ERROR,
                            "CONNECT refers to missing layer: " + from + " -> " + to
                    ));

                    return;
                }

                log.debug("CONNECT: from={} ({}) to={} ({})", from, fromLayer, to, toLayer);
                fromLayer.addOutput(toLayer);
                toLayer.addInput(fromLayer);
                return;
            }

            case MODULE_DEF: {
                String name = instr.getName();
                addTrace("[MODULE_DEF] " + name);

                ModuleLayer module = new ModuleLayer(name);
                if (instr.getBody() != null) {
                    for (Instruction sub : instr.getBody()) compileInstruction(sub, model, layers);
                }
                layers.put(name, module);
                model.addLayer(module);
                return;
            }

            case FOR: {
                String var = instr.getVar();
                int from = asInt(instr.getFromVal());
                int to = asInt(instr.getToVal());

                addTrace("[FOR] " + var + " from " + from + " to " + to);
                for (int i = from; i <= to; i++) {
                    for (Instruction sub : instr.getBody()) {
                        Instruction clone = deepCloneWithReplace(sub, var, i);
                        compileInstruction(clone, model, layers);
                    }
                }
                return;
            }

            case MACRO_DEF: {
                String name = instr.getName();
                model.addMacro(name, instr);
                addTrace("[MACRO_DEF] " + name);
                return;
            }

            case MACRO_CALL: {
                InstructionCompiler handler = handlers.get(instr.getOp());
                if (handler != null) {
                    handler.compile(instr, model, layers);
                    addTrace("[MACRO_CALL] " + instr.getName());
                    return;
                }
                throw new CompilationException("No handler for: " + instr.getOp(), trace);
            }

            case EXPAND: {
                InstructionCompiler handler = handlers.get(OpCode.EXPAND);
                if (handler != null) {
                    handler.compile(instr, model, layers);
                    addTrace("[EXPAND] " + instr.getName());
                    return;
                }
                throw new CompilationException("No handler for: " + instr.getOp(), trace);
            }

            case CALL: {
                String macroName = instr.getName();
                List<String> args = instr.getInputs();

                Instruction def = model.getMacro(macroName);
                if (def == null) throw new CompilationException("Macro not found: " + macroName, trace);

                List<String> params = def.getInputs();
                if (params.size() != args.size()) {
                    throw new CompilationException("Argument mismatch in call to " + macroName, trace);
                }

                Map<String, String> bindings = new HashMap<>();
                for (int i = 0; i < params.size(); i++) bindings.put(params.get(i), args.get(i));

                String macroSuffix = "__" + macroName + "_" + UUID.randomUUID().toString().substring(0, 4);
                bindings.put("__suffix__", macroSuffix);

                Set<String> externalNames = new HashSet<>(layers.keySet());
                for (Instruction bodyInstr : def.getBody()) {
                    Instruction clone = deepCloneWithMultipleReplace(bodyInstr, bindings, externalNames);
                    compileInstruction(clone, model, layers);
                }
                log.debug("CALL macro: {}, params: {}, args: {}, bindings: {}", macroName, params, args, bindings);
                addTrace("[CALL] " + instr.getName());
                return;
            }

            case IF: {
                String conditionExpr = (instr.getCond() != null) ? instr.getCond().getExpr() : null;
                addTrace("[IF] condition: " + conditionExpr);

                // Plain block (no condition expression)
                if (conditionExpr == null || conditionExpr.trim().isEmpty()) {
                    if (instr.getBody() != null) {
                        for (Instruction sub : instr.getBody()) compileInstruction(sub, model, layers);
                    }
                    return;
                }

                String condId = UUID.randomUUID().toString().substring(0, 4);
                String thenSuffix = "__then_" + condId;
                String elseSuffix = "__else_" + condId;

                List<Instruction> body = instr.getBody();
                Instruction thenBlock = (body != null && body.size() > 0) ? body.get(0) : null;
                Instruction elseBlock = (body != null && body.size() > 1) ? body.get(1) : null;

                Map<String, Layer> thenLayers = new HashMap<>();
                Layer thenRoot = null;
                if (thenBlock != null && thenBlock.getBody() != null) {
                    Map<String, String> thenBindings = Map.of("__suffix__", thenSuffix);
                    Set<String> externalNames = new HashSet<>(layers.keySet());
                    for (Instruction sub : thenBlock.getBody()) {
                        Instruction cloned = deepCloneWithMultipleReplace(sub, thenBindings, externalNames);
                        compileInstruction(cloned, model, thenLayers);
                    }
                    for (Layer l : thenLayers.values()) if (l instanceof OutputLayer) thenRoot = l;
                }

                Map<String, Layer> elseLayers = new HashMap<>();
                Layer elseRoot = null;
                if (elseBlock != null && elseBlock.getBody() != null) {
                    Map<String, String> elseBindings = Map.of("__suffix__", elseSuffix);
                    Set<String> externalNames = new HashSet<>(layers.keySet());
                    for (Instruction sub : elseBlock.getBody()) {
                        Instruction cloned = deepCloneWithMultipleReplace(sub, elseBindings, externalNames);
                        compileInstruction(cloned, model, elseLayers);
                    }
                    for (Layer l : elseLayers.values()) if (l instanceof OutputLayer) elseRoot = l;
                }

                for (var e : thenLayers.entrySet()) {
                    if (!layers.containsKey(e.getKey())) layers.put(e.getKey(), e.getValue());
                    model.addLayer(e.getValue());
                }
                for (var e : elseLayers.entrySet()) {
                    if (!layers.containsKey(e.getKey())) layers.put(e.getKey(), e.getValue());
                    model.addLayer(e.getValue());
                }

                String layerName = "cond_" + conditionExpr.replaceAll("\\W+", "_");
                ConditionalLayer condLayer = new ConditionalLayer(layerName, conditionExpr, thenRoot, elseRoot);

                // Try to connect input variables used in condition as inputs
                Matcher matcher = IDENTIFIERS_IN_EXPR.matcher(conditionExpr);
                Set<String> vars = new HashSet<>();
                while (matcher.find()) vars.add(matcher.group(1));
                for (String var : vars) {
                    Layer inp = model.getLayer(var);
                    if (inp instanceof InputLayer) condLayer.addInput(inp);
                }

                layers.put(layerName, condLayer);
                model.addLayer(condLayer);

                log.debug("CONDITIONAL: thenRoot={}, elseRoot={}",
                        (thenRoot != null ? thenRoot.getName() : null),
                        (elseRoot != null ? elseRoot.getName() : null));
                return;
            }

            default:
                // fall through
        }

        // If not returned above, process nested instructions
        if (instr.getBody() != null) {
            for (Instruction sub : instr.getBody()) compileInstruction(sub, model, layers);
        }
    }

    private void lazyCompile(String name, NetworkModel model, Map<String, Layer> layers) {
        if (layers.containsKey(name)) return;
        if (!instructionMap.containsKey(name)) {
            log.debug("[LAZY SKIP] not found: {}", name);
            return;
        }
        Instruction instr = instructionMap.get(name);
        log.debug("[LAZY-COMPILE] {} via {}", name, instr.getOp());
        compileInstruction(instr, model, layers);
    }

    private Layer createLayerByType(Instruction instr) {
        String type = instr.getType();
        String name = instr.getName();

        switch (type.toLowerCase()) {
            case "dense": {
                int size = (instr.getSize() != null) ? instr.getSize() : 0;
                String activation = instr.getActivation();
                return new DenseLayer(name, size, activation);
            }
            case "conv":
                return new ConvLayer(name);
            case "dropout": {
                double rate = (instr.getDropout() != null) ? instr.getDropout() : 0.5;
                return new DropoutLayer(name, rate);
            }
            case "attention":
                return new AttentionLayer(name);
            case "input": {
                int inputSize = (instr.getSize() != null) ? instr.getSize() : 0;
                Object shape = instr.getShape();
                if (globalInputLayers.containsKey(name)) return globalInputLayers.get(name);
                InputLayer inputLayer = (shape == null) ? new InputLayer(name, inputSize) : new InputLayer(name, shape);
                globalInputLayers.put(name, inputLayer);
                return inputLayer;
            }
            case "output": {
                int outputSize = (instr.getSize() != null) ? instr.getSize() : 0;
                String outActivation = instr.getActivation();
                Object shape = instr.getShape();
                return (shape == null) ? new OutputLayer(name, outputSize, outActivation) : new OutputLayer(name, shape);
            }
            case "transformer": {
                int dim = (instr.getDim() != null) ? instr.getDim() : 0;
                int depth = (instr.getDepth() != null) ? instr.getDepth() : 0;
                int heads = (instr.getHeads() != null) ? instr.getHeads() : 0;
                return new TransformerLayer(name, depth, heads, dim);
            }
            default:
                throw new CompilationException("Unknown layer type: " + type, trace);
        }
    }

    private Instruction deepCloneWithReplace(Instruction instr, String var, int value) {
        log.debug("TEST (deepCloneWithReplace)::{}", replaceVarSmart("transcend_[(d+1)*13]", "d", 3));

        Instruction clone = new Instruction();
        clone.setOp(instr.getOp());
        clone.setType(instr.getType());
        log.debug("deepCloneWithReplace: op={} name={}", clone.getOp(), clone.getName());

        clone.setName(replaceVarSmart(instr.getName(), var, value));
        clone.setFrom(replaceVarSmart(instr.getFrom(), var, value));
        clone.setTo(replaceVarSmart(instr.getTo(), var, value));
        clone.setActivation(instr.getActivation());
        clone.setShape(instr.getShape());
        clone.setSize(instr.getSize());
        clone.setExpr(replaceVarSmart(instr.getExpr(), var, value));
        clone.setDim(instr.getDim());
        clone.setDepth(instr.getDepth());
        clone.setAttention(instr.getAttention());
        clone.setDropout(instr.getDropout());
        clone.setHeads(instr.getHeads());
        clone.setGroup(replaceVarSmart(instr.getGroup(), var, value));
        clone.setSpace(replaceVarSmart(instr.getSpace(), var, value));
        clone.setTarget(replaceVarSmart(instr.getTarget(), var, value));
        clone.setPath(replaceVarSmart(instr.getPath(), var, value));
        clone.setVar(instr.getVar());
        clone.setFromVal(instr.getFromVal());
        clone.setToVal(instr.getToVal());
        clone.setCond(instr.getCond());

        if (instr.getInputs() != null) {
            clone.setInputs(instr.getInputs().stream().map(s -> replaceVarSmart(s, var, value)).toList());
        }
        if (instr.getOutputs() != null) {
            clone.setOutputs(instr.getOutputs().stream().map(s -> replaceVarSmart(s, var, value)).toList());
        }
        if (instr.getTags() != null) {
            clone.setTags(instr.getTags().stream().map(s -> replaceVarSmart(s, var, value)).toList());
        }
        if (instr.getParams() != null) {
            Map<String, Object> newParams = instr.getParams().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> replaceVarSmart(e.getKey(), var, value),
                            e -> {
                                Object v = e.getValue();
                                return (v instanceof String s) ? replaceVarSmart(s, var, value) : v;
                            }
                    ));
            clone.setParams(newParams);
        }
        if (instr.getMeta() != null) {
            Map<String, Object> newMeta = instr.getMeta().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> replaceVarSmart(e.getKey(), var, value),
                            e -> {
                                Object v = e.getValue();
                                return (v instanceof String s) ? replaceVarSmart(s, var, value) : v;
                            }
                    ));
            clone.setMeta(newMeta);
        }
        if (instr.getBody() != null) {
            List<Instruction> newBody = instr.getBody().stream()
                    .map(sub -> deepCloneWithReplace(sub, var, value))
                    .toList();
            clone.setBody(newBody);
        }

        log.debug("[CLONE] i={} op={} name={} from={} to={}", value, clone.getOp(), clone.getName(), clone.getFrom(), clone.getTo());
        return clone;
    }

    public String replaceVarSmart(String str, String varName, int value) {
        if (str == null) return null;

        Matcher matcher = SQUARE_EXPR_OR_TOKEN.matcher(str);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String expr = matcher.group(1);
            if (expr.startsWith("(") && expr.endsWith(")")) {
                expr = expr.substring(1, expr.length() - 1);
            }
            expr = expr.replaceAll("\\b" + Pattern.quote(varName) + "\\b", String.valueOf(value));
            int evalResult = evalSimple(expr);
            matcher.appendReplacement(result, String.valueOf(evalResult));
        }
        matcher.appendTail(result);

        // Replace remaining bare varName tokens outside brackets
        return result.toString().replaceAll("\\b" + Pattern.quote(varName) + "\\b", String.valueOf(value));
    }

    /** Very small four-ops evaluator (+,-,*,/), using exp4j. */
    public int evalSimple(String expr) {
        try {
            double result = new ExpressionBuilder(expr).build().evaluate();
            return (int) result;
        } catch (Exception e) {
            throw new CompilationException("Expression error: " + expr + " " + e.getMessage(), trace);
        }
    }

    private int asInt(Object value) {
        if (value instanceof Integer i) return i;
        if (value instanceof String s) return Integer.parseInt(s);
        throw new CompilationException("Cannot convert to int: " + value, trace);
    }

    private Layer findLayer(Map<String, Layer> layers, NetworkModel model, String name) {
        if (name == null) return null;
        if (layerMap.containsKey(name)) return layerMap.get(name);

        Layer layer = layers.get(name);
        if (layer == null) layer = model.getLayer(name);
        if (layer != null) return layer;

        // Try to resolve by base name (strip suffix and trailing digits)
        String base = SUFFIX_STRIPPER.matcher(name).replaceAll("");
        if (TRAILING_DIGITS.matcher(base).find()) {
            base = TRAILING_DIGITS.matcher(base).replaceAll("");
        }
        if (layerMap.containsKey(base)) return layerMap.get(base);
        layer = layers.get(base);
        if (layer == null) layer = model.getLayer(base);
        return layer;
    }

    public String replaceVarsSmart(String input, Map<String, String> bindings) {
        if (input == null) return null;

        // Evaluate [(expr)] first
        Matcher matcher = VAR_IN_PARENS_EXPR.matcher(input);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String expr = matcher.group(1);
            for (Map.Entry<String, String> e : bindings.entrySet()) {
                expr = expr.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b", e.getValue());
            }
            int evalResult = evalSimple(expr);
            matcher.appendReplacement(result, String.valueOf(evalResult));
        }
        matcher.appendTail(result);

        String replaced = result.toString();

        // Replace [var] → value
        matcher = BARE_VAR_TOKEN.matcher(replaced);
        result = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String replacement = bindings.getOrDefault(varName, varName);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        // Final safety: replace remaining occurrences outside brackets
        String out = result.toString();
        for (Map.Entry<String, String> e : bindings.entrySet()) {
            out = out.replaceAll("\\b" + Pattern.quote(e.getKey()) + "\\b", e.getValue());
        }
        return out;
    }

    public Instruction deepCloneWithMultipleReplace(Instruction instr,
                                                    Map<String, String> bindings,
                                                    Set<String> externalNames) {
        Set<String> argumentNames = new HashSet<>(bindings.values());

        Instruction clone = new Instruction();
        clone.setOp(instr.getOp());

        if (instr.getOp() == OpCode.MACRO_CALL) {
            clone.setName(replaceVarsSmart(instr.getName(), bindings));
        } else if ("input".equalsIgnoreCase(instr.getType()) || argumentNames.contains(instr.getName())) {
            // Do not suffix input-layer names and argument-bound names
            clone.setName(replaceVarsSmart(instr.getName(), bindings));
        } else {
            clone.setName(suffixedIfNeeded(
                    replaceVarsSmart(instr.getName(), bindings),
                    bindings,
                    instr.getName(),
                    instr.getType()
            ));
        }

        clone.setFrom(suffixedIfNeeded(
                replaceVarsSmart(instr.getFrom(), bindings),
                bindings,
                instr.getFrom(),
                instr.getType()
        ));
        clone.setTo(suffixedIfNeeded(
                replaceVarsSmart(instr.getTo(), bindings),
                bindings,
                instr.getTo(),
                instr.getType()
        ));
        clone.setType(instr.getType());
        clone.setInputs(replaceVarsList(instr.getInputs(), bindings));
        clone.setOutputs(replaceVarsList(instr.getOutputs(), bindings));
        clone.setSize(instr.getSize());
        clone.setActivation(instr.getActivation());

        if (instr.getBody() != null) {
            List<Instruction> newBody = new ArrayList<>();
            for (Instruction sub : instr.getBody()) {
                newBody.add(deepCloneWithMultipleReplace(sub, bindings, externalNames));
            }
            clone.setBody(newBody);
        }
        return clone;
    }

    private String suffixedIfNeeded(String name,
                                    Map<String, String> bindings,
                                    String templateName,
                                    String layerType) {
        if (name == null) return null;
        if ("input".equalsIgnoreCase(layerType)) return name;    // never suffix inputs
        if (templateName != null && !name.equals(templateName)) return name;
        if (bindings.containsValue(name)) return name;
        String suffix = bindings.get("__suffix__");
        return (suffix == null) ? name : name + suffix;
    }

    private List<String> replaceVarsList(List<String> list, Map<String, String> bindings) {
        if (list == null) return null;
        List<String> out = new ArrayList<>(list.size());
        for (String s : list) out.add(replaceVarsSmart(s, bindings));
        return out;
    }

    public void printAllLayerNames(NetworkModel model) {
        if (!log.isDebugEnabled()) return;
        log.debug("\nALL LAYERS:");
        for (Layer l : model.getAllLayers()) log.debug("  {}", l.getName());
    }
}
