package io.github.swampus.alexandra.networkapi.compiler.infrastructure.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.dto.shared.instruction.InstructionDto;
import io.github.swampus.alexandra.dto.shared.network.NNetworkDto;
import io.github.swampus.alexandra.dto.shared.network.NetworkEdgeDto;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkModelMapperPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure-level mapper responsible for converting internal
 * {@link NetworkModel} representations into transport-layer
 * {@link NNetworkDto} objects.
 */
public class NetworkModelMapper implements NetworkModelMapperPort {

    private static final String DEFAULT_CHANNEL = "default";
    private static final String DIRECTION_FORWARD = "FORWARD";
    private static final String EDGE_TYPE_DATA = "DATA";
    private static final String STATE_COMPILED = "COMPILED";

    private final ObjectMapper objectMapper;

    public NetworkModelMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public NNetworkDto mapModelToDto(
            NetworkModel model,
            String source,
            InstructionDto irDto
    ) {
        List<NetworkEdgeDto> edges = extractEdges(model);
        String irJson = serializeIr(irDto);

        return new NNetworkDto(
                UUID.randomUUID().toString(), // id
                null,                         // name
                irJson,                       // ir
                null,                         // weights
                STATE_COMPILED,               // state
                edges,                        // edges
                null,                         // subNetworks
                null,                         // tasks
                null,                         // tags
                model.getMeta(),              // meta
                source,                       // nueronLangSource
                null                          // memory
        );
    }

    /**
     * Extracts directed edges from the internal network model.
     *
     * <p>Each edge represents a forward data-flow connection
     * between two layers.</p>
     */
    private List<NetworkEdgeDto> extractEdges(NetworkModel model) {
        List<NetworkEdgeDto> edges = new ArrayList<>();

        for (Layer from : model.getAllLayers()) {
            if (from.getOutputs() == null) {
                continue;
            }

            for (Layer to : from.getOutputs()) {
                edges.add(new NetworkEdgeDto(
                        from.getName(),
                        to.getName(),
                        DEFAULT_CHANNEL,
                        DIRECTION_FORWARD,
                        EDGE_TYPE_DATA,
                        Map.of()
                ));
            }
        }

        return edges;
    }

    /**
     * Serializes the intermediate representation (IR) to JSON.
     */
    private String serializeIr(InstructionDto irDto) {
        if (irDto == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(List.of(irDto));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to serialize intermediate representation (IR)", e
            );
        }
    }
}
