package io.github.swampus.alexandra.networkapi.artifact.spi.exception;

/**
 * Thrown when a serialized artifact cannot be deserialized
 * into a runtime network model.
 */
public class ArtifactDeserializationException extends RuntimeException {

    public ArtifactDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtifactDeserializationException(String message) {
        super(message);
    }
}
