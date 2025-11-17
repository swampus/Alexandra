package io.github.swampus.alexandra;

import io.github.swampus.alexandra.cli.NueronlangCompilerCli;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErr;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic integration tests for the NueronLang compiler CLI.
 * <p>
 * Important:
 * - We call NueronlangCompilerCli#run(...) instead of main(...)
 * to avoid System.exit(...) terminating the test JVM.
 */
public class NueronlangCompilerCliTest {

    @Test
    void testCompileNoArgsShowsUsage() throws Exception {
        NueronlangCompilerCli cli = new NueronlangCompilerCli();

        String err = tapSystemErr(() ->
                cli.run(new String[]{"compile"})
        );

        assertTrue(err.contains("Missing source file path"),
                "Expected error message about missing source file path");
    }

    @Test
    void testUnknownCommand() throws Exception {
        NueronlangCompilerCli cli = new NueronlangCompilerCli();

        String err = tapSystemErr(() ->
                cli.run(new String[]{"unknown"})
        );

        assertTrue(err.contains("Unknown command"),
                "Expected error message about unknown command");
    }

    @Test
    void testCompileFileNotFound(@TempDir Path tmp) throws Exception {
        NueronlangCompilerCli cli = new NueronlangCompilerCli();
        Path wrong = tmp.resolve("no-such-file.nl");

        String err = tapSystemErr(() ->
                cli.run(new String[]{"compile", wrong.toString()})
        );

        assertTrue(err.contains("File not found"),
                "Expected error message about missing file");
    }

    @Test
    void testCompileMinimalProgram(@TempDir Path tmp) throws Exception {
        NueronlangCompilerCli cli = new NueronlangCompilerCli();
        Path file = tmp.resolve("simple.nl");

        Files.writeString(file, """
                              LAYER x input size=4
                              LAYER dense1 dense size=4
                              LAYER y output size=4
                              CONNECT x -> dense1
                              CONNECT dense1 -> y
                """);

        int exitCode = cli.run(new String[]{"compile", file.toString()});

        assertNotEquals(0, exitCode,
                "For now this snippet is not a valid NureonLang program, compiler must fail.");
    }

    @Test
    void testInvalidNureonLangSyntax(@TempDir Path tmp) throws Exception {
        NueronlangCompilerCli cli = new NueronlangCompilerCli();
        Path file = tmp.resolve("bad.nl");

        Files.writeString(file, "THIS *** IS INVALID");

        String err = tapSystemErr(() ->
                cli.run(new String[]{"compile", file.toString()})
        );

        assertTrue(err.contains("Compilation failed"),
                "Expected generic 'Compilation failed' message for invalid program");
    }
}
