package io.github.swampus.alexandra.compiler.handlers.compilers.impl;

import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.compiler.model.layer.ModuleLayer;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Compiler for MODULE definitions.
 *
 * <p>This compiler creates a {@link ModuleLayer} representing the module entry
 * point and (optionally) compiles its internal instruction body. A module may
 * serve as a namespaced group of layers or as a reusable component.</p>
 *
 * <p>Current implementation works globally (all layers go to the same graph).
 * A future enhancement will introduce a per-module scope.</p>
 */
public class ModuleDefInstructionCompiler implements InstructionCompiler {

    private static final Logger log = LoggerFactory.getLogger(ModuleDefInstructionCompiler.class);

    @Override
    public void compile(Instruction instr,
                        NetworkModel model,
                        Map<String, Layer> layers) {

        String name = instr.getName();
        if (name == null) {
            throw new IllegalArgumentException("ModuleDefInstructionCompiler: module has no name: " + instr);
        }

        log.debug("Compiling module definition: {}", name);

        ModuleLayer module = new ModuleLayer(name);

        // Attach empty or real sub-instructions
        if (instr.getBody() != null && !instr.getBody().isEmpty()) {

            log.debug("Module {} contains {} instructions", name, instr.getBody().size());

            // ---------------------------------------------------------------------
            // FUTURE FEATURE: local scope for the module
            // ---------------------------------------------------------------------
            // Map<String, Layer> localScope = new HashMap<>(layers);
            // compileSubInstructions(instr, model, localScope);
            // module.setInternalLayers(localScope);
            //
            // Пока используется глобальный scope (как у тебя).
            // ---------------------------------------------------------------------
        } else {
            log.debug("Module {} is empty (no body instructions)", name);
        }

        // Register module layer
        if (layers.containsKey(name)) {
            log.warn("Module {} overrides an existing layer with the same name", name);
        }

        layers.put(name, module);
        model.addLayer(module);

        log.info("Module '{}' registered.", name);
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap — future improvements for module system
    // -------------------------------------------------------------------------

    // TODO (1): Implement full sub-instruction compilation inside module
    //           including MACROs, nested MODULEs, etc.

    // TODO (2): Add real lexical scope for module-local layers.
    //           External layers should not accidentally leak into modules.

    // TODO (3): Add namespace support:
    //           module.name.subLayerName as fully-qualified identifier.

    // TODO (4): Support module parameters and parameter binding (like PyTorch nn.Module).

    // TODO (5): Add validation: modules must not form cycles unless explicitly allowed.

    // TODO (6): Allow exporting selected internal layers (public API of module).

    // TODO (7): Add unit tests:
    //           - empty module
    //           - module with nested layers
    //           - module name conflicts
    //           - scope isolation tests
}
