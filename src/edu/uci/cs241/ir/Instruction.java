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
    // Pointing to the BB owing this instruction (easier for insertion and deletion)
    public BasicBlock parent;

    // constructor
    public Instruction(InstructionType type){
        this.operator = type;
        this.operands = new LinkedList<Operand>();
        this.op_count = 0;
        this.parent = null;
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
