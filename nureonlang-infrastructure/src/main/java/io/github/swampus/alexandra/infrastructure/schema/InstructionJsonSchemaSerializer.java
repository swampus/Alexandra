package io.github.swampus.alexandra.infrastructure.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.translator.exception.NureonLangTranslateException;
import io.github.swampus.alexandra.translator.exception.ParseError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * JSON serializer/deserializer for {@link Instruction} collections.
 *
 * <p>This class does not perform schema validation. It only provides
 * straightforward JSON conversion using Jackson.</p>
 */
public class InstructionJsonSchemaSerializer {

    private static final Logger log = LoggerFactory.getLogger(InstructionJsonSchemaSerializer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a list of {@link Instruction} objects into JSON (pretty printed).
     */
    public String toJson(List<Instruction> instructions) {
        Objects.requireNonNull(instructions, "instructions must not be null");

        try {
            log.debug("Serializing {} Instruction(s) to JSON", instructions.size());
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(instructions);
        } catch (Exception e) {
            log.error("Failed to serialize Instructions: {}", e.getMessage());
            throw new NureonLangTranslateException(List.of(
                    new ParseError("Failed to serialize Instructions to JSON: " + e.getMessage(), -1, -1)
            ));
        }
    }

    /**
     * Deserializes JSON array of instructions into a {@link List} of {@link Instruction}.
     */
    public List<Instruction> fromJson(String json) {
        Objects.requireNonNull(json, "json must not be null");

        try {
            log.debug("Deserializing Instructions from JSON ({} chars)", json.length());
            return objectMapper.readValue(
                    json,
                    objectMapper
                            .getTypeFactory()
                            .constructCollectionType(List.class, Instruction.class)
            );
        } catch (Exception e) {
            log.error("Failed to deserialize Instructions: {}", e.getMessage());
            throw new NureonLangTranslateException(List.of(
                    new ParseError("Failed to parse Instructions from JSON: " + e.getMessage(), -1, -1)
            ));
        }
    }

    // -------------------------------------------------------------------------
    // TODO Roadmap (for students)
    // -------------------------------------------------------------------------

    // TODO (1): Add JSON schema validation before deserialization
    //           (Draft-07 or Jackson JSON Schema Module).

    // TODO (2): Add automatic recovery mode:
    //           Try to parse as many Instructions as possible and collect all errors.

    // TODO (3): Add metadata header:
    //           {
    //             "schemaVersion": "1.0",
    //             "instructions": [ ... ]
    //           }
    //           And support migration from older schemas.

    // TODO (4): Support YAML format in addition to JSON.

    // TODO (5): Add binary format (e.g. Smile or CBOR) for faster load.

    // TODO (6): Add `toJsonMinified()` for storage-efficient version.

    // TODO (7): Add pretty-print configuration options (indentation, sorting).
}
