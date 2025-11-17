package io.github.swampus.alexandra.compiler.mainflow;

import io.github.swampus.alexandra.compiler.extensions.InvalidNetworkException;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.validator.NetworkModelValidator;
import io.github.swampus.alexandra.compiler.validator.PostCompileValidationService;
import io.github.swampus.alexandra.compiler.validator.ValidationLevel;
import io.github.swampus.alexandra.compiler.validator.spi.ShapeAndDryRunValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Тесты PostCompileValidationService: проверяем делегирование к валидаторам.
 */
@ExtendWith(MockitoExtension.class)
class PostCompileValidationServiceTest {

    @Mock
    private NetworkModelValidator structuralValidator;

    @Mock
    private ShapeAndDryRunValidator shapesValidator;

    @Mock
    private NetworkModel model;

    private PostCompileValidationService service;

    @BeforeEach
    void setUp() {
        service = new PostCompileValidationService(structuralValidator, shapesValidator);
    }

    @Test
    void validateAll_invokesStructuralThenShapes() throws InvalidNetworkException {
        // act
        service.validateAll(model);

        // assert — порядок вызовов важен (сначала структурные, затем shapes/dry-run)
        InOrder inOrder = inOrder(structuralValidator, shapesValidator);
        inOrder.verify(structuralValidator).validate(model);
        inOrder.verify(shapesValidator).validate(model);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void validate_withLevelNONE_invokesNothing() throws InvalidNetworkException {
        // act
        service.validate(model, ValidationLevel.NONE);

        // assert
        verifyNoInteractions(structuralValidator, shapesValidator);
    }

    @Test
    void validate_withLevelSTRUCTURAL_invokesOnlyStructural() throws InvalidNetworkException {
        // act
        service.validate(model, ValidationLevel.STRUCTURAL);

        // assert
        verify(structuralValidator, times(1)).validate(model);
        verifyNoInteractions(shapesValidator);
    }

    @Test
    void validate_withLevelSHAPES_invokesStructuralThenShapes() throws InvalidNetworkException {
        // act
        service.validate(model, ValidationLevel.SHAPES);

        // assert — сначала структурный, затем shapes/dry-run
        InOrder inOrder = inOrder(structuralValidator, shapesValidator);
        inOrder.verify(structuralValidator).validate(model);
        inOrder.verify(shapesValidator).validate(model);
        inOrder.verifyNoMoreInteractions();
    }
}

