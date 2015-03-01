package edu.uci.cs241.ir.types;

/**
 * Created by hanplusplus on 2/23/15.
 */
// OperandType Enum
public enum OperandType {
    CONST,
    VARIABLE,

    ARR_ADDRESS,
    BASE_ADDRESS,
    MEM_ADDRESS,

    INST,
    JUMP_ADDRESS,

    FP,
    FUNC_PARAM,
    FUNC_RETURN_PARAM,
    LOCAL_VARIABLE;
}