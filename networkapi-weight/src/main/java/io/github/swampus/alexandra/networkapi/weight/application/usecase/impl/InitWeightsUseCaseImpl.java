package io.github.swampus.alexandra.networkapi.weight.application.usecase.impl;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.port.ShapeSpecProviderPort;
import io.github.swampus.alexandra.networkapi.weight.application.usecase.InitWeightsUseCase;
import io.github.swampus.alexandra.networkapi.weight.domain.init.InitMode;
import io.github.swampus.alexandra.networkapi.weight.domain.model.Weights;
import io.github.swampus.alexandra.networkapi.weight.domain.service.WeightsValidator;
import io.github.swampus.alexandra.networkapi.weight.infrastructure.meta.NetworkModelMetaKeys;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Deterministic weights initializer.
 *
 * <p>Uses {@code seed} for reproducibility. For Xavier/He, per-key randomness is derived
 * from the base seed and the key's hash to avoid order-dependence.</p>
 */
public final class InitWeightsUseCaseImpl implements InitWeightsUseCase {

    private final WeightsValidator validator;
    private final ShapeSpecProviderPort<ModelWithMeta> specProvider;

    public InitWeightsUseCaseImpl(WeightsValidator validator, ShapeSpecProviderPort<ModelWithMeta> specProvider) {
        this.validator = Objects.requireNonNull(validator, "validator");
        this.specProvider = Objects.requireNonNull(specProvider, "specProvider");
    }

    @Override
    public void init(ModelWithMeta model, InitWeightsCommand command) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(command.mode(), "mode");
        Objects.requireNonNull(command.version(), "version");

        Map<String, int[]> shapes = specProvider.paramShapes(model);
        if (shapes.isEmpty()) {
            throw new IllegalStateException("No parameter shapes provided by ShapeSpecProviderPort; cannot init weights");
        }

        Map<String, double[]> flat = new LinkedHashMap<>(shapes.size());
        for (var e : shapes.entrySet()) {
            String key = e.getKey();
            int len = product(e.getValue());
            long derivedSeed = mixSeed(command.seed(), key);
            double[] arr = initArray(command.mode(), len, derivedSeed, command.constantValue(), e.getValue());
            flat.put(key, arr);
        }

        Weights weights = new Weights(flat);
        validator.validateExact(shapes, weights);

        model.meta().put(NetworkModelMetaKeys.WEIGHTS_CURRENT, weights);
        model.meta().put(NetworkModelMetaKeys.WEIGHTS_VERSION, command.version());
    }

    private static double[] initArray(InitMode mode, int len, long seed, Double constantValue, int[] shape) {
        return switch (mode) {
            case ZEROS -> fill(len, 0.0);
            case ONES -> fill(len, 1.0);
            case CONSTANT -> {
                if (constantValue == null) throw new IllegalArgumentException("constantValue must be provided for CONSTANT");
                yield fill(len, constantValue);
            }
            case XAVIER_UNIFORM -> xavierUniform(len, seed, shape);
            case HE_NORMAL -> heNormal(len, seed, shape);
        };
    }

    private static double[] fill(int len, double v) {
        double[] a = new double[len];
        for (int i = 0; i < len; i++) a[i] = v;
        return a;
    }

    private static double[] xavierUniform(int len, long seed, int[] shape) {
        int fanIn = fanIn(shape);
        int fanOut = fanOut(shape);
        double limit = Math.sqrt(6.0 / Math.max(1, fanIn + fanOut));
        Random r = new Random(seed);
        double[] a = new double[len];
        for (int i = 0; i < len; i++) {
            a[i] = (r.nextDouble() * 2.0 - 1.0) * limit;
        }
        return a;
    }

    private static double[] heNormal(int len, long seed, int[] shape) {
        int fanIn = fanIn(shape);
        double std = Math.sqrt(2.0 / Math.max(1, fanIn));
        Random r = new Random(seed);
        double[] a = new double[len];
        for (int i = 0; i < len; i++) {
            a[i] = nextGaussian(r) * std;
        }
        return a;
    }

    private static double nextGaussian(Random r) {
        double u1 = Math.max(1e-12, r.nextDouble());
        double u2 = r.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }

    private static int product(int[] dims) {
        if (dims == null || dims.length == 0) return 0;
        long p = 1;
        for (int d : dims) {
            if (d <= 0) throw new IllegalArgumentException("Invalid dimension: " + d);
            p *= d;
            if (p > Integer.MAX_VALUE) throw new IllegalArgumentException("Tensor too large: " + p);
        }
        return (int) p;
    }

    private static long mixSeed(long base, String key) {
        long x = base ^ (long) key.hashCode() * 0x9E3779B97F4A7C15L;
        x ^= (x >>> 33);
        x *= 0xff51afd7ed558ccdL;
        x ^= (x >>> 33);
        x *= 0xc4ceb9fe1a85ec53L;
        x ^= (x >>> 33);
        return x;
    }

    private static int fanIn(int[] shape) {
        if (shape == null || shape.length == 0) return 1;
        if (shape.length == 1) return shape[0];
        return shape[shape.length - 1];
    }

    private static int fanOut(int[] shape) {
        if (shape == null || shape.length == 0) return 1;
        if (shape.length == 1) return shape[0];
        return shape[0];
    }
}
