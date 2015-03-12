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
    public int regno;

    // constructor
    public Operand(){
        this.type = null;
        this.name = null;
        this.address = 0;
        this.value = 0;
        this.which_param = 0;
        this.line  = 0;
        this.global = false;
        this.regno = -1;
    }

    public Operand(OperandType type, String input) {
        this();
        this.type = type;
        switch (this.type) {
            case CONST:
                this.value = Integer.valueOf(input);
                break;
            case VARIABLE:
                this.global = !input.contains("_");
                this.name = input;
                break;
            case BASE_ADDRESS:
                this.name = input + "_base_addr";
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
            case REG:
                this.regno = Integer.valueOf(input);
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
            case ARR_ADDRESS:
                return this.name.equals(op.name);
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
            case REG:
                return this.regno == op.regno;
            default:
                return false;
        }
    }

    public String toString(){
        switch (this.type) {
            case CONST:
                return "#" + this.value;
            case VARIABLE:
                return this.name;
            case ARR_ADDRESS:
                return "ARR[" + this.address + "]";
            case BASE_ADDRESS:
                return this.name;
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
            case REG:
                return "R" + this.regno;
            default:
                return "" + this.value;
        }
    }

    public String getValue(){
        switch (this.type) {
            case CONST:
                return "#" + this.value;
            case VARIABLE:
                return this.name;
            case ARR_ADDRESS:
                return "ARR[" + this.address + "]";
            case BASE_ADDRESS:
                return this.name;
            case MEM_ADDRESS:
                return "MEM[" + this.address + "]";
            case FP:
                return this.name;
            case FUNC_PARAM:
                return "" + this.which_param;
            case JUMP_ADDRESS:
                return "JUMP[" + this.line + "]";
            case INST:
                return String.valueOf(this.line);
            case FUNC_RETURN_PARAM:
                return "PARAM[" + this.line + "]";
            case REG:
                return "R" + this.regno;
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
        res.regno = this.regno;
        return res;
    }
}
