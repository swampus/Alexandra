package io.github.swampus.alexandra.networkapi.artifact.impl.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.networkapi.artifact.model.RuntimeLayer;
import io.github.swampus.alexandra.networkapi.artifact.model.RuntimeNetworkModel;
import io.github.swampus.alexandra.networkapi.artifact.spi.ArtifactSerializer;
import io.github.swampus.alexandra.networkapi.artifact.spi.exception.ArtifactDeserializationException;
import io.github.swampus.alexandra.networkapi.artifact.spi.exception.ArtifactSerializationException;

import java.util.stream.Collectors;

public class ArtifactSerializerV1 implements ArtifactSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String formatVersion() {
        return "artifact-v1";
    }

    @Override
    public String serialize(NetworkModel model) {
        try {
            RuntimeNetworkModel runtime = toRuntimeModel(model);
            return MAPPER.writeValueAsString(runtime);
        } catch (Exception e) {
            throw new ArtifactSerializationException(
                    "Failed to serialize NetworkModel to artifact-v1 format", e
            );
        }
    }

    @Override
    public RuntimeNetworkModel deserialize(String compiledIr) {
        try {
            return MAPPER.readValue(compiledIr, RuntimeNetworkModel.class);
        } catch (Exception e) {
            throw new ArtifactDeserializationException(
                    "Failed to deserialize artifact-v1 into RuntimeNetworkModel", e
            );
        }
    }

    private RuntimeNetworkModel toRuntimeModel(NetworkModel model) {
        return RuntimeNetworkModel.builder()
                .layers(
                        model.getAllLayers().stream()
                                .map(this::toRuntimeLayer)
                                .collect(Collectors.toList())
                )
                .inputLayers(
                        model.getInputLayers().stream()
                                .map(Layer::getName)
                                .toList()
                )
                .outputLayers(
                        model.getOutputLayers().stream()
                                .map(Layer::getName)
                                .toList()
                )
                .meta(model.getMeta())
                .build();
    }

    private RuntimeLayer toRuntimeLayer(Layer layer) {
        return RuntimeLayer.builder()
                .name(layer.getName())
                .type(layer.getClass().getSimpleName())
                .params(layer.getParams())
                .build();
    }
}
