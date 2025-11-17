package io.github.swampus.alexandra.translator;

import io.github.swampus.alexandra.ir.model.Instruction;


public interface NureonLangToIRTranslator {
    Instruction translate(String code);
}
