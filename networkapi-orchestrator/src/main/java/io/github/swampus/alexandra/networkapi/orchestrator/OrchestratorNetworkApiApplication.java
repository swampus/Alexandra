package io.github.swampus.alexandra.networkapi.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Alexandra Network Orchestrator application.
 *
 * <p>This class bootstraps the Spring Boot runtime for the orchestrator service,
 * which exposes a REST API responsible for coordinating high-level network
 * lifecycle operations.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Starts the Spring application context.</li>
 *   <li>Enables component scanning for orchestrator modules
 *       (controllers, infrastructure configuration, adapters).</li>
 *   <li>Acts as the deployment and runtime boundary of the orchestrator service.</li>
 * </ul>
 *
 * <h2>What this class is NOT</h2>
 * <ul>
 *   <li>Does <b>not</b> contain business logic.</li>
 *   <li>Does <b>not</b> define orchestration workflows.</li>
 *   <li>Does <b>not</b> replace application use cases.</li>
 * </ul>
 *
 * <h2>Architectural role</h2>
 * <p>The orchestrator itself is implemented as a modular, reusable library
 * (controllers, use cases, ports, adapters). This class represents the
 * <b>application launcher</b> that assembles those modules into a runnable
 * Spring Boot service.</p>
 *
 * <p>Separating the application entry point from the core orchestrator logic
 * allows the same orchestrator module to be reused in different runtimes
 * (standalone service, embedded deployment, tests) without modification.</p>
 *
 * <h2>External communication</h2>
 * <p>Client applications (e.g. React-based UIs or other services) interact
 * with the orchestrator exclusively through its REST controllers.
 * This class exists solely to make that API available at runtime.</p>
 */
@SpringBootApplication
public class OrchestratorNetworkApiApplication {

    /**
     * Boots the Alexandra Network Orchestrator service.
     *
     * @param args command-line arguments passed to the JVM
     */
    public static void main(String[] args) {
        SpringApplication.run(OrchestratorNetworkApiApplication.class, args);
    }
}
