package io.github.swampus.alexandra.compiler.handlers.compilers.impl;

import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.AttentionLayer;
import io.github.swampus.alexandra.compiler.model.layer.ConvLayer;
import io.github.swampus.alexandra.compiler.model.layer.DenseLayer;
import io.github.swampus.alexandra.compiler.model.layer.DropoutLayer;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

/**
 * Compiles a single LAYER instruction into a concrete {@link Layer} instance.
 *
 * <p>Supported types (case-insensitive):</p>
 * <ul>
 *   <li>{@code dense}</li>
 *   <li>{@code conv}</li>
 *   <li>{@code dropout}</li>
 *   <li>{@code attention}</li>
 * </ul>
 *
 * <p>New layer types should be added here (or in a dedicated registry) as the
 * model grows.</p>
 */
public class LayerInstructionCompiler implements InstructionCompiler {

    private static final Logger log = LoggerFactory.getLogger(LayerInstructionCompiler.class);

    @Override
    public void compile(Instruction instr,
                        NetworkModel model,
                        Map<String, Layer> layers) {

        if (instr == null) {
            throw new IllegalArgumentException("LayerInstructionCompiler: instruction must not be null");
        }

        String name = instr.getName();
        String type = instr.getType();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("LayerInstructionCompiler: layer has no name: " + instr);
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("LayerInstructionCompiler: layer '" + name + "' has no type");
        }

        String normalizedType = type.toLowerCase(Locale.ROOT);
        Layer layer;

        switch (normalizedType) {
            case "dense": {
                int size = (instr.getSize() != null) ? instr.getSize() : 0;
                String activation = instr.getActivation();
                layer = new DenseLayer(name, size, activation);
                break;
            }
            case "conv": {
                // For now we only use the name; extended metadata can be added later.
                layer = new ConvLayer(name);
                break;
            }
            case "dropout": {
                double rate = (instr.getDropout() != null) ? instr.getDropout() : 0.5;
                layer = new DropoutLayer(name, rate);
                break;
            }
            case "attention": {
                layer = new AttentionLayer(name);
                break;
            }
            // Add additional layer types here as the system evolves.
            default:
                throw new UnsupportedOperationException("Unknown layer type: " + type);
        }

        if (layers.containsKey(name)) {
            log.warn("Layer '{}' overrides an existing layer in the current scope", name);
        }

        layers.put(name, layer);
        model.addLayer(layer);

        if (log.isDebugEnabled()) {
            log.debug("Registered layer '{}' of type '{}'", name, normalizedType);
        }
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO (1): Move layer type registration to a pluggable registry:
    //           - allow external modules to register custom layer factories.
    //
    // TODO (2): Support richer ConvLayer, AttentionLayer, etc.:
    //           - read kernel/stride/shape/head parameters from Instruction.params/meta.
    //
    // TODO (3): Add validation before instantiation:
    //           - ensure required parameters are present for each layer type.
    //
    // TODO (4): Add unit tests:
    //           - dense with/without activation,
    //           - dropout with and without explicit rate,
    //           - unknown type -> exception,
    //           - name/type missing -> exception.
    //
    // TODO (5): Consider separating "layer construction" from "compiler wiring"
    //           by introducing a LayerFactory SPI.
}
