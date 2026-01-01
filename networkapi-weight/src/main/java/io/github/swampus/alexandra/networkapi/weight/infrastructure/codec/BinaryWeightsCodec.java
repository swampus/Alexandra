package io.github.swampus.alexandra.networkapi.weight.infrastructure.codec;

import io.github.swampus.alexandra.networkapi.weight.application.port.WeightsCodecPort;
import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simple binary codec for weights (no external dependencies).
 *
 * Format:
 *  - int entries
 *  - for each: UTF key, int length, then length doubles
 */
public final class BinaryWeightsCodec implements WeightsCodecPort {

    @Override
    public byte[] encode(Weights weights) {
        Objects.requireNonNull(weights, "weights");
        try (var baos = new ByteArrayOutputStream();
             var out = new DataOutputStream(baos)) {

            Map<String, double[]> flat = weights.flat();
            out.writeInt(flat.size());
            for (var e : flat.entrySet()) {
                out.writeUTF(e.getKey());
                double[] arr = e.getValue();
                if (arr == null) {
                    out.writeInt(-1);
                    continue;
                }
                out.writeInt(arr.length);
                for (double v : arr) out.writeDouble(v);
            }
            out.flush();
            return baos.toByteArray();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    @Override
    public Weights decode(byte[] payload) {
        Objects.requireNonNull(payload, "payload");
        try (var in = new DataInputStream(new ByteArrayInputStream(payload))) {
            int n = in.readInt();
            Map<String, double[]> map = new LinkedHashMap<>(Math.max(16, n));
            for (int i = 0; i < n; i++) {
                String key = in.readUTF();
                int len = in.readInt();
                if (len < 0) {
                    map.put(key, null);
                    continue;
                }
                double[] arr = new double[len];
                for (int j = 0; j < len; j++) arr[j] = in.readDouble();
                map.put(key, arr);
            }
            return new Weights(map);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    @Override
    public String format() {
        return "BIN";
    }
}
