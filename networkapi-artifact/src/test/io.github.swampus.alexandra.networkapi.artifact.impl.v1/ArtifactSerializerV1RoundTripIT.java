package io.github.swampus.alexandra.networkapi.artifact.impl.v1;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.InputLayer;
import io.github.swampus.alexandra.compiler.model.layer.OutputLayer;
import io.github.swampus.alexandra.networkapi.artifact.model.RuntimeLayer;
import io.github.swampus.alexandra.networkapi.artifact.model.RuntimeNetworkModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration contract test for ArtifactSerializerV1.
 *
 * <p>This test verifies the end-to-end round-trip:
 * <pre>
 * NetworkModel → artifact(JSON) → RuntimeNetworkModel
 * </pre>
 *
 * <p>If this test fails, it indicates a breaking change
 * in artifact format or runtime compatibility.</p>
 */
class ArtifactSerializerV1RoundTripIT {

    private final ArtifactSerializerV1 serializer = new ArtifactSerializerV1();

    @Test
    @DisplayName("Round-trip: compiled model → artifact → runtime model (structure & params preserved)")
    void fullRoundTripContractTest() {
        // ------------------------------------------------------------------
        // GIVEN: a realistic compiled network model
        // ------------------------------------------------------------------
        NetworkModel model = new NetworkModel();

        model.getMeta().put("compiler", "nureonlang-compiler");
        model.getMeta().put("compilerVersion", "0.9.0");
        model.getMeta().put("purpose", "integration-test");

        model.addLayer(new InputLayer("input", 8));
        model.addLayer(new OutputLayer("output", 1, "sigmoid"));

        // ------------------------------------------------------------------
        // WHEN: model is serialized to artifact and deserialized back
        // ------------------------------------------------------------------
        String artifact = serializer.serialize(model);
        RuntimeNetworkModel runtime = serializer.deserialize(artifact);

        // ------------------------------------------------------------------
        // THEN: runtime model is structurally and semantically correct
        // ------------------------------------------------------------------
        assertThat(runtime).isNotNull();

        // --- inputs / outputs ---
        assertThat(runtime.getInputLayers())
                .containsExactly("input");

        assertThat(runtime.getOutputLayers())
                .containsExactly("output");

        // --- layers ---
        assertThat(runtime.getLayers())
                .hasSize(2);

        RuntimeLayer inputLayer = runtime.getLayers().get(0);
        RuntimeLayer outputLayer = runtime.getLayers().get(1);

        assertThat(inputLayer.getName()).isEqualTo("input");
        assertThat(inputLayer.getType()).contains("InputLayer");

        assertThat(outputLayer.getName()).isEqualTo("output");
        assertThat(outputLayer.getType()).contains("OutputLayer");

        // --- layer parameters ---
        Map<String, Object> outputParams = outputLayer.getParams();
        assertThat(outputParams)
                .containsEntry("size", 1)
                .containsEntry("units", 1)
                .containsEntry("activation", "sigmoid");

        // --- metadata ---
        assertThat(runtime.getMeta())
                .containsEntry("compiler", "nureonlang-compiler")
                .containsEntry("compilerVersion", "0.9.0")
                .containsEntry("purpose", "integration-test");
    }
}

