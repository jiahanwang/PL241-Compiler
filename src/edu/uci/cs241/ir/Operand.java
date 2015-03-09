package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.OperandType;

import java.util.Objects;

/**
 * Created by hanplusplus on 3/7/15.
 */

public class Operand implements Cloneable{
    // declaration
    public OperandType type;
    public String name;
    public int address;
    public int value;
    public int which_param;
    public int line;
    public boolean global;

    // constructor
    Operand(){
        this.type = null;
        this.name = null;
        this.address = 0;
        this.value = 0;
        this.which_param = 0;
        this.line  = 0;
        this.global = false;
    }

    Operand(OperandType type, String input) {
        this();
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

    public Object clone (){
        Operand res = new Operand();
        res.type = this.type;
        res.name = this.name;
        res.address = this.address;
        res.value = this.value;
        res.which_param = this.which_param;
        res.line = this.line;
        res.global = this.global;
        return res;
    }
}
