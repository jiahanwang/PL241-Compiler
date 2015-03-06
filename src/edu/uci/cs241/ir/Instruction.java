package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.ConditionType;
import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;
import edu.uci.cs241.ir.types.ResultType;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanplusplus on 2/20/15.
 */
public class Instruction {

    // declaration
    public int id;
    public InstructionType operator;
    public List<Operand> operands;
    public int op_count;

    // constructor
    public Instruction(InstructionType type){
        this.operator = type;
        this.operands = new LinkedList<Operand>();
        this.op_count = 0;
    }

    public static Instruction createInstructionByConditionType(ConditionType c_type){
        switch (c_type) {
            case GT:
                return new Instruction(InstructionType.BGT);
            case LT:
                return new Instruction(InstructionType.BLT);
            case GE:
                return new Instruction(InstructionType.BGE);
            case LE:
                return new Instruction(InstructionType.BLE);
            case NE:
                return new Instruction(InstructionType.BNE);
            case EQ:
                return new Instruction(InstructionType.BEQ);
            default:
                return null;
        }
    }

    public boolean addOperand(OperandType type, String input){
        if(op_count >= this.operator.operand_num) return false;
        Operand op = new Operand(type, input);
        this.operands.add(op);
        op_count++;
        return true;
    }

    public boolean addOperandByResultType(Result res){
        switch(res.type){
            case ARR:
                addOperand(OperandType.ARR_ADDRESS, String.valueOf(res.address));
                break;
            case VAR:
                addOperand(OperandType.VARIABLE, String.valueOf(res.name));
                break;
            case CONST:
                addOperand(OperandType.CONST, String.valueOf(res.value));
                break;
            case INST:
                addOperand(OperandType.INST, String.valueOf(res.line));
                break;
            case FUNC_CALL:
                addOperand(OperandType.INST, String.valueOf(res.line));
                break;
        }
        return true;
    }
    // Operand Object
    public class Operand {
        // declaration
        public OperandType type = null;
        public String name = null;
        public int address = 0;
        public int value = 0;
        public int which_param = 0;
        public int line  = 0;
        // constructor
        Operand(OperandType type, String input) {
            this.type = type;
            switch (this.type) {
                case CONST:
                    this.value = Integer.valueOf(input);
                    break;
                case VARIABLE:
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
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.operator.toString());
        builder.append(" ");
        for(Operand operand : this.operands){
            builder.append(operand.toString());
            builder.append(" ");
        }
        return builder.toString();
    }
}
