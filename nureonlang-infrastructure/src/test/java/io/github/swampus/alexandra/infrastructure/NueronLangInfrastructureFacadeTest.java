package io.github.swampus.alexandra.infrastructure;

import io.github.swampus.alexandra.ir.model.Instruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class nureonLangInfrastructureFacadeTest {

    private NureonLangInfrastructureFacade facade;

    @BeforeEach
    void setUp() {
        facade = new NureonLangInfrastructureFacade();
    }

    @Test
    void testParseLayerInBlock() {
        String code = """
            BEGIN
               LAYER DENSE name=proj size=20
            END
            """;
        Instruction program = facade.parseCode(code);

        assertNotNull(program);
        assertEquals("PROGRAM", program.getOp().name());
        Instruction block = program.getBody().get(0);
        Instruction layer = block.getBody().get(0);

        assertEquals("LAYER", layer.getOp().name());
        assertEquals("DENSE", layer.getType());
        assertEquals("proj", layer.getName());
        assertEquals(20, layer.getSize());
        assertEquals("proj", layer.getParams().get("name"));
        assertEquals(20, Integer.valueOf((String) layer.getParams().get("size")));
    }

    @Test
    void testWriteAndReadFile() throws Exception {
        Instruction instr = Instruction.builder()
                .op(null)
                .name("test_layer")
                .activation("sigmoid")
                .size(42)
                .build();

        List<Instruction> instructions = List.of(instr);
        Path tempFile = Files.createTempFile("instructions", ".json");
        try {
            facade.writeToFile(instructions, tempFile);

            List<Instruction> readBack = facade.readFromFile(tempFile);

            assertNotNull(readBack);
            assertEquals(1, readBack.size());
            assertEquals("test_layer", readBack.get(0).getName());
            assertEquals("sigmoid", readBack.get(0).getActivation());
            assertEquals(42, readBack.get(0).getSize());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}