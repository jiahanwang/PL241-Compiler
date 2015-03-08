package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.OperandType;

/**
 * Created by hanplusplus on 3/7/15.
 */

public class Operand {
    // declaration
    public OperandType type = null;
    public String name = null;
    public int address = 0;
    public int value = 0;
    public int which_param = 0;
    public int line  = 0;
    public boolean global = false;
    // constructor
    Operand(OperandType type, String input) {
        this.type = type;
        switch (this.type) {
            case CONST:
                this.value = Integer.valueOf(input);
                break;
            case VARIABLE:
                this.global = !input.contains("_");
            case BASE_ADDRESS:
                this.name = input;
                break;
            case ARR_ADDRESS:
            case MEM_ADDRESS:
                this.address = Integer.valueOf(input);
                break;
            case FP:
                this.name = input;
                break;
            case FUNC_PARAM:
                this.which_param = Integer.valueOf(input);
                break;
            case INST:
            case JUMP_ADDRESS:
            case FUNC_RETURN_PARAM:
                this.line = Integer.valueOf(input);
                break;
            default:
                try {
                    this.value = Integer.valueOf(input);
                }catch(Exception e){
                    this.name = input;
                }
                break;
        }
    }

    public boolean equals(Operand op) {
        if(this.type != op.type) {
            return false;
        }
        //TODO: need Han to confirm
        switch (this.type) {
            case CONST:
                return this.value == op.value;
            case VARIABLE:
            case BASE_ADDRESS:
            case FP:
                return this.name.equals(op.name);
            case ARR_ADDRESS:
            case MEM_ADDRESS:
                return this.address == op.address;
            case FUNC_PARAM:
                return this.which_param == op.which_param;
            case INST:
                return this.line == op.line;
            case FUNC_RETURN_PARAM:
                return this.address == op.address;
            case JUMP_ADDRESS:
                return false;
            default:
                return false;
        }
    }

    public String toString(){
        switch (this.type) {
            case CONST:
                return "" + this.value;
            case VARIABLE:
                return this.name;
            case ARR_ADDRESS:
                return "ARR[" + this.address + "]";
            case BASE_ADDRESS:
                return this.name + "_base_address";
            case MEM_ADDRESS:
                return "MEM[" + this.address + "]";
            case FP:
                return this.name;
            case FUNC_PARAM:
                return "" + this.which_param;
            case JUMP_ADDRESS:
                return "JUMP[" + this.line + "]";
            case INST:
                return "[" + this.line + "]";
            case FUNC_RETURN_PARAM:
                return "PARAM[" + this.line + "]";
            default:
                return "" + this.value;
        }
    }
}
