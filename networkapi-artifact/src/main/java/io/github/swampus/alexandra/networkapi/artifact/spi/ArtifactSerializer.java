package io.github.swampus.alexandra.networkapi.artifact.spi;

import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.networkapi.artifact.model.RuntimeNetworkModel;

public interface ArtifactSerializer {

    String formatVersion();

    String serialize(NetworkModel model);

    RuntimeNetworkModel deserialize(String compiledIr);
}
