package io.github.swampus.alexandra.nueronlang.parser.impl.mainflow;

import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangLexer;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test suite for maximal NureonLang syntax coverage.
 * Each test includes an English description of the network purpose.
 */
class NureonLangMainflowTest {

    private void assertParses(String code) {
        CharStream input = CharStreams.fromString(code);
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

    /**
     * 1. Simple feedforward network for addition: sums two numbers.
     * This network takes two inputs and produces their sum as output.
     */
    @Test
    public void testAdditionNetwork() {
        String code = """
                // Network for summing two numbers (a + b)
                BEGIN
                    LAYER INPUT name=a size=1
                    LAYER INPUT name=b size=1
                    LAYER DENSE name=add size=1 activation=linear
                    LAYER OUTPUT name=out size=1
                    CONNECT a -> add
                    CONNECT b -> add
                    CONNECT add -> out
                END
                FILE "add_weights.bin"
                """;
        assertParses(code);
    }

    /**
     * 2. Quadratic equation solver: outputs real roots.
     * The network receives coefficients a, b, c and produces roots.
     */
    @Test
    public void testQuadraticSolver() {
        String code = """
                // Network for solving quadratic equations: ax^2 + bx + c = 0
                BEGIN
                    LAYER INPUT name=a size=1
                    LAYER INPUT name=b size=1
                    LAYER INPUT name=c size=1
                    LAYER DENSE name=features size=4 activation=relu
                    LAYER DENSE name=roots size=2 activation=linear
                    LAYER OUTPUT name=output size=2
                    CONNECT a -> features
                    CONNECT b -> features
                    CONNECT c -> features
                    CONNECT features -> roots
                    CONNECT roots -> output
                END
                FILE "quadratic_weights.bin"
                """;
        assertParses(code);
    }

    /**
     * 3. Sequence predictor: predicts next element in number sequences.
     * Network demonstrates loop, let, and array access.
     */
    @Test
    public void testSequencePredictor() {
        String code = """
                // Sequence prediction: predict the next number in a sequence
                BEGIN
                    LAYER INPUT name=seq_in size=5
                    LET i = 0;
                    FOR i FROM 0 TO 3
                    BEGIN
                        LAYER DENSE name=hidden_[i] size=8 activation=tanh
                        CONNECT seq_in[i] -> hidden_[i]
                    END
                    LAYER DENSE name=final_hidden size=8 activation=relu
                    LAYER OUTPUT name=out size=1
                    CONNECT hidden_0 -> final_hidden
                    CONNECT hidden_1 -> final_hidden
                    CONNECT hidden_2 -> final_hidden
                    CONNECT hidden_3 -> final_hidden
                    CONNECT final_hidden -> out
                END
                FILE "seq_pred_weights.bin"
                """;
        assertParses(code);
    }

    /**
     * 4. Image classifier: recognizes images (e.g., MNIST digits).
     * Demonstrates shape, macro, and module usage.
     */
    @Test
    public void testImageClassifier() {
        String code = """
                // Image recognition network (e.g., for handwritten digits)
                DEFINE norm_block(input_name)
                BEGIN
                    LAYER DENSE name=normed size=128 activation=relu
                    CONNECT input_name -> normed
                END

                MODULE preprocess
                BEGIN
                    LAYER INPUT name=img_in shape=28 x 28
                    norm_block(img_in)
                END

                BEGIN
                    preprocess
                    LAYER DENSE name=features size=64 activation=relu
                    LAYER OUTPUT name=class_out size=10 activation=softmax
                    CONNECT normed -> features
                    CONNECT features -> class_out
                END
                FILE "mnist_weights.dat"
                """;
        assertParses(code);
    }

    /**
     * 5. ChatGPT-style Transformer block: language model.
     * Shows custom layers, parameters, block nesting, and fileRef.
     */
    @Test
    public void testChatGPTNetwork() {
        String code = """
                // Language model similar to ChatGPT (toy example)
                BEGIN
                    LAYER INPUT name=text_in size=256
                    LAYER TRANSFORMER name=block depth=12 heads=8 dim=256
                    LAYER OUTPUT name=word_probs size=50257 activation=softmax
                    CONNECT text_in -> block
                    CONNECT block -> word_probs
                END
                FILE "chatgpt_weights.pt"
                """;
        assertParses(code);
    }

    /**
     * 6. Dimensional expansion: expands ChatGPT network to 3D token embedding space.
     * Shows use of EXPAND, SPACE, and dimensionality features.
     */
    @Test
    public void testDimensionalExpand() {
        String code = """
                // Expanding the ChatGPT network to 3D token embedding space
                BEGIN
                    LAYER INPUT name=tokens size=512
                    LAYER TRANSFORMER name=transform3d depth=24 heads=16 dim=512
                    LAYER OUTPUT name=embeddings shape=3 x 512
                    CONNECT tokens -> transform3d
                    CONNECT transform3d -> embeddings
                    EXPAND SPACE = 3D
                    BEGIN
                        LAYER DENSE name=project3d size=512 activation=relu
                        CONNECT embeddings -> project3d
                    END
                END
                FILE "3dchatgpt_weights.pt"
                """;
        assertParses(code);
    }

    /**
     * 7. Custom test: symbolic logic evaluation network (demonstrates macros, let, conditions).
     * This network evaluates simple logical formulas from input.
     */
    @Test
    public void testSymbolicLogicEvaluator() {
        String code = """
                // Network for symbolic logic formula evaluation
                DEFINE logic_block(input_name, output_name)
                BEGIN
                    LAYER DENSE name=logic_1 size=8 activation=relu
                    LAYER DENSE name=logic_2 size=4 activation=relu
                    LAYER OUTPUT name=output_name size=1 activation=sigmoid
                    CONNECT input_name -> logic_1
                    CONNECT logic_1 -> logic_2
                    CONNECT logic_2 -> output_name
                END

                BEGIN
                    LAYER INPUT name=logic_in size=4
                    IF logic_in[0] == 1
                    BEGIN
                        logic_block(logic_in, logic_out)
                    END
                    ELSE
                    BEGIN
                        LAYER OUTPUT name=logic_out size=1 activation=linear
                        CONNECT logic_in -> logic_out
                    END
                END
                FILE "logic_weights.bin"
                """;
        assertParses(code);
    }

    @Test
    public void testMadGodHypernetwork() {
        String code = """
                // Hypernetwork of the Mad God in 66 dimensions
                DEFINE transcend_block(layer_idx)
                BEGIN
                    LAYER DENSE name=transcend_[layer_idx] size=2048 activation=chaos
                    CONNECT universe_in -> transcend_[layer_idx]
                END

                BEGIN
                    LAYER INPUT name=universe_in shape=(66, 66, 66)
                    LET i = 0;
                    FOR i FROM 0 TO 999
                    BEGIN
                        transcend_block(i)
                    END

                    EXPAND SPACE = 13D
                    BEGIN
                        FOR d FROM 0 TO 12
                        BEGIN
                            LAYER DENSE name=dimension_[d] size=666 activation=madness
                            CONNECT transcend_[(d+1)*13] -> dimension_[d]
                        END
                    END

                    LAYER OUTPUT name=divine_utterance size=1 activation=apocalypse
                    CONNECT dimension_12 -> divine_utterance
                END
                FILE "madgod66d_model.bin"
                """;
        assertParses(code);
    }

    @Test
    public void testNeurysteriaEmotionalNetwork() {
        String code = """
                // Neurysteria: neural network with emotional states (calm, angry, hysterical)
                DEFINE calm_behavior(input_name, output_name)
                BEGIN
                    LAYER DENSE name=calm1 size=16 activation=relu
                    LAYER DENSE name=calm2 size=8 activation=relu
                    LAYER OUTPUT name=output_name size=1 activation=sigmoid
                    CONNECT input_name -> calm1
                    CONNECT calm1 -> calm2
                    CONNECT calm2 -> output_name
                END

                DEFINE angry_behavior(input_name, output_name)
                BEGIN
                    LAYER DENSE name=angry1 size=32 activation=leaky_relu
                    LAYER DENSE name=angry2 size=16 activation=relu
                    LAYER OUTPUT name=output_name size=1 activation=sigmoid
                    CONNECT input_name -> angry1
                    CONNECT angry1 -> angry2
                    CONNECT angry2 -> output_name
                END

                DEFINE hysterical_behavior(input_name, output_name)
                BEGIN
                    LAYER DENSE name=hyst1 size=64 activation=elu
                    LAYER DENSE name=hyst2 size=32 activation=relu
                    LAYER OUTPUT name=output_name size=1 activation=sigmoid
                    CONNECT input_name -> hyst1
                    CONNECT hyst1 -> hyst2
                    CONNECT hyst2 -> output_name
                END

                BEGIN
                    LAYER INPUT name=stimulus size=4
                    LAYER INPUT name=rage_level size=1

                    IF rage_level < 3
                    BEGIN
                        calm_behavior(stimulus, mood_out)
                    END
                    ELSE
                    BEGIN
                        IF rage_level < 7
                        BEGIN
                            angry_behavior(stimulus, mood_out)
                        END
                        ELSE
                        BEGIN
                            hysterical_behavior(stimulus, mood_out)
                        END
                    END
                END
                FILE "neurysteria_emotion.bin"
                """;
        assertParses(code);
    }

    /**
     * 10. Expand ChatGPT-like network using a custom symmetry group.
     * This demonstrates how to upscale a pretrained model into a complex group-structured space (e.g., 20X group symmetry)
     * while preserving weights and allowing for mathematically consistent expansion without full retraining.
     */
    @Test
    public void testExpandChatGptWithCustomGroup() {
        String code = """
                // Expanding ChatGPT network using custom group symmetry for weight-preserving expansion.
                BEGIN
                    LAYER INPUT name=tokens size=512
                    LAYER TRANSFORMER name=block depth=12 heads=8 dim=512
                    LAYER OUTPUT name=word_probs size=50257 activation=softmax
                    CONNECT tokens -> block
                    CONNECT block -> word_probs
                    EXPAND GROUP = Cyclic20X
                    BEGIN
                        LAYER DENSE name=grouped_project size=20 activation=relu
                        CONNECT block -> grouped_project
                        LAYER OUTPUT name=words_grouped size=50257 activation=softmax
                        CONNECT grouped_project -> words_grouped
                    END
                END
                FILE "chatgpt_grouped_weights.bin"
                """;
        assertParses(code);
    }

}

