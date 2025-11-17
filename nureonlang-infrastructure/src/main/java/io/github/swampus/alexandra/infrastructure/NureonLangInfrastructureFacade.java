package io.github.swampus.alexandra.infrastructure;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.infrastructure.adapters.InstructionFileAdapter;
import io.github.swampus.alexandra.infrastructure.parser.InstructionParserService;
import io.github.swampus.alexandra.infrastructure.schema.InstructionJsonSchemaSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * High-level facade for the NureonLang infrastructure layer.
 *
 * <p>This class aggregates parser, serializer, and file I/O functionality
 * and exposes simple methods for loading, parsing, serializing, and saving
 * instructions. It contains no domain logic and no compilation logic —
 * only infrastructure-related operations.</p>
 *
 * <p>All components are currently created internally. In the future, this can be
 * extended to dependency injection (e.g. via Spring or manual DI).</p>
 */
public class NureonLangInfrastructureFacade {

    private static final Logger log = LoggerFactory.getLogger(NureonLangInfrastructureFacade.class);

    private final InstructionParserService parserService;
    private final InstructionJsonSchemaSerializer serializer;
    private final InstructionFileAdapter fileAdapter;

    public NureonLangInfrastructureFacade() {
        this.parserService = new InstructionParserService();
        this.serializer = new InstructionJsonSchemaSerializer();
        this.fileAdapter = new InstructionFileAdapter();
    }

    /**
     * Parses raw NureonLang source code into a root {@link Instruction}.
     *
     * @param code textual source code
     * @return parsed instruction tree
     */
    public Instruction parseCode(String code) {
        Objects.requireNonNull(code, "code must not be null");
        log.debug("Parsing NureonLang code ({} chars)", code.length());
        return parserService.parse(code);
    }

    /**
     * Serializes a list of instructions into JSON using the JSON schema format.
     */
    public String toJson(List<Instruction> instructions) {
        Objects.requireNonNull(instructions, "instructions must not be null");
        log.debug("Serializing {} instructions to JSON", instructions.size());
        return serializer.toJson(instructions);
    }

    /**
     * Deserializes JSON back into a list of instructions.
     */
    public List<Instruction> fromJson(String json) {
        Objects.requireNonNull(json, "json must not be null");
        log.debug("Deserializing instructions from JSON ({} chars)", json.length());
        return serializer.fromJson(json);
    }

    /**
     * Writes instructions to the file system using the adapter.
     */
    public void writeToFile(List<Instruction> instructions, Path filePath) {
        Objects.requireNonNull(instructions, "instructions must not be null");
        Objects.requireNonNull(filePath, "filePath must not be null");
        log.info("Writing {} instructions to file '{}'", instructions.size(), filePath);
        fileAdapter.writeToFile(instructions, filePath);
    }

    /**
     * Reads instructions from a JSON file.
     */
    public List<Instruction> readFromFile(Path filePath) {
        Objects.requireNonNull(filePath, "filePath must not be null");
        log.info("Reading instructions from file '{}'", filePath);
        return fileAdapter.readFromFile(filePath);
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students)
    // -------------------------------------------------------------------------

    // TODO (1): Introduce NureonLangInfrastructureConfig so all adapters/serializers
    //           can be injected (remove "new" from this class).

    // TODO (2): Add schema validation before deserialization,
    //           so invalid JSON produces descriptive compiler-grade errors.

    // TODO (3): Add version header to JSON format (e.g., "schemaVersion"),
    //           and automatic migration from older schemas.

    // TODO (4): Add checksum / digital signature support for instruction files
    //           to ensure integrity when loading NureonLang from disk.

    // TODO (5): Add file format autodetection (YAML, JSON, binary format)
    //           via small magic header instead of only JSON.

    // TODO (6): Add async I/O versions (readAsync/writeAsync) for large models.

    // TODO (7): Create CLI wrapper: `nureon parse`, `nureon validate`, `nureon json`.
}
