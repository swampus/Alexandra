package io.github.swampus.alexandra.compiler.handlers.compilers.impl;

import io.github.swampus.alexandra.compiler.handlers.compilers.InstructionCompiler;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Compiles CONNECT instructions into graph edges between layers.
 *
 * <p>Current behavior:</p>
 * <ul>
 *   <li>Reads {@code from} and {@code to} layer names from the instruction.</li>
 *   <li>Rejects any connection that uses index notation (e.g. {@code seq[i]}).</li>
 *   <li>Looks up layers in the provided {@code layers} map and wires their
 *       {@code inputs}/{@code outputs} lists.</li>
 * </ul>
 *
 * <p>This compiler assumes that all referenced layers are already created and
 * present in the {@code layers} map at the time of invocation.</p>
 */
public class ConnectInstructionCompiler implements InstructionCompiler {

    private static final Logger log = LoggerFactory.getLogger(ConnectInstructionCompiler.class);

    @Override
    public void compile(Instruction instr,
                        NetworkModel model,
                        Map<String, Layer> layers) {

        if (instr == null) {
            throw new IllegalArgumentException("CONNECT: instruction must not be null");
        }

        String from = instr.getFrom();
        String to   = instr.getTo();

        if (from == null || to == null) {
            throw new IllegalArgumentException(
                    "CONNECT: 'from' and 'to' must not be null: from=" + from + ", to=" + to
            );
        }

        // Preserve original restriction: indexing syntax is not yet supported
        if (from.contains("[") || to.contains("[")) {
            throw new IllegalStateException(
                    "CONNECT: Indexing like seq_in[i] is not supported yet without expanded inputs."
            );
        }

        Layer fromLayer = layers.get(from);
        Layer toLayer   = layers.get(to);

        if (fromLayer != null && toLayer != null) {
            fromLayer.addOutput(toLayer);
            toLayer.addInput(fromLayer);

            if (log.isDebugEnabled()) {
                log.debug("CONNECT: {} -> {}", from, to);
            }
        } else {
            // Keep original exception type & message shape
            throw new IllegalArgumentException(
                    "CONNECT: One of layers not found: from=" + from + ", to=" + to
            );
        }
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students / contributors)
    // -------------------------------------------------------------------------

    // TODO (1): Support indexed connections (e.g. seq[i]) once tensor slicing is implemented.
    //
    // TODO (2): Add validation:
    //           - prevent duplicate edges (optional),
    //           - prevent self-connections (from == to) unless explicitly allowed.
    //
    // TODO (3): Integrate with a topology validator:
    //           - detect creation of cycles immediately after connecting layers.
    //
    // TODO (4): Add support for delayed resolution:
    //           - if from/to layers are not yet defined, store pending connections
    //             and resolve them after all layers are compiled.
    //
    // TODO (5): Add unit tests:
    //           - valid connect,
    //           - missing 'from'/'to',
    //           - unknown layers,
    //           - indexing syntax -> IllegalStateException.
}
