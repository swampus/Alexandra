package io.github.swampus.alexandra.networkapi.weight.application.port;

import io.github.swampus.alexandra.networkapi.weight.domain.model.*;

import java.util.List;

/**
 * Stores and loads weights snapshots.
 *
 * <p>In v1, an implementation may store weights in {@code NetworkModel.meta}.
 * In v2+, implementations may store weights in files/DB/registry.</p>
 */
public interface WeightsStorePort {

    void save(String networkId, WeightsVersion version, Weights weights, WeightsManifest manifest);

    Weights load(String networkId, WeightsVersion version);

    WeightsManifest loadManifest(String networkId, WeightsVersion version);

    List<WeightsVersion> listVersions(String networkId);
}
