package io.github.swampus.alexandra.translator.impl;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.nureonlang.parser.impl.InternalNureonLangServiceImpl;
import io.github.swampus.alexandra.nureonlang.parser.internal.InternalNureonLangService;
import io.github.swampus.alexandra.translator.exception.NureonLangTranslateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NureonLangToIRTranslatorImplTest {

    @Test
    void returnsIR_whenNoErrors() {
        InternalNureonLangService parserService = new InternalNureonLangServiceImpl();
        String code = "LAYER INPUT name=tokens size=512";
        NureonLangToIRTranslatorImpl translator = new NureonLangToIRTranslatorImpl(parserService);

        Instruction ir = translator.translate(code);
        assertNotNull(ir);
        assertEquals("PROGRAM", ir.getOp().name());
    }

    @Test
    void throwsOnSemanticErrors() {
        InternalNureonLangService parserService = new InternalNureonLangServiceImpl();
        String code = "LAYER INPUT name=tokens";
        NureonLangToIRTranslatorImpl translator = new NureonLangToIRTranslatorImpl(parserService);

        NureonLangTranslateException ex = assertThrows(
                NureonLangTranslateException.class,
                () -> translator.translate(code)
        );
        assertFalse(ex.getErrors().isEmpty());
        assertTrue(ex.getErrors().stream().anyMatch(e -> e.getMessage().contains("size")));
    }

    @Test
    void throwsOnInternalException() {
        InternalNureonLangService parserService = mock(InternalNureonLangService.class);
        when(parserService.parse(any()))
                .thenThrow(new RuntimeException("BOOM"));

        NureonLangToIRTranslatorImpl translator =
                new NureonLangToIRTranslatorImpl(parserService);

        NureonLangTranslateException ex = assertThrows(
                NureonLangTranslateException.class,
                () -> translator.translate("SOMETHING")
        );

        // Line is unknown → OK
        assertEquals(-1, ex.getErrors().get(0).getLine());

        // Error message comes from ParseError, not Exception#getMessage
        assertEquals("BOOM", ex.getErrors().get(0).getMessage());
    }


}