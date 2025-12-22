package io.github.swampus.alexandra.networkapi.artifact.impl.v1;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.InputLayer;
import io.github.swampus.alexandra.compiler.model.layer.OutputLayer;
import io.github.swampus.alexandra.networkapi.artifact.model.RuntimeNetworkModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactSerializerV1Test {

    private final ArtifactSerializerV1 serializer = new ArtifactSerializerV1();

    @Test
    void shouldSerializeAndDeserializeSimpleNetwork() {
        // given
        NetworkModel model = new NetworkModel();

        model.addLayer(new InputLayer("input", 3));
        model.addLayer(new OutputLayer("output", 1, "sigmoid"));

        // when
        String ir = serializer.serialize(model);
        RuntimeNetworkModel runtime = serializer.deserialize(ir);

        // then
        assertThat(runtime).isNotNull();
        assertThat(runtime.getInputLayers()).containsExactly("input");
        assertThat(runtime.getOutputLayers()).containsExactly("output");
        assertThat(runtime.getLayers()).hasSize(2);
    }

    @Test
    void shouldPreserveLayerParams() {
        NetworkModel model = new NetworkModel();
        model.addLayer(new OutputLayer("out", 1, "sigmoid"));

        String ir = serializer.serialize(model);
        RuntimeNetworkModel runtime = serializer.deserialize(ir);

        var layer = runtime.getLayers().get(0);

        assertThat(layer.getParams())
                .containsEntry("activation", "sigmoid")
                .containsEntry("size", 1);
    }



}
