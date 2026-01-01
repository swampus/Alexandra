package io.github.swampus.alexandra.networkapi.weight.infrastructure.service;

import io.github.swampus.alexandra.networkapi.weight.domain.service.WeightsHasher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/** SHA-256 hasher for shape specs. */
public final class Sha256WeightsHasher implements WeightsHasher {

    @Override
    public String hashShapes(Map<String, int[]> shapes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            shapes.keySet().stream().sorted().forEach(k -> {
                md.update(k.getBytes(StandardCharsets.UTF_8));
                md.update((byte) 0);
                int[] dims = shapes.get(k);
                if (dims != null) {
                    for (int d : dims) {
                        md.update((byte) (d >>> 24));
                        md.update((byte) (d >>> 16));
                        md.update((byte) (d >>> 8));
                        md.update((byte) d);
                    }
                }
                md.update((byte) 1);
            });
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash shapes", e);
        }
    }
}
