
package io.github.swampus.alexandra.networkapi.registry.application.usecase.network;

import io.github.swampus.alexandra.networkapi.registry.domain.model.network.NetworkArtifact;
import io.github.swampus.alexandra.networkapi.registry.domain.model.network.NetworkState;

import java.util.List;
import java.util.Optional;

public interface NetworkRegistryManagerUseCase {
    NetworkArtifact registerNetwork(NetworkArtifact network);
    void removeNetwork(String id);
    List<NetworkArtifact> getAllNetworks();
    Optional<NetworkArtifact> findById(String id);
    List<NetworkArtifact> findByState(NetworkState state);
}
