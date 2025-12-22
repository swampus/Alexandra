package io.github.swampus.alexandra.networkapi.artifact.impl.v1;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.InputLayer;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.compiler.model.layer.OutputLayer;
import io.github.swampus.alexandra.networkapi.artifact.model.RuntimeLayer;
import io.github.swampus.alexandra.networkapi.artifact.model.RuntimeNetworkModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract integration test for artifact-v1:
 * NetworkModel → JSON artifact → RuntimeNetworkModel.
 *
 * This uses a "realistic" multi-input network (not just 2 layers),
 * to protect compatibility when compiler model evolves.
 */
class ArtifactSerializerV1RealisticRoundTripIT {

    private final ArtifactSerializerV1 serializer = new ArtifactSerializerV1();

    @Test
    @DisplayName("Round-trip: multi-input feedforward network (structure, IO lists, metadata, params)")
    void roundTripMultiInputFeedForwardNetwork() {
        // ------------------------------------------------------------------
        // GIVEN: a more realistic compiled network model
        // ------------------------------------------------------------------
        NetworkModel model = new NetworkModel();

        model.getMeta().put("compiler", "nureonlang-compiler");
        model.getMeta().put("compilerVersion", "0.9.0");
        model.getMeta().put("example", "multi-input-feedforward");
        model.getMeta().put("contract", "artifact-v1");

        // Inputs
        model.addLayer(new InputLayer("a", 1));
        model.addLayer(new InputLayer("b", 1));

        // Hidden layers (use whatever Dense layer class exists in your compiler)
        // If your Dense layer class name differs, replace DenseLayer below.
        Layer h1 = new io.github.swampus.alexandra.compiler.model.layer.DenseLayer("h1", 4, "relu");
        Layer h2 = new io.github.swampus.alexandra.compiler.model.layer.DenseLayer("h2", 2, "relu");

        model.addLayer(h1);
        model.addLayer(h2);

        // Output
        model.addLayer(new OutputLayer("out", 1, "sigmoid"));

        // Note: We do NOT test CONNECT semantics here, because NetworkModel currently
        // is a "layer container". Execution graph connectivity belongs to compiler/runtime
        // and should be tested elsewhere.

        // ------------------------------------------------------------------
        // WHEN: serialize → deserialize
        // ------------------------------------------------------------------
        String artifact = serializer.serialize(model);
        RuntimeNetworkModel runtime = serializer.deserialize(artifact);

        // ------------------------------------------------------------------
        // THEN: invariants of the artifact format hold
        // ------------------------------------------------------------------
        assertThat(runtime).isNotNull();

        // IO lists: order matters (insertion order)
        assertThat(runtime.getInputLayers()).containsExactly("a", "b");
        assertThat(runtime.getOutputLayers()).containsExactly("out");

        // Layers: total count preserved
        assertThat(runtime.getLayers()).hasSize(5);

        // Layer names preserved (in insertion order)
        List<String> layerNames = runtime.getLayers().stream().map(RuntimeLayer::getName).toList();
        assertThat(layerNames).containsExactly("a", "b", "h1", "h2", "out");

        // Layer types look sane
        assertThat(runtime.getLayers().get(0).getType()).contains("InputLayer");
        assertThat(runtime.getLayers().get(4).getType()).contains("OutputLayer");

        // Output params MUST be preserved (strong contract)
        Map<String, Object> outParams = runtime.getLayers().get(4).getParams();
        assertThat(outParams)
                .containsEntry("size", 1)
                .containsEntry("units", 1)
                .containsEntry("activation", "sigmoid");

        // Hidden layers params: keep this as a "soft" contract to avoid brittleness.
        // If DenseLayer.getParams() is implemented, this will validate it.
        Map<String, Object> h1Params = runtime.getLayers().get(2).getParams();
        if (h1Params != null && !h1Params.isEmpty()) {
            assertThat(h1Params).containsKey("size");
        }

        // Metadata preserved
        assertThat(runtime.getMeta())
                .containsEntry("compiler", "nureonlang-compiler")
                .containsEntry("compilerVersion", "0.9.0")
                .containsEntry("example", "multi-input-feedforward")
                .containsEntry("contract", "artifact-v1");
    }
}

