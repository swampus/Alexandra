package io.github.swampus.alexandra.compiler.development;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Holds compile-time variables used during development-phase expansion
 * (FOR loops, IF conditions, macros, etc.).
 *
 * <p>This context is intentionally simple and mutable. It behaves like
 * a scoped variable table for the developmental interpreter.</p>
 */
public class DevelopmentContext {

    private final Map<String, Object> variables = new HashMap<>();

    /** Bind variable to value */
    public void set(String name, Object value) {
        Objects.requireNonNull(name, "variable name must not be null");
        variables.put(name, value);
    }

    /** Get variable or null if absent */
    public Object get(String name) {
        return variables.get(name);
    }

    /** Get variable as int (common FOR use-case) */
    public int getInt(String name) {
        Object v = variables.get(name);
        if (v instanceof Number n) return n.intValue();
        throw new IllegalStateException("Variable not numeric: " + name);
    }

    /** Check existence */
    public boolean has(String name) {
        return variables.containsKey(name);
    }

    /** Remove variable */
    public void remove(String name) {
        variables.remove(name);
    }

    /**
     * Resolve simple expressions:
     *
     * - "i" -> value of variable i
     * - "5" -> integer 5
     * - null -> null
     *
     * This is intentionally minimal; replace later with real expression engine.
     */
    public Object resolve(String expr) {

        if (expr == null) return null;

        // numeric literal
        try {
            return Integer.parseInt(expr);
        } catch (NumberFormatException ignored) {}

        // variable lookup
        if (variables.containsKey(expr)) {
            return variables.get(expr);
        }

        // fallback: return raw string
        return expr;
    }
}
