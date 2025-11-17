package io.github.swampus.alexandra.compiler.validator.spi;

import io.github.swampus.alexandra.compiler.contract.OutputContract;
import io.github.swampus.alexandra.compiler.extensions.InvalidNetworkException;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;

import java.util.*;

/**
 * Static shape validation + lightweight "dry-run" without actual weights.
 *
 * <p>Supported:</p>
 * <ul>
 *   <li>Dense: size/units</li>
 *   <li>INPUT/OUTPUT: shape/size</li>
 *   <li>Activations/normalizations treated as shape-preserving</li>
 *   <li>Shape formats: "(3,224,224)" / "[3,224,224]" / "3 x 512"</li>
 * </ul>
 *
 * <p><b>Behavior:</b> Identical to the original implementation; changes are limited to
 * documentation and code style polish. No logging has been added to preserve runtime
 * output (warnings are still collected in-memory only).</p>
 *
 * @since 0.9.0
 */
public class ShapeAndDryRunValidator {

    private final LayerIntrospector li;
    private final int maxPasses;

    private OutputContract outputContract;

    /**
     * Creates a validator with default {@link OutputContract#THIN}.
     *
     * @param li        layer introspector (non-null)
     * @param maxPasses maximum number of shape-inference passes (>=1)
     */
    public ShapeAndDryRunValidator(LayerIntrospector li, int maxPasses) {
        this(li, maxPasses, OutputContract.THIN);
    }

    /**
     * Creates a validator with an explicit {@link OutputContract}.
     *
     * @param li        layer introspector (non-null)
     * @param maxPasses maximum number of shape-inference passes (>=1)
     * @param contract  output contract (non-null)
     */
    public ShapeAndDryRunValidator(LayerIntrospector li, int maxPasses, OutputContract contract) {
        this.li = Objects.requireNonNull(li, "LayerIntrospector is null");
        this.maxPasses = Math.max(1, maxPasses);
        this.outputContract = Objects.requireNonNull(contract, "OutputContract is null");
    }

    /**
     * Validates the network shapes and performs a lightweight dry-run check.
     * <p>Throws {@link InvalidNetworkException} on validation errors; otherwise returns silently.</p>
     *
     * @param model compiled network model (non-null)
     * @throws InvalidNetworkException on shape/dry-run validation failure
     */
    public void validate(NetworkModel model) throws InvalidNetworkException {
        List<String> errs = new ArrayList<>();
        List<String> warns = new ArrayList<>();

        List<Layer> layers = li.layers(model);
        if (layers.isEmpty()) {
            throw new InvalidNetworkException("No layers in NetworkModel.");
        }

        // (1) Seed known output shapes (INPUT/CONST/RESHAPE/etc.)
        Map<String, int[]> outShape = new HashMap<>();
        for (Layer l : layers) {
            li.intrinsicOutputShape(l, li.params(l)).ifPresent(shp -> outShape.put(li.name(l), shp));
        }

        // (2) Topological order by 'outputs'
        List<Layer> topo = topoSortByOutputs(layers, warns);

        // (3) Iterative shape inference
        boolean changed = true;
        int pass = 0;
        while (changed && pass++ < maxPasses) {
            changed = false;
            for (Layer u : topo) {
                String uname = li.name(u);

                int[][] inShapes = u.getInputs().stream()
                        .map(li::name)
                        .map(outShape::get)
                        .filter(Objects::nonNull)
                        .toArray(int[][]::new);

                int[] current = outShape.get(uname);
                int[] inferred = inferShape(
                        safeKind(li.kind(u)),
                        li.params(u),
                        inShapes,
                        current,
                        errs,
                        uname
                );
                if (inferred != null && !Arrays.equals(current, inferred)) {
                    outShape.put(uname, inferred);
                    changed = true;
                }
            }
        }

        // (4) Edge consistency for shape-preserving layers
        for (Layer u : layers) {
            String uname = li.name(u);
            int[] src = outShape.get(uname);
            for (Layer v : u.getOutputs()) {
                String vname = li.name(v);
                int[] dst = outShape.get(vname);
                String vkind = safeKind(li.kind(v));

                if (src == null) {
                    warns.add("Cannot infer output shape for '" + uname + "'; edge '" + uname + "->" + vname + "' may fail at runtime.");
                } else if (dst != null && preservesInputShape(vkind) && !shapeCompatible(src, dst)) {
                    errs.add("Shape mismatch (shape-preserving) " + uname + "->" + vname + ": "
                            + Arrays.toString(src) + " -> " + Arrays.toString(dst));
                }
                // For layers that change shape (Dense/Conv/Pooling/...), we do not compare out(u) vs out(v) here.
            }
        }

        // (5) Lightweight dry-run: ensure buffers are allocatable (positive size)
        if (errs.isEmpty()) {
            try {
                for (Layer l : topo) {
                    String n = li.name(l);
                    int[] shp = outShape.get(n);
                    if (shp == null) {
                        warns.add("Dry-run: unknown output shape at layer '" + n + "'");
                        continue;
                    }
                    int sz = product(shp);
                    if (sz <= 0) {
                        throw new IllegalStateException("Non-positive shape at '" + n + "': " + Arrays.toString(shp));
                    }
                }
            } catch (Exception e) {
                errs.add("Dry-run failed: " + e.getMessage());
            }
        }

        // Keep logging disabled to preserve behavior; caller may inspect/print warns if needed.
        // if (!warns.isEmpty()) { ... }

        if (!errs.isEmpty()) {
            throw new InvalidNetworkException(String.join("\n", errs));
        }
    }

    // ---------- Helpers (package-private/private) ----------

    private String safeKind(String k) {
        return (k == null) ? "" : k.toLowerCase(Locale.ROOT);
    }

    /**
     * Kahn's algorithm over outgoing edges.
     * Adds any remaining (cycled/unreachable) nodes to the tail to keep subsequent passes intact.
     */
    private List<Layer> topoSortByOutputs(List<Layer> nodes, List<String> warns) {
        Map<Layer, Integer> indeg = new IdentityHashMap<>();
        for (Layer l : nodes) indeg.put(l, 0);
        for (Layer l : nodes) {
            for (Layer v : l.getOutputs()) {
                indeg.computeIfPresent(v, (k, val) -> val + 1);
            }
        }
        Deque<Layer> q = new ArrayDeque<>();
        indeg.forEach((k, v) -> { if (v == 0) q.addLast(k); });

        List<Layer> out = new ArrayList<>();
        Set<Layer> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        while (!q.isEmpty()) {
            Layer u = q.removeFirst();
            if (!seen.add(u)) continue;
            out.add(u);
            for (Layer v : u.getOutputs()) {
                indeg.computeIfPresent(v, (k, val) -> val - 1);
                if (indeg.getOrDefault(v, 0) == 0) q.addLast(v);
            }
        }
        if (out.size() < nodes.size()) {
            warns.add("Cycle or unreachable layers detected: topoOrder=" + out.size() + " < total=" + nodes.size());
            // Append remaining nodes so later checks still see them
            for (Layer l : nodes) if (!out.contains(l)) out.add(l);
        }
        return out;
    }

    private boolean shapeCompatible(int[] a, int[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            int x = a[i], y = b[i];
            if (x != y && x != -1 && y != -1) return false; // -1 = wildcard/unknown
        }
        return true;
    }

    private int product(int[] a) {
        int p = 1;
        for (int v : a) p *= Math.max(1, v);
        return p;
    }

    /**
     * Minimal shape inference rules. Extend as needed for your layer types.
     */
    private int[] inferShape(String kind,
                             Map<String, Object> p,
                             int[][] ins,
                             int[] current,
                             List<String> errs,
                             String lname) {
        if (current != null) return current;

        // INPUT: use declared shape or scalar size
        if (kind.contains("input")) {
            int[] shp = asShape(p.get("shape"));
            if (shp != null) return shp;
            Integer sz = asInt(or(p, "size", "units", null));
            if (sz != null) return new int[]{sz};
            return null; // unknown is fine here
        }

        // OUTPUT
        if (kind.contains("output")) {
            int[] declared = asShape(p.get("shape"));
            Integer sz = asInt(or(p, "size", "units", null));
            if (declared == null && sz != null) declared = new int[]{sz};

            int[] in = (ins.length > 0) ? ins[0] : null;

            if (outputContract == OutputContract.THIN) {
                // Thin OUTPUT must preserve shape; mismatch is an error but we still propagate the declared
                if (declared != null && in != null && !shapeCompatible(in, declared)) {
                    errs.add("OUTPUT '" + lname + "': input shape " + Arrays.toString(in)
                            + " does not match declared " + Arrays.toString(declared)
                            + ". Add a projection layer (e.g., DENSE size="
                            + (declared.length == 1 ? declared[0] : Arrays.toString(declared)) + ").");
                    return declared; // downstream sees target shape
                }
                return (in != null) ? in : declared; // shape-preserving
            } else {
                // Thick OUTPUT projects to declared shape if provided
                return (declared != null) ? declared : in;
            }
        }

        // Dense: [*, D] -> [*, units]
        if (kind.contains("dense")) {
            Integer units = asInt(or(p, "units", "size", null));
            if (units == null) {
                errs.add("Layer '" + lname + "' Dense: missing param 'units/size'");
                return null;
            }
            if (ins.length == 0 || ins[0] == null) return new int[]{units};
            int[] in = ins[0];
            if (in.length == 1) return new int[]{units};
            int[] out = Arrays.copyOf(in, in.length);
            out[out.length - 1] = units;
            return out;
        }

        // Reshape(shape=...)
        if (kind.contains("reshape")) {
            int[] shp = asShape(p.get("shape"));
            if (shp == null) {
                errs.add("Layer '" + lname + "' Reshape: missing 'shape'");
                return null;
            }
            return shp;
        }

        // Activations/normalizations/dropouts are shape-preserving by type (not by activation param)
        if (kind.equals("activation")
                || (kind.startsWith("relu") && kind.contains("relu"))
                || (kind.startsWith("sigmoid") && kind.contains("sigmoid"))
                || (kind.startsWith("tanh") && kind.contains("tanh"))
                || (kind.startsWith("softmax") && kind.contains("softmax"))
                || kind.contains("norm") || kind.contains("batchnorm") || kind.contains("layernorm")
                || kind.contains("dropout")) {
            return ins.length > 0 ? ins[0] : null;
        }

        // Default: pass-through first input shape
        return ins.length > 0 ? ins[0] : null;
    }

    private boolean preservesInputShape(String kind) {
        if (kind == null) return false;
        return kind.equals("activation")
                || (kind.startsWith("relu") && kind.contains("relu"))
                || (kind.startsWith("sigmoid") && kind.contains("sigmoid"))
                || (kind.startsWith("tanh") && kind.contains("tanh"))
                || (kind.startsWith("softmax") && kind.contains("softmax"))
                || kind.contains("norm") || kind.contains("batchnorm") || kind.contains("layernorm")
                || kind.contains("dropout");
        // Intentionally excludes "output" — it may change shape in THICK mode.
    }

    private Object or(Map<String, Object> p, String a, String b, Object def) {
        if (p.containsKey(a)) return p.get(a);
        if (p.containsKey(b)) return p.get(b);
        return def;
    }

    private Integer asInt(Object o) {
        if (o instanceof Integer i) return i;
        if (o instanceof Number n) return n.intValue();
        if (o instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (Exception ignore) { /* keep null */ }
        }
        return null;
    }

    private int[] asShape(Object o) {
        if (o instanceof int[] a) return a;
        if (o instanceof List<?> l) {
            int[] a = new int[l.size()];
            for (int i = 0; i < l.size(); i++) a[i] = asInt(l.get(i));
            return a;
        }
        if (o instanceof String s) {
            s = s.trim();
            // "3 x 512" or "3x512"
            if (s.contains("x") || s.contains("X")) {
                String[] parts = s.replaceAll("\\s", "").toLowerCase(Locale.ROOT).split("x");
                int[] a = new int[parts.length];
                for (int i = 0; i < parts.length; i++) a[i] = Integer.parseInt(parts[i]);
                return a;
            }
            // "(3, 224, 224)" | "[3,224,224]"
            String[] parts = s.replaceAll("[()\\[\\]\\s]", "").split(",");
            if (parts.length == 1 && parts[0].isEmpty()) return null;
            int[] a = new int[parts.length];
            for (int i = 0; i < parts.length; i++) a[i] = Integer.parseInt(parts[i]);
            return a;
        }
        return null;
    }
}
