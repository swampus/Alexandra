package io.github.swampus.alexandra.compiler.handlers.providers.impl;

import io.github.swampus.alexandra.compiler.handlers.providers.InstructionProvider;
import io.github.swampus.alexandra.ir.model.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory storage for IR instructions.
 *
 * <p>This provider is simple but stable: it supports lookup, mutation,
 * and safe retrieval. Designed for use in compilation pipelines where
 * performance is important and disk I/O is not required.</p>
 */
public class InMemoryInstructionProvider implements InstructionProvider {

    private static final Logger log = LoggerFactory.getLogger(InMemoryInstructionProvider.class);

    private final Map<String, Instruction> instructionMap;

    public InMemoryInstructionProvider(Map<String, Instruction> instructionMap) {
        // Defensive copy to avoid external modifications.
        this.instructionMap = new HashMap<>(instructionMap);
    }

    /**
     * Adds an instruction to the provider if the name is valid.
     */
    public void add(Instruction i) {
        if (i == null) {
            log.warn("Attempted to add null instruction.");
            return;
        }
        if (i.getName() == null || i.getName().isBlank()) {
            log.warn("Skipping unnamed instruction: {}", i);
            return;
        }
        instructionMap.put(i.getName(), i);

        if (log.isDebugEnabled()) {
            log.debug("Added instruction '{}'", i.getName());
        }
    }

    /**
     * Returns an unmodifiable map of stored instructions.
     */
    public Map<String, Instruction> getInstructions() {
        return Collections.unmodifiableMap(instructionMap);
    }

    @Override
    public Instruction getInstructionByName(String name) {
        if (name == null) {
            log.warn("Requested instruction by null name.");
            return null;
        }
        Instruction i = instructionMap.get(name);

        if (i == null && log.isDebugEnabled()) {
            log.debug("Instruction '{}' not found", name);
        }

        return i;
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for labs / assignments)
    // -------------------------------------------------------------------------

    // TODO (1): Add thread-safety via ConcurrentHashMap or ReadWriteLock.
    //
    // TODO (2): Add validation rules:
    //           - duplicate instruction names
    //           - invalid IR structures
    //
    // TODO (3): Add instruction removal by name.
    //
    // TODO (4): Add integrity checks:
    //           ensure referenced instructions exist.
    //
    // TODO (5): Add persistence layer:
    //           - serialize instructions to JSON/YAML
    //           - load from resources directory
    //
    // TODO (6): Add versioning for instructions
    //           (multiple revisions per name).
    //
    // TODO (7): Add optional caching/memoization for performance analytics.
    //
    // TODO (8): Add unit tests:
    //           - add/get/remove
    //           - null names
    //           - repeated inserts
    //
    // TODO (9): Add "snapshot" feature:
    //           returns deep copy for safe analysis.
    //
    // TODO (10): Integrate autodiff backend:
    //            attach metadata or gradients to instructions.
}
