package io.github.swampus.alexandra.networkapi.compiler.web.handler;

import io.github.swampus.alexandra.compiler.extensions.InvalidNetworkException;
import io.github.swampus.alexandra.dto.shared.response.CompileResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CompilerExceptionHandler {

    @ExceptionHandler(InvalidNetworkException.class)
    public ResponseEntity<CompileResponseDto> handleInvalidNetwork(
            InvalidNetworkException ex
    ) {
        CompileResponseDto dto = new CompileResponseDto(
                null,               // modelJson
                ex.getMessage(),    // error  ✅
                null,               // compilationTrace
                null,               // parseErrors
                null,               // model
                null                // payload
        );

        return ResponseEntity.ok(dto);
    }
}


