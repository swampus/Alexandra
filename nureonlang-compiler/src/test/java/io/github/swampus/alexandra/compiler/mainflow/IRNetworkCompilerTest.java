package io.github.swampus.alexandra.compiler.mainflow;

import io.github.swampus.alexandra.compiler.IRNetworkCompiler;
import io.github.swampus.alexandra.compiler.NetworkCompilerFacade;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.ConditionalLayer;
import io.github.swampus.alexandra.compiler.model.layer.DenseLayer;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.compiler.model.layer.OutputLayer;
import io.github.swampus.alexandra.compiler.validator.ValidationLevel;
import io.github.swampus.alexandra.infrastructure.NureonLangInfrastructureFacade;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IRNetworkCompilerTest {

    private NureonLangInfrastructureFacade nureonLangInfrastructureFacade
            = new NureonLangInfrastructureFacade();

    private NetworkCompilerFacade networkCompilerFacade =
            new NetworkCompilerFacade(ValidationLevel.STRUCTURAL);

    /**
     * 1. Simple feedforward network for addition: sums two numbers.
     * This network takes two inputs and produces their sum as output.
     */
    @Test
    void testFeedforwardNetworkCompiles() {
        String code = """
                BEGIN
                    LAYER INPUT name=x size=1
                    LAYER INPUT name=y size=1
                    LAYER DENSE name=sum size=1 activation=linear
                    LAYER OUTPUT name=out size=1
                    CONNECT x -> sum
                    CONNECT y -> sum
                    CONNECT sum -> out
                END
                FILE "sum.bin"
                """;
        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        NetworkModel model = networkCompilerFacade.compile(ir);

        assertNotNull(model.getLayer("sum"));
        assertEquals(4, model.getAllLayers().size());

        Layer x = model.getLayer("x");
        Layer y = model.getLayer("y");
        Layer sum = model.getLayer("sum");
        Layer out = model.getLayer("out");

        assertTrue(x.getOutputs().contains(sum));
        assertTrue(y.getOutputs().contains(sum));
        assertTrue(sum.getOutputs().contains(out));
    }

    /**
     * 2. Quadratic equation solver: outputs real roots.
     * The network receives coefficients a, b, c and produces roots.
     */
    @Test
    void testQuadraticSolverCompiles() {
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

        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        NetworkModel model = networkCompilerFacade.compile(ir);

        assertEquals(6, model.getAllLayers().size());
        assertNotNull(model.getLayer("a"));
        assertNotNull(model.getLayer("b"));
        assertNotNull(model.getLayer("c"));
        assertNotNull(model.getLayer("features"));
        assertNotNull(model.getLayer("roots"));
        assertNotNull(model.getLayer("output"));

        Layer a = model.getLayer("a");
        Layer b = model.getLayer("b");
        Layer c = model.getLayer("c");
        Layer features = model.getLayer("features");
        Layer roots = model.getLayer("roots");
        Layer output = model.getLayer("output");

        assertTrue(a.getOutputs().contains(features));
        assertTrue(b.getOutputs().contains(features));
        assertTrue(c.getOutputs().contains(features));
        assertTrue(features.getOutputs().contains(roots));
        assertTrue(roots.getOutputs().contains(output));
    }

    /**
     * 3. Sequence predictor: predicts next element in number sequences.
     * Network demonstrates loop, let, and array access.
     */
    @Test
    void testSequencePredictorCompiles() {
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
        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        System.out.println(ir);
        NetworkModel model = networkCompilerFacade.compile(ir);

        assertNotNull(model.getLayer("seq_in"));
        for (int i = 0; i < 4; i++) {
            Layer hidden = model.getLayer("hidden_" + i);
            var seqIn = model.getLayer("seq_in");
            assertNotNull(hidden);
            System.out.println("seq_in outputs: " + seqIn.getOutputs().stream().map(Layer::getName).toList());
            System.out.println("hidden_" + i + " inputs: " + hidden.getInputs().stream().map(Layer::getName).toList());
            assertTrue(seqIn.getOutputs().contains(hidden) || hidden.getInputs().contains(seqIn));
        }
        assertNotNull(model.getLayer("final_hidden"));
        assertNotNull(model.getLayer("out"));
        Layer final_hidden = model.getLayer("final_hidden");
        for (int i = 0; i < 4; i++) {
            Layer hidden = model.getLayer("hidden_" + i);
            assertTrue(hidden.getOutputs().contains(final_hidden));
        }
        Layer out = model.getLayer("out");
        assertTrue(final_hidden.getOutputs().contains(out));
    }

    /**
     * 4. Image classifier: recognizes images (e.g., MNIST digits).
     * Проверка shape, цепочки слоев и передачи данных через макрос norm_block.
     */
    @Test
    public void testImageClassifier() {
        String code = """
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
        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        assertNotNull(ir);

        // Только если твой компилятор поддерживает макросы/модули:
        NetworkModel model = networkCompilerFacade.compile(ir);

        // Проверяем ключевые слои (все, кроме normed и img_in, если макросы не поддерживаются)
        assertNotNull(model.getLayer("features"));
        assertNotNull(model.getLayer("class_out"));

        Layer features = model.getLayer("features");
        Layer class_out = model.getLayer("class_out");
        assertEquals(64, features.getSize());
        assertEquals(10, class_out.getSize());
        // Проверяем связь
        assertTrue(features.getOutputs().contains(class_out));
        // Если реализован normed — тоже проверить
        Layer normed = model.getLayer("normed");
        if (normed != null) {
            assertEquals(128, normed.getSize());
            assertTrue(normed.getOutputs().contains(features));
        }
    }

    /**
     * 5. ChatGPT-style Transformer block: language model.
     * Проверяем кастомный слой TRANSFORMER, параметры heads/dim/depth, цепочку CONNECT.
     */
    @Test
    public void testChatGPTNetwork() {
        String code = """
                BEGIN
                    LAYER INPUT name=text_in size=256
                    LAYER TRANSFORMER name=block depth=12 heads=8 dim=256
                    LAYER OUTPUT name=wordProbs size=50257 activation=softmax
                    CONNECT text_in -> block
                    CONNECT block -> wordProbs
                END
                FILE "chatgpt_weights.pt"
                """;
        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        assertNotNull(ir);

        NetworkModel model = networkCompilerFacade.compile(ir);

        assertNotNull(model.getLayer("block"));
        assertNotNull(model.getLayer("text_in"));
        assertNotNull(model.getLayer("wordProbs"));
        Layer block = model.getLayer("block");
        Layer wordProbs = model.getLayer("wordProbs");

        // Проверяем параметры TRANSFORMER слоя (если они сохраняются)
        // Предполагается, что они хранятся в meta/params (иначе нужно достать через твой Layer класс)
        assertEquals(256, block.getSize()); // dim
        // Можно проверить еще activation/heads/depth, если твоя модель поддерживает их

        assertTrue(block.getOutputs().contains(wordProbs));
    }

    /**
     * 6. Dimensional expansion: EXPAND + shape проверка.
     */
    @Test
    public void testDimensionalExpand() {
        String code = """
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
        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        assertNotNull(ir);

        NetworkModel model = networkCompilerFacade.compile(ir);

        // Проверка shape и цепочки до project3d
        assertNotNull(model.getLayer("embeddings"));
        assertNotNull(model.getLayer("project3d"));

        Layer embeddings = model.getLayer("embeddings");
        Layer project3d = model.getLayer("project3d");

        assertTrue(embeddings.getOutputs().contains(project3d));
        assertTrue(embeddings.getShape().toString().contains("3") ||
                embeddings.getShape().toString().contains("3 x 512"));

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

        // Парсим и компилируем
        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        NetworkModel model = networkCompilerFacade.compile(ir);

        // Отладка, если надо
        DEBUG_OUTPUT(model);
        System.out.println("------");

        // Проверка входного слоя
        Layer logic_in = model.getLayer("logic_in");
        assertNotNull(logic_in);
        assertEquals(4, logic_in.getSize());

        // Получаем оба слоя logic_out
        List<Layer> logicOuts = model.getLayersByName("logic_out");
        assertEquals(2, logicOuts.size(), "Ожидалось два logic_out слоя: sigmoid и linear");

        // Отдельно получаем sigmoid и linear выходы
        Layer sigmoidOut = logicOuts.stream()
                .filter(l -> "sigmoid".equals(l.getActivation()))
                .findFirst()
                .orElse(null);
        Layer linearOut = model.getAllLayers().stream()
                .filter(l -> l.getName().startsWith("logic_out"))
                .filter(l -> "linear".equals(((OutputLayer) l).getActivation()))
                .findFirst()
                .orElse(null);

        model.getAllLayers().forEach(l -> {
            if (l instanceof OutputLayer) {
                System.out.println("Output: " + l.getName() + ", activation=" + ((OutputLayer) l).getActivation());
            }
        });

        assertNotNull(sigmoidOut, "Отсутствует logic_out с активацией sigmoid");

        assertNotNull(linearOut, "Отсутствует logic_out с активацией linear");

        assertEquals(1, sigmoidOut.getSize());
        assertEquals(1, linearOut.getSize());

        // Проверка ELSE-ветки
        assertTrue(
                logic_in.getOutputs().contains(linearOut) || linearOut.getInputs().contains(logic_in),
                "logic_in должен быть связан с linearOut"
        );

        // Поиск слоёв из макроса
        Layer logic_1 = model.getAllLayers().stream()
                .filter(l -> l.getName().startsWith("logic_1__"))
                .findFirst().orElse(null);
        Layer logic_2 = model.getAllLayers().stream()
                .filter(l -> l.getName().startsWith("logic_2__"))
                .findFirst().orElse(null);

        assertNotNull(logic_1, "Слой logic_1 из макроса не найден");
        assertNotNull(logic_2, "Слой logic_2 из макроса не найден");

        assertEquals(8, logic_1.getSize());
        assertEquals(4, logic_2.getSize());
        assertEquals("relu", logic_1.getActivation());
        assertEquals("relu", logic_2.getActivation());

        // Проверка связей THEN-ветки (из макроса)
        assertTrue(logic_1.getOutputs().contains(logic_2), "logic_1 должен быть связан с logic_2");
        assertTrue(logic_2.getOutputs().contains(sigmoidOut), "logic_2 должен быть связан с sigmoidOut");
    }

    private static void DEBUG_OUTPUT(NetworkModel model) {
        System.out.println("--- LAYERS IN MODEL ---");
        for (Layer l : model.getAllLayers()) {
            System.out.println("Layer: " + l.getName() + " (" + l.getClass().getSimpleName() + ")");
            if (l.getInputs() != null) {
                for (Layer inL : l.getInputs()) {
                    System.out.println("    <- input: " + inL.getName());
                }
            }
            if (l.getOutputs() != null) {
                for (Layer outL : l.getOutputs()) {
                    System.out.println("    -> output: " + outL.getName());
                }
            }
        }
    }

    /**
     * 8. Mad God Hypernetwork test: 1000 transcendence layers + 13D expand + divine output.
     */
    @Test
    void testMadGodHypernetwork() {

        String code = """
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


        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        NetworkModel model = networkCompilerFacade.compile(ir);

        System.out.println("NEW PARSE: " +
                new IRNetworkCompiler().replaceVarSmart("transcend_[(d+1)*13]", "d", 1));

        System.out.println("Test 1: " + new IRNetworkCompiler().replaceVarSmart("transcend_[(d+1)*13]", "d", 1)); // → transcend_26
        System.out.println("Test 2: " + new IRNetworkCompiler().replaceVarSmart("foo[d+2]", "d", 3));            // → foo5
        System.out.println("Test 3: " + new IRNetworkCompiler().replaceVarSmart("bar[(d+2)*10]", "d", 1));       // → bar30

        System.out.println("TEST PARSE: " + new IRNetworkCompiler().replaceVarSmart("transcend_[(d+1)*13]", "d", 1));
        System.out.println("TEST PARSE: " + new IRNetworkCompiler().replaceVarSmart("transcend_[(d+1)*13]", "d", 0));
        System.out.println("TEST PARSE: " + new IRNetworkCompiler().replaceVarSmart("dimension_[d]", "d", 12));
        System.out.println("TEST: " + new IRNetworkCompiler().replaceVarSmart("transcend_[(d+1)*13]", "d", 0));

        Map<String, String> bindings = Map.of("d", "3");
        String res = new IRNetworkCompiler().replaceVarsSmart("transcend_[(d+1)*13]", bindings);

        System.out.println("TEST::" + res);

        Layer universeIn = model.getLayer("universe_in");
        assertNotNull(universeIn, "universe_in layer должен быть создан");

        new IRNetworkCompiler().printAllLayerNames(model);

        Layer dimension12 = model.getLayer("dimension_12");
        assertNotNull(dimension12, "dimension_12 layer должен быть создан");

        Layer divineUtterance = model.getLayer("divine_utterance");
        assertNotNull(divineUtterance, "divine_utterance должен быть создан");

        // Шаг 3. Проверяем связи (outputs/inputs)
        // Проверка что dimension_12 ведёт в divine_utterance
        boolean found = false;
        if (dimension12.getOutputs() != null) {
            for (Layer out : dimension12.getOutputs()) {
                if (out == divineUtterance) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "dimension_12 должен быть соединён с divine_utterance");
        for (int i = 0; i < 1000; i++) {
            Layer l = model.getLayer("transcend_" + i);
            assertNotNull(l, "transcend_" + i + " Should be created");
        }

        assertEquals(2048, ((DenseLayer) model.getLayer("transcend_0")).getSize());

        // DEBUG: печать структуры
        for (Layer layer : model.getAllLayers()) {
            System.out.println("Layer: " + layer.getName() + " (" + layer.getClass() + ")");
            if (layer.getOutputs() != null) {
                for (Layer out : layer.getOutputs()) {
                    System.out.println("    connects to: " + out.getName() + " (" + out.getClass() + ")");
                }
            }
            if (layer.getInputs() != null) {
                for (Layer inL : layer.getInputs()) {
                    System.out.println("    receives from: " + inL.getName() + " (" + inL.getClass() + ")");
                }
            }
        }
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

        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        assertNotNull(ir);

        NetworkModel model = networkCompilerFacade.compile(ir);
        assertNotNull(model);

        // Проверка входных слоёв
        Layer stimulus = model.getLayer("stimulus");
        Layer rage = model.getLayer("rage_level");
        assertNotNull(stimulus);
        assertNotNull(rage);

        // mood_out должен быть output в любом из вариантов
        Layer mood = model.getLayer("mood_out");
        assertNotNull(mood);
        assertTrue(model.getOutputLayers().contains(mood));

        // Проверка связей (в одном из трёх возможных путей)
        List<String> expected = List.of("calm1", "calm2", "angry1", "angry2", "hyst1", "hyst2");
        for (String baseName : expected) {
            boolean present = model.getAllLayers().stream()
                    .map(Layer::getName)
                    .anyMatch(name -> name.startsWith(baseName + "__")); // "__" — уникальный суффикс из макроса/ветки
            assertTrue(present, "Layer with base name " + baseName + " was not generated");
        }
        long count = model.getAllLayers().stream()
                .map(Layer::getName)
                .filter(name -> name.startsWith("calm1__") || name.startsWith("angry1__") || name.startsWith("hyst1__"))
                .count();
        assertTrue(count >= 1, "Хотя бы одна ветка должна быть сгенерирована");
        boolean atLeastOnePathCompiled = model.getAllLayers().stream()
                .map(Layer::getName)
                .anyMatch(name ->
                        name.startsWith("calm1__") ||
                                name.startsWith("angry1__") ||
                                name.startsWith("hyst1__"));
        assertTrue(atLeastOnePathCompiled, "At least one behavior path (calm/angry/hyst) should be compiled");

        // Пример: если был выбран calm_behavior
        if (model.getLayer("calm1") != null) {
            Layer calm1 = model.getLayer("calm1");
            Layer calm2 = model.getLayer("calm2");
            assertNotNull(calm2);
            assertEquals(16, calm1.getSize());
            assertEquals("relu", calm1.getActivation());
            assertEquals(8, calm2.getSize());

            assertTrue(stimulus.getOutputs().contains(calm1));
            assertTrue(calm1.getOutputs().contains(calm2));

            System.out.println("calm2 -> outputs: " + calm2.getOutputs());
            System.out.println("mood layer: " + mood);
            assertTrue(calm2.getOutputs().contains(mood));
        }
    }

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

        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        assertNotNull(ir);
        System.out.println("EXPAND meta: " + ir.getMeta());

        NetworkModel model = networkCompilerFacade.compile(ir);
        assertNotNull(model);

        // Основные слои должны быть
        Layer tokens = model.getLayer("tokens");
        Layer block = model.getLayer("block");
        Layer wordProbs = model.getLayer("word_probs");
        Layer groupedProject = model.getLayer("grouped_project");
        Layer wordsGrouped = model.getLayer("words_grouped");

        assertNotNull(tokens);
        assertNotNull(block);
        assertNotNull(wordProbs);
        assertNotNull(groupedProject);
        assertNotNull(wordsGrouped);

        // Проверка размеров
        assertEquals(512, tokens.getSize());
        assertEquals(512, block.getSize());
        assertEquals(50257, wordProbs.getSize());
        assertEquals(20, groupedProject.getSize());
        assertEquals(50257, wordsGrouped.getSize());

        // Проверка активаций
        assertEquals("softmax", wordProbs.getActivation());
        assertEquals("relu", groupedProject.getActivation());
        assertEquals("softmax", wordsGrouped.getActivation());

        // Проверка связей
        assertTrue(tokens.getOutputs().contains(block));
        assertTrue(block.getOutputs().contains(wordProbs));
        assertTrue(block.getOutputs().contains(groupedProject));
        assertTrue(groupedProject.getOutputs().contains(wordsGrouped));

        Instruction expandInstr = ir.getBody().stream()
                .flatMap(b -> b.getBody().stream())
                .filter(instr -> instr.getOp() == OpCode.EXPAND)
                .findFirst().orElse(null);

        Object groupSymmetry = expandInstr != null ? expandInstr.getMeta().getOrDefault("GROUP", null) : null;

        assertEquals("Cyclic20X", groupSymmetry, "Group symmetry must be Cyclic20X");
    }

    @Test
    public void testSimpleIf() {
        String code = """
                    BEGIN
                        LAYER INPUT name=x size=2
                    IF x[0] == 1
                    BEGIN
                        LAYER OUTPUT name=out_true size=1 activation=linear
                        CONNECT x -> out_true
                    END
                    ELSE
                    BEGIN
                        LAYER OUTPUT name=out_false size=1 activation=linear
                        CONNECT x -> out_false
                    END
                END
                """;
        // Парсим в IR (здесь твой парсер)
        Instruction ir = nureonLangInfrastructureFacade.parseCode(code);
        System.out.println("IR: " + ir); // или ir.prettyPrint() если есть
        NetworkModel model = networkCompilerFacade.compile(ir);
        DEBUG_OUTPUT(model);

        // Проверим, что ConditionalLayer появился
        ConditionalLayer cond = (ConditionalLayer) model.getLayer("cond_x_0_1");
        assertNotNull(cond);

        Map<String, double[]> inputTrueMap = Map.of("x", new double[]{1, 0});
        double[] outTrue = cond.forward(inputTrueMap);

        Map<String, double[]> inputFalseMap = Map.of("x", new double[]{0, 0});
        double[] outFalse = cond.forward(inputFalseMap);

        // outTrue должен быть output из out_true, outFalse — из out_false
        // (тут можно проверить размер или что outTrue ≠ outFalse)
        assertEquals(1, outTrue.length);
        assertEquals(1, outFalse.length);
    }


}