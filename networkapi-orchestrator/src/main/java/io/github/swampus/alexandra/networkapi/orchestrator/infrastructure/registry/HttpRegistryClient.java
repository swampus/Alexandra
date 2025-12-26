package io.github.swampus.alexandra.networkapi.orchestrator.infrastructure.registry;

import io.github.swampus.alexandra.dto.shared.request.registry.SaveArtifactRequestDTO;
import io.github.swampus.alexandra.dto.shared.request.registry.SearchArtifactRequestDTO;
import io.github.swampus.alexandra.networkapi.orchestrator.application.port.RegistryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequiredArgsConstructor
@Component
public class HttpRegistryClient {

    private final RestTemplate restTemplate;

    public void save(SaveArtifactRequestDTO request) {
        restTemplate.postForLocation(
                "/api/registry/artifacts",
                request
        );
    }

    public RegistryArtifactDto getById(String artifactId) {
        return restTemplate.getForObject(
                "/api/registry/artifacts/{id}",
                RegistryArtifactDto.class,
                artifactId
        );
    }

    public void delete(String artifactId) {
        restTemplate.delete(
                "/api/registry/artifacts/{id}",
                artifactId
        );
    }

    public List<RegistryArtifactDto> search(SearchArtifactRequestDTO request) {

        ResponseEntity<List<RegistryArtifactDto>> response =
                restTemplate.exchange(
                        "/api/registry/artifacts/search",
                        HttpMethod.POST,
                        new HttpEntity<>(request),
                        new ParameterizedTypeReference<>() {}
                );

        return response.getBody();
    }

}


