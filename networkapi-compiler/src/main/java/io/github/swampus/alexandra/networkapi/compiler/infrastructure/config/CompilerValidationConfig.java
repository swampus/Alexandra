package io.github.swampus.alexandra.networkapi.compiler.infrastructure.config;

import io.github.swampus.alexandra.compiler.validator.NetworkModelValidator;
import io.github.swampus.alexandra.compiler.validator.impl.CompositeNetworkModelValidator;
import io.github.swampus.alexandra.compiler.validator.impl.ReferenceIntegrityValidator;
import io.github.swampus.alexandra.compiler.validator.impl.CycleValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Infrastructure configuration exposing domain-level
 * network validation services as Spring beans.
 *
 * <p>This class bridges pure domain validation logic
 * with the Spring-managed application runtime.</p>
 */
@Configuration
public class CompilerValidationConfig {

    @Bean
    public NetworkModelValidator networkModelValidator() {
        return new CompositeNetworkModelValidator(List.of(
                new CycleValidator(), new ReferenceIntegrityValidator()
        ));
    }

}
