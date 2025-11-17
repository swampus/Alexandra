package io.github.swampus.alexandra.ir.model;

/**
 * Operation codes for NeuronLang IR instructions.
 * Used instead of plain strings for type-safety and autocompletion.
 */
public enum OpCode {
    // Structural/Root
    PROGRAM,

    BLOCK,

    // Layers
    LAYER,

    // Connections
    CONNECT,

    // Module system
    MODULE_DEF,
    MODULE_CALL,

    // Macro system
    MACRO_DEF,
    MACRO_CALL,

    // Control flow
    LET,
    FOR,

    CALL,
    IF,

    CONDITION,
    ELSE,

    // Symmetry/expand
    EXPAND,

    // External file
    FILE,

    // For future extensibility (custom/user-defined)
    CUSTOM
}
