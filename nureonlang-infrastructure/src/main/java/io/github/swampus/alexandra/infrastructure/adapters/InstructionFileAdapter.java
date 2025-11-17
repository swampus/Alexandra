package io.github.swampus.alexandra.infrastructure.adapters;

import io.github.swampus.alexandra.infrastructure.schema.InstructionJsonSchemaSerializer;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.translator.exception.NureonLangTranslateException;
import io.github.swampus.alexandra.translator.exception.ParseError;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class InstructionFileAdapter {
    private final InstructionJsonSchemaSerializer serializer = new InstructionJsonSchemaSerializer();

    public void writeToFile(List<Instruction> instructions, Path filePath) {
        try {
            String json = serializer.toJson(instructions);
            Files.writeString(filePath, json);
        } catch (Exception e) {
            throw new NureonLangTranslateException(List.of(
                    new ParseError("Failed to write Instructions to file: " + e.getMessage(), -1, -1)
            ));
        }
    }

    public List<Instruction> readFromFile(Path filePath) {
        try {
            String json = Files.readString(filePath);
            return serializer.fromJson(json);
        } catch (Exception e) {
            throw new NureonLangTranslateException(List.of(
                    new ParseError("Failed to read Instructions from file: " + e.getMessage(), -1, -1)
            ));
        }
    }
}
