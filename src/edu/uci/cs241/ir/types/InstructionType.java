package edu.uci.cs241.ir.types;

/**
 * Created by hanplusplus on 2/20/15.
 */
public enum InstructionType {

    NEG("neg", 1),
    ADD("add", 2),
    SUB("sub",2 ),
    MUL("mul", 2),
    DIV("div", 2),
    CMP("cmp", 2),

    ADDA("adda", 2),
    LOAD("load", 1),
    STORE("store", 2),
    MOVE("move", 2),
    PHI("phi", Integer.MAX_VALUE),

    END("end", 0),

    BRA("bra", 1),
    BNE("bne", 2),
    BEQ("beq", 2),
    BLE("ble", 2),
    BLT("blt", 2),
    BGE("bge", 2),
    BGT("bgt", 2),

    READ("read", 0),
    WRITE("write", 1),
    WLN("wln", 0),

    // added instructions
    FUNC("func", Integer.MAX_VALUE),
    LOADPARAM("loadparam", 2),
    RETURN("return", 2),

    // For CSE
    REF("ref", 1);

    public final String representation;

    public final int operand_num;

    InstructionType(String representation, int operand_num) {
        this.representation = representation;
        this.operand_num = operand_num;
    }

}
