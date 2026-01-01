package io.github.swampus.alexandra.compiler.model;

import io.github.swampus.alexandra.compiler.model.layer.ConditionalLayer;
import io.github.swampus.alexandra.compiler.model.layer.InputLayer;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.compiler.model.layer.OutputLayer;
import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.ir.model.Instruction;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable container for compiled network layers and metadata.
 *
 * <p><b>Behavior:</b> This class preserves the original semantics:
 * layers are appended in insertion order; input/output tracking uses
 * internal lists that may contain duplicates until normalized by
 * accessors. No thread-safety is provided.</p>
 *
 * @since 0.9.0
 */
public class NetworkModel implements ModelWithMeta {

    private final List<CompilationIssue> issues = new ArrayList<>();

    /** All layers in insertion order. */
    private final List<Layer> layers = new ArrayList<>();

    /** Raw inputs as discovered; may contain duplicates. */
    private List<Layer> inputLayers = new ArrayList<>();

    /** Raw outputs as discovered; may contain duplicates. */
    private List<Layer> outputLayers = new ArrayList<>();

    /** Arbitrary metadata bag attached to the model. */
    @Getter
    private final Map<String, Object> meta = new LinkedHashMap<>();

    /** Macro registry by name. */
    private final Map<String, Instruction> macros = new LinkedHashMap<>();

    public void addIssue(CompilationIssue issue) {
        issues.add(issue);
    }

    public List<CompilationIssue> getIssues() {
        return List.copyOf(issues);
    }

    @Override
    public Map<String, Object> meta() {
        return meta;
    }

    public boolean hasErrors() {
        return issues.stream()
                .anyMatch(i -> i.severity() == CompilationIssue.Severity.ERROR);
    }

    /**
     * Returns the backing list of all layers.
     * <p>Note: returned list is mutable (behavior preserved).</p>
     */
    public List<Layer> getAllLayers() {
        return layers;
    }

    /**
     * Returns the only output layer or throws if there are none or multiple.
     *
     * @throws IllegalStateException if 0 or &gt;1 outputs are present
     */
    public Layer getSingleOutputLayer() {
        List<Layer> outs = getOutputLayers();
        if (outs.isEmpty()) {
            throw new IllegalStateException("No output layers found!");
        }
        if (outs.size() > 1) {
            throw new IllegalStateException(
                    "Multiple output layers found! Use getOutputLayers() for multi-output models.");
        }
        return outs.get(0);
    }

    /**
     * Returns the "main" output layer.
     * <p>Prefers {@link ConditionalLayer} if present; otherwise the first {@link OutputLayer}.</p>
     *
     * @throws IllegalStateException if no output layers are present
     */
    public Layer getMainOutputLayer() {
        for (Layer l : getOutputLayers()) {
            if (l instanceof ConditionalLayer) return l;
        }
        for (Layer l : getOutputLayers()) {
            if (l instanceof OutputLayer) return l;
        }
        throw new IllegalStateException("No output layer found");
    }

    /**
     * Returns all layers matching the given name (may be multiple).
     */
    public List<Layer> getLayersByName(String name) {
        return layers.stream()
                .filter(l -> name.equals(l.getName()))
                .toList();
    }

    /**
     * Returns the first layer with the given name, or {@code null} if none.
     * <p>Kept for backward compatibility with legacy call sites.</p>
     */
    public Layer getLayer(String name) {
        return layers.stream()
                .filter(l -> name.equals(l.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Registers an input layer (raw list; may contain duplicates).
     */
    public void addInputLayer(Layer layer) {
        inputLayers.add(layer);
    }

    /**
     * Registers a macro definition under the given name.
     */
    public void addMacro(String name, Instruction macro) {
        macros.put(name, macro);
    }

    /**
     * Returns the macro by name, or {@code null} if absent.
     */
    public Instruction getMacro(String name) {
        return macros.get(name);
    }

    /**
     * Returns whether a macro with the given name exists.
     */
    public boolean hasMacro(String name) {
        return macros.containsKey(name);
    }

    /**
     * Registers an output layer (raw list; duplicates prevented on {@link #addLayer}).
     */
    public void addOutputLayer(Layer layer) {
        outputLayers.add(layer);
    }

    /**
     * Returns unique input layers, preserving the first-seen order.
     */
    public List<Layer> getInputLayers() {
        Map<String, Layer> unique = new LinkedHashMap<>();
        for (Layer l : inputLayers) {
            unique.putIfAbsent(l.getName(), l);
        }
        return new ArrayList<>(unique.values());
    }

    /**
     * Returns output layers with conditional branches expanded to all nested {@link OutputLayer}s.
     * <p>If a {@link ConditionalLayer} is present, all {@link OutputLayer}s reachable via its
     * then/else branches are collected.</p>
     */
    public List<Layer> getOutputLayers() {
        List<Layer> outputs = new ArrayList<>();
        for (Layer l : outputLayers) {
            if (l instanceof ConditionalLayer cond) {
                outputs.addAll(findAllOutputLayers(cond));
            } else if (l instanceof OutputLayer) {
                outputs.add(l);
            }
        }
        return outputs;
    }

    /**
     * Recursively collects all {@link OutputLayer}s under the given layer.
     * <p>Currently handles nested {@link ConditionalLayer}s; can be extended for modules/blocks.</p>
     */
    private List<Layer> findAllOutputLayers(Layer layer) {
        List<Layer> found = new ArrayList<>();
        if (layer instanceof OutputLayer) {
            found.add(layer);
        }
        if (layer instanceof ConditionalLayer cond) {
            if (cond.getThenLayer() != null) found.addAll(findAllOutputLayers(cond.getThenLayer()));
            if (cond.getElseLayer() != null) found.addAll(findAllOutputLayers(cond.getElseLayer()));
        }
        return found;
    }

    /**
     * Adds a layer to the model and updates input/output tracking.
     * <p>Duplicates are preserved in {@code layers}; output tracking avoids duplicates.</p>
     */
    public void addLayer(Layer layer) {
        layers.add(layer);

        if (layer instanceof InputLayer) {
            inputLayers.add(layer);
        }
        if (layer instanceof OutputLayer) {
            if (!outputLayers.contains(layer)) {
                outputLayers.add(layer);
            }
        }
        if (layer instanceof ConditionalLayer) {
            // ConditionalLayer must be considered an output entry point.
            if (!outputLayers.contains(layer)) {
                outputLayers.add(layer);
            }
        }
    }
}
