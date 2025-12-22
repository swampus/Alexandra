package io.github.swampus.alexandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Network Compiler API service.
 *
 * <p>This Spring Boot application exposes a REST-based interface for compiling
 * neural network descriptions (e.g. NureonLang / IR) into an internal
 * {@code NetworkModel} representation.</p>
 *
 * <p><strong>Responsibilities:</strong>
 * <ul>
 *   <li>Bootstraps the Spring application context</li>
 *   <li>Wires REST adapters to application use cases</li>
 * </ul>
 * </p>
 *
 * <p><strong>Non-responsibilities:</strong>
 * <ul>
 *   <li>Does NOT perform network training</li>
 *   <li>Does NOT persist compiled models</li>
 *   <li>Does NOT orchestrate execution or inference</li>
 * </ul>
 * </p>
 *
 * <p>This module is designed to be consumed by higher-level services
 * (e.g. {@code alexandra-oracle}) or CLI tools, not directly by end users.</p>
 *
 * <p>The application follows Clean Architecture principles:
 * controllers act as thin adapters, while all business logic is delegated
 * to application-level use cases.</p>
 */
@SpringBootApplication
public class CompilerNetworkApiApplication {

    /**
     * Boots the Network Compiler API application.
     *
     * @param args standard Spring Boot application arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CompilerNetworkApiApplication.class, args);
    }
}
