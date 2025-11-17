package io.github.swampus.alexandra.compiler.model.layer;

import java.util.Map;
import java.util.Random;

/**
 * Implements dropout — a stochastic regularization layer that randomly zeroes
 * a subset of activations during training.
 *
 * <p><b>Important:</b> In production / inference mode dropout must be disabled.
 * This layer currently always behaves as inference-mode passthrough.</p>
 *
 * <p>During training, the correct behavior is:
 * <ul>
 *   <li>each activation is set to 0 with probability {@code rate},</li>
 *   <li>the remaining activations are scaled by {@code 1 / (1 - rate)}.</li>
 * </ul>
 * This ensures expectation-preserving behavior.</p>
 */
public class DropoutLayer extends Layer {

    private final double rate;
    private final int size;
    private final Random rng = new Random();

    /** Controls whether dropout is active. Default = false (inference mode). */
    private boolean training = false;

    public DropoutLayer(String name, double rate) {
        super(name);
        this.rate = rate;
        this.size = -1;
    }

    public DropoutLayer(String name, double rate, int size) {
        super(name);
        this.rate = rate;
        this.size = size;
    }

    /**
     * Performs dropout only when {@code training == true}.
     * In inference mode acts as a no-op pass-through.
     */
    @Override
    public double[] forward(Map<String, double[]> inputByName) {
        double[] input = resolveInput(inputByName);
        if (input == null) {
            throw new IllegalArgumentException(
                    "Input not found for DropoutLayer: " + getName()
            );
        }

        // Inference mode: dropout disabled
        if (!training) {
            double[] out = new double[input.length];
            System.arraycopy(input, 0, out, 0, input.length);
            return out;
        }

        // Training mode: apply dropout mask
        double keepProb = 1.0 - rate;
        double scale = keepProb > 0 ? (1.0 / keepProb) : 0.0;

        double[] out = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            if (rng.nextDouble() < rate) {
                out[i] = 0.0;
            } else {
                out[i] = input[i] * scale;
            }
        }

        return out;
    }

    /** Enables or disables training mode. */
    public void setTraining(boolean training) {
        this.training = training;
    }

    public boolean isTraining() {
        return training;
    }

    @Override
    public int getSize() {
        return size >= 0 ? size : -1;
    }

    @Override
    public String getActivation() {
        return null;
    }

    @Override
    public Object getShape() {
        // Dropout is shape-preserving
        return null;
    }


    // -------------------------------------------------------------------------
    // TODOs — tasks for students / contributors
    // -------------------------------------------------------------------------

    // TODO: (1) Propagate and return correct shape from input instead of null.
    // TODO: (2) Integrate dropout with global training/inference mode:
    //             - compiler-level flag
    //             - runtime context flag
    //             - per-layer override
    // TODO: (3) Add deterministic mode for reproducible tests (fixed RNG seed).
    // TODO: (4) Extend forwardUniversal() — support multi-dimensional tensors.
    // TODO: (5) Add validator rule: dropout rate must be 0.0–1.0.
    // TODO: (6) Add unit tests:
    //             - inference passthrough,
    //             - proper dropout mask,
    //             - expectation preservation,
    //             - shape preservation.
    // TODO: (7) Add an optional mask output for debugging (similar to PyTorch).
    // TODO: (8) Add support for structured dropout: spatial dropout, channel dropout.
}
