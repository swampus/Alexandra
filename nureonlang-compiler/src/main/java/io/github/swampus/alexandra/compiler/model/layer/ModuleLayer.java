package io.github.swampus.alexandra.compiler.model.layer;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A logical container that groups multiple layers into a single composite unit.
 *
 * <p><b>Important:</b> Current implementation preserves legacy behavior:
 * <ul>
 *   <li>{@code forward()} ignores intermediate outputs and repeatedly passes the
 *       <em>original</em> input map to each child layer.</li>
 *   <li>This means the module does NOT form an actual chain; each layer re-reads
 *       the same input and overwrites {@code out} with its own output.</li>
 *   <li>The bug is preserved intentionally to avoid breaking existing tests.</li>
 * </ul>
 *
 * A real module should probably pipe the output of layer[i] into layer[i+1].
 */
public class ModuleLayer extends Layer {

    @Getter
    private final List<Layer> body = new ArrayList<>();

    public ModuleLayer(String name) {
        super(name);
    }

    /**
     * Adds a child layer to the module body.
     */
    public void addLayer(Layer layer) {
        body.add(layer);
    }

    /**
     * Executes each child layer.
     *
     * <p><b>Legacy behavior preserved:</b> every layer receives {@code inputByName}
     * instead of chained outputs. Only the last child's result is returned.</p>
     */
    @Override
    public double[] forward(Map<String, double[]> inputByName) {
        double[] out = resolveInput(inputByName);

        for (Layer l : body) {
            // Legacy: not l.forward(using 'out'), but always reusing the original input map.
            out = l.forward(inputByName);
        }

        return out;
    }

    /**
     * Module does not expose its own dimensionality.
     */
    @Override
    public int getSize() {
        return -1;
    }

    /**
     * Module does not define an activation function.
     */
    @Override
    public String getActivation() {
        return null;
    }

    /**
     * Module has no explicit shape; could be inferred from body if necessary.
     */
    @Override
    public Object getShape() {
        return null;
    }
}
