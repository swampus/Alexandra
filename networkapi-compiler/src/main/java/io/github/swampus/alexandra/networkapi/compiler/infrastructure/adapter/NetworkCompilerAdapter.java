package io.github.swampus.alexandra.networkapi.compiler.infrastructure.adapter;

import io.github.swampus.alexandra.compiler.IRNetworkCompiler;
import io.github.swampus.alexandra.compiler.NetworkCompilerFacade;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.networkapi.compiler.application.port.NetworkCompilerPort;

/**
 * Infrastructure adapter exposing network compilation capabilities
 * through the {@link NetworkCompilerPort}.
 *
 * <p>This adapter delegates compilation either to a facade-based
 * compiler or to a trace-enabled compiler depending on the
 * requested compilation mode.</p>
 *
 * <p>No business decisions are made here; the adapter only
 * adapts infrastructure capabilities to application-level ports.</p>
 */
public class NetworkCompilerAdapter implements NetworkCompilerPort {

    private final NetworkCompilerFacade facade;
    private final IRNetworkCompiler traceCompiler;

    public NetworkCompilerAdapter(
            NetworkCompilerFacade facade,
            IRNetworkCompiler traceCompiler
    ) {
        this.facade = facade;
        this.traceCompiler = traceCompiler;
    }

    @Override
    public CompilationOutput compile(
            Instruction instruction,
            boolean traceRequired
    ) {
        if (!traceRequired) {
            NetworkModel model = facade.compile(instruction);
            return new CompilationOutput(model, null);
        }

        NetworkModel model = traceCompiler.compile(instruction);
        String trace = traceCompiler.getTraceAsString();

        return new CompilationOutput(model, trace);
    }
}
