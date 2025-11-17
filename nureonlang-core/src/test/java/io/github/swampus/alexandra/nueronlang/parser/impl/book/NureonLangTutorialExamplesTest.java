package io.github.swampus.alexandra.nureonlang.parser.impl.book;

import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tutorial-aligned examples for NureonLang.
 * All snippets are corrected to strictly follow the grammar:
 * - Named layer:  LAYER <Type> <Name> <param+>
 * - Anonymous:    LAYER <Type> <param+>
 * - param+ is mandatory
 * - No name templating like hidden_[i]; no use of loop index in identifiers
 */
class NureonLangTutorialExamplesTest {

    private void assertParses(String code) {
        CharStream input = CharStreams.fromString(code == null ? "" : code);
        NureonLangLexer lexer = new NureonLangLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NureonLangParser parser = new NureonLangParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg, RecognitionException e) {
                fail("Syntax error at " + line + ":" + charPositionInLine + " — " + msg);
            }
        });

        ParseTree tree = parser.program();
        assertNotNull(tree);
    }

    // ---------------------------------------------------------------------
    // 5. Blocks and Statements
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§5 Blocks: nested BEGIN...END with inner connection")
    void blocksAndStatements() {
        String code = """
                    BEGIN
                        LAYER input shape=(3,224,224)
                        LAYER conv1 filters=64 kernel=(3,3)
                        BEGIN
                            LAYER inner type=ReLU
                            CONNECT conv1 -> inner
                        END
                        CONNECT inner -> output
                    END
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 6. Layer Declarations
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§6 Layers: anonymous and named forms")
    void layerDeclarations() {
        String code = """
                BEGIN
                    // Anonymous layer (type + params)
                    LAYER Dense units=128 activation=ReLU
                    // Named layer (type + name + params)
                    LAYER Conv2D conv1 filters=32 kernel=(3,3) stride=1
                    LAYER ReLU relu1 flag=1
                    CONNECT conv1 -> relu1
                END
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 7. Parameters and Shapes
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§7 Params & Shapes: tuple and simple forms")
    void paramsAndShapes() {
        String code = """
                BEGIN
                    LAYER Dense dense1 units=256 activation=ReLU
                    // Tuple shape (channels, height, width)
                    LAYER Input in_tuple shape=(3,224,224)
                    // Simple shape using ID 'x' as separator
                    LAYER Image img_simple shape=256 x 256
                END
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 8. Expressions
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§8 Expressions: arithmetic, power, parens")
    void expressions() {
        String code = """
                BEGIN
                    LET base = 32;
                    LAYER Dense d1 units=(1+2)^2
                    LAYER Dense d2 units=base*4
                    LAYER Dense d3 units=base/2 + 16
                    CONNECT d1 -> d2
                    CONNECT d2 -> d3
                END
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 9. Connections (with dottedId)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§9 Connections: dotted identifiers across modules")
    void connectionsDottedId() {
        String code = """
                BEGIN
                    MODULE Encoder BEGIN
                        LAYER Conv2D conv filters=64 kernel=(3,3)
                        LAYER ReLU   relu flag=1
                        CONNECT conv -> relu
                    END
                    MODULE Decoder BEGIN
                        LAYER Conv2D deconv filters=64 kernel=(3,3)
                    END
                    // Dotted identifiers
                    CONNECT Encoder.conv -> Decoder.deconv
                END
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 10. Control Flow (FOR / IF)
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§10 FOR: repeated local structure (no name templating)")
    void controlFlowFor() {
        String code = """
                BEGIN
                    FOR i FROM 0 TO 2
                    BEGIN
                        // Inside the loop we keep layers anonymous or locally named
                        LAYER Dense h units=64 activation=ReLU
                        LAYER Dense n units=64 activation=ReLU
                        CONNECT h -> n
                    END
                END
                """;
        assertParses(code);
    }

    @Test
    @DisplayName("§10 IF: simple conditional branch")
    void controlFlowIf() {
        String code = """
                BEGIN
                    LET dropout = 0.1;
                    IF dropout == 0.1
                    BEGIN
                        LAYER Dropout d p=dropout
                    END
                    ELSE
                    BEGIN
                        LAYER Dropout d p=0.2
                    END
                END
                """;
        assertParses(code);
    }

    @Test
    @DisplayName("§10 IF: nested condition example")
    void controlFlowIfNested() {
        String code = """
                BEGIN
                    LET mode = 1;
                    IF mode == 0
                    BEGIN
                        LAYER BatchNorm bn momentum=0.9
                    END
                    ELSE
                    BEGIN
                        IF mode == 1
                        BEGIN
                            LAYER ReLU act flag=1
                        END
                    END
                END
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 11. Modules
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§11 Modules: declaration and call")
    void modules() {
        String code = """
                MODULE Encoder BEGIN
                    LAYER Conv2D conv1 filters=64 kernel=(3,3)
                    LAYER ReLU   relu1 flag=1
                    CONNECT conv1 -> relu1
                END
                BEGIN
                    Encoder
                END
                """;
        assertParses(code);
    }

    @Test
    @DisplayName("§11 Modules: encoder-decoder pair and connection")
    void modulesEncoderDecoder() {
        String code = """
                MODULE Encoder BEGIN
                    LAYER Conv2D conv1 filters=128 kernel=(3,3)
                    LAYER ReLU   relu1 flag=1
                    CONNECT conv1 -> relu1
                END
                MODULE Decoder BEGIN
                    LAYER Dense dense1 units=256
                    LAYER Dense out units=10 activation=Softmax
                    CONNECT dense1 -> out
                END
                CONNECT Encoder -> Decoder
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 12. Macros
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§12 Macros: define and call")
    void macros() {
        String code = """
                DEFINE MLP(hidden, out) BEGIN
                    LAYER Dense L1 units=hidden activation="relu"
                    LAYER Dense L2 units=out activation="softmax"
                    CONNECT L1 -> L2
                END
                BEGIN
                    MLP(128, 10)
                END
                """;
        assertParses(code);
    }

    @Test
    @DisplayName("§12 Macros: nested macro usage")
    void macrosNested() {
        String code = """
                DEFINE Block(x) BEGIN
                    LAYER Dense dense units=128
                    CONNECT x -> dense
                END
                DEFINE DeepNetwork(input) BEGIN
                    Block(input)
                    Block(dense)
                END
                BEGIN
                    LAYER Input in shape=(3,224,224)
                    DeepNetwork(in)
                END
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 13. File References
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§13 FILE: single reference at the end")
    void fileRef() {
        String code = """
                BEGIN
                    LAYER Input in shape=(1,28,28)
                    LAYER Dense out units=10 activation="softmax"
                END
                FILE "mnist.json"
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 14. EXPAND
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§14 EXPAND: SPACE = 3D")
    void expandSpace3D() {
        String code = """
                BEGIN
                    EXPAND SPACE = 3D
                    BEGIN
                        LAYER Conv3D c filters=32 kernel=(3,3,3)
                        LAYER ReLU   a flag=1
                        CONNECT c -> a
                    END
                END
                """;
        assertParses(code);
    }

    @Test
    @DisplayName("§14 EXPAND: GROUP = Residual and custom ID")
    void expandGroupAndCustom() {
        String code = """
                BEGIN
                    EXPAND GROUP = Residual
                    BEGIN
                        LAYER Conv2D a filters=64 kernel=(3,3)
                        LAYER ReLU   b flag=1
                        CONNECT a -> b
                    END
                    EXPAND WithResidual
                    BEGIN
                        LAYER Dense p units=128
                    END
                END
                """;
        assertParses(code);
    }

    // ---------------------------------------------------------------------
    // 15. MiniResNet Example
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("§15 MiniResNet: macros + modules + connections + FILE")
    void miniResNet() {
        String code = """
                // MiniResNet Example
                BEGIN
                    // Input Layer
                    LAYER Input input shape=(3,224,224)

                    // Residual Block Macro
                    DEFINE ResidualBlock(x, filters) BEGIN
                        LAYER Conv2D conv1 filters=filters kernel=(3,3) padding=Same
                        LAYER ReLU   relu1 flag=1
                        LAYER Conv2D conv2 filters=filters kernel=(3,3) padding=Same
                        CONNECT x -> conv1
                        CONNECT conv1 -> relu1
                        CONNECT relu1 -> conv2
                    END

                    // Encoder Module
                    MODULE Encoder BEGIN
                        ResidualBlock(input, 64)
                        ResidualBlock(conv2, 128)
                    END

                    // Decoder Module
                    MODULE Decoder BEGIN
                        LAYER Dense dense1 units=256
                        LAYER Dense output units=10 activation=Softmax
                        CONNECT dense1 -> output
                    END

                    // Combine Encoder and Decoder
                    CONNECT Encoder -> Decoder
                END
                FILE "miniresnet.json"
                """;
        assertParses(code);
    }
}
