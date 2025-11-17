package io.github.swampus.alexandra.compiler.model.layer;

import io.github.swampus.alexandra.ir.model.Instruction;

import java.util.List;
import java.util.Map;

/**
 * Placeholder layer representing a compiled macro.
 *
 * <p>Currently this is a very thin wrapper: it stores macro metadata (body + params)
 * but does not actually execute the macro logic. The forward pass simply returns
 * the input vector associated with this layer's name.</p>
 *
 * <p>This is sufficient for wiring, validation and future extensions where the
 * macro is expanded at compile-time rather than executed at runtime.</p>
 */
// TODO: MacroLayer – future macro runtime support
//  1) Define a clear contract for how macros should be represented at runtime (if at all).
//  2) Decide whether MacroLayer should execute instructions at runtime or remain compile-time only.
//  3) If runtime execution is needed, add an interpreter for the Instruction body:
//       - Support basic ops (LAYER, CONNECT, IF, FOR, CALL) in a local subgraph.
//       - Handle input/output mapping between parent graph and macro body.
//  4) Integrate with ShapeAndDryRunValidator so macros can contribute shape information.
//  5) Add support for macro parameters (params map) with proper type handling and defaults.
//  6) Consider converting MacroLayer into a pure metadata node and removing it from the runtime graph.
//  7) Add unit tests covering:
//       - MacroLayer with no body (metadata only).
//       - Access to params and body for tooling/debugging.
//       - Behavior of forward() when inputByName does not contain this layer's name.
//  8) Decide how macros interact with serialization (IR export/import).
public class MacroLayer extends Layer {

    /**
     * IR instructions that define the macro body.
     * May be null or empty in the current placeholder implementation.
     */
    private List<Instruction> body;

    /**
     * Macro parameters (configuration, defaults, etc.).
     */
    private Map<String, Object> params;

    public MacroLayer(String name) {
        super(name);
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * Legacy placeholder behavior:
     * returns the vector associated with this layer's name from the input map.
     *
     * <p>No actual macro execution is performed here.</p>
     */
    @Override
    public double[] forward(Map<String, double[]> inputByName) {
        return inputByName.get(getName());
    }

    @Override
    public int getSize() {
        // Macros do not declare their own dimensionality at this layer.
        return -1;
    }

    @Override
    public String getActivation() {
        // Macros are not activation layers.
        return null;
    }

    @Override
    public Object getShape() {
        // Shape is unspecified; could be proxied from the expanded body in the future.
        return null;
    }

    public List<Instruction> getBody() {
        return body;
    }

    public void setBody(List<Instruction> body) {
        this.body = body;
    }

    @Override
    public Map<String, Object> getParams() {
        return params;
    }
}
