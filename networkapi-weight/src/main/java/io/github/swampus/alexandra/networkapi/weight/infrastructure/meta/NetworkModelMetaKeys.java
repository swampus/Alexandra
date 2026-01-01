package io.github.swampus.alexandra.networkapi.weight.infrastructure.meta;

/** Standard meta keys for weights storage in NetworkModel.meta. */
public final class NetworkModelMetaKeys {

    private NetworkModelMetaKeys() {}

    public static final String WEIGHTS_CURRENT = "weights.current";
    public static final String WEIGHTS_VERSION = "weights.version";
    public static final String WEIGHTS_MANIFESTS = "weights.manifests"; // Map<String, WeightsManifest>
    public static final String WEIGHTS_SNAPSHOTS = "weights.snapshots"; // Map<String, Weights>
}
