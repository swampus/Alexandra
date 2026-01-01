package io.github.swampus.alexandra.networkapi.weight.infrastructure.meta;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.port.WeightsStorePort;
import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;
import io.github.swampus.alexandra.networkapi.weight.domain.model.WeightsManifest;
import io.github.swampus.alexandra.networkapi.weight.domain.model.WeightsVersion;

import java.util.*;

/**
 * In-memory weights store backed by {@link ModelWithMeta#meta()} ()}.
 *
 * <p>This is the v1 default: fast, simple, no external storage.</p>
 */
public final class NetworkModelMetaWeightsStore implements WeightsStorePort {

    private final ModelWithMeta model;
    private final String networkId;

    public NetworkModelMetaWeightsStore(ModelWithMeta model, String networkId) {
        this.model = Objects.requireNonNull(model, "model");
        this.networkId = Objects.requireNonNull(networkId, "networkId");
        ensureMaps();
    }

    @Override
    public void save(String networkId, WeightsVersion version, Weights weights, WeightsManifest manifest) {
        if (!this.networkId.equals(networkId)) {
            throw new IllegalArgumentException("This store is bound to networkId=" + this.networkId);
        }
        snapshots().put(version.value(), weights);
        manifests().put(version.value(), manifest);
        model.meta().put(NetworkModelMetaKeys.WEIGHTS_CURRENT, weights);
        model.meta().put(NetworkModelMetaKeys.WEIGHTS_VERSION, version.value());
    }

    @Override
    public Weights load(String networkId, WeightsVersion version) {
        if (!this.networkId.equals(networkId)) {
            throw new IllegalArgumentException("This store is bound to networkId=" + this.networkId);
        }
        Weights w = snapshots().get(version.value());
        if (w == null) throw new NoSuchElementException("No weights version: " + version.value());
        return w;
    }

    @Override
    public WeightsManifest loadManifest(String networkId, WeightsVersion version) {
        if (!this.networkId.equals(networkId)) {
            throw new IllegalArgumentException("This store is bound to networkId=" + this.networkId);
        }
        WeightsManifest m = manifests().get(version.value());
        if (m == null) throw new NoSuchElementException("No manifest for version: " + version.value());
        return m;
    }

    @Override
    public List<WeightsVersion> listVersions(String networkId) {
        if (!this.networkId.equals(networkId)) {
            throw new IllegalArgumentException("This store is bound to networkId=" + this.networkId);
        }
        return snapshots().keySet().stream().map(WeightsVersion::of).toList();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Weights> snapshots() {
        return (Map<String, Weights>) model.meta().get(NetworkModelMetaKeys.WEIGHTS_SNAPSHOTS);
    }

    @SuppressWarnings("unchecked")
    private Map<String, WeightsManifest> manifests() {
        return (Map<String, WeightsManifest>) model.meta().get(NetworkModelMetaKeys.WEIGHTS_MANIFESTS);
    }

    private void ensureMaps() {
        model.meta().computeIfAbsent(NetworkModelMetaKeys.WEIGHTS_SNAPSHOTS, k -> new LinkedHashMap<String, Weights>());
        model.meta().computeIfAbsent(NetworkModelMetaKeys.WEIGHTS_MANIFESTS, k -> new LinkedHashMap<String, WeightsManifest>());
    }
}
