package io.github.swampus.alexandra.networkapi.artifact.spi.exception;

/**
 * Thrown when a compiled NetworkModel cannot be serialized
 * into a stable artifact representation.
 */
public class ArtifactSerializationException extends RuntimeException {

    public ArtifactSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtifactSerializationException(String message) {
        super(message);
    }
}
