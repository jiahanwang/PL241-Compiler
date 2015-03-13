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
    // Regno to store this instruction in
    public int regno;
    // For kill
    public String arr_name;
    public boolean reload;

    // constructor
    public Instruction(InstructionType type){
        this.operator = type;
        this.operands = new LinkedList<Operand>();
        this.op_count = 0;
        this.parent = null;
        this.regno = -1;
        this.arr_name = null;
        this.reload = false;
    }

    public boolean equals(Instruction inst) {
        if (this.operator == inst.operator) {
            // Special case: for ADD / MUL (commutative)
            if (this.operator == InstructionType.ADD || this.operator == InstructionType.MUL) {
                // only for ADD x y == ADD y x or MUL
                if (operands.get(0).equals(inst.operands.get(1)) && operands.get(1).equals(inst.operands.get(0))) {
                    return true;
                }
            }
            // Default: check operand for operand.
            if (operands.size() == inst.operands.size()) {
                boolean e = true;
                for (int i = 0; i < operands.size(); i++) {
                    if (operands.get(i).global || inst.operands.get(i).global) {
                        return false;
                        // short circuit global vars, they cannot be equal to be safe.
                    }
                    if (!operands.get(i).equals(inst.operands.get(i))) {
                        e = false;
                        break;
                    }
                }
                return e;
            }
        }
        return false;
    }


    public void clearOperands() {
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

    // For RA
    public List<String> getInputs(){
        List<String> inputs = new LinkedList<String>();
        switch (this.operator) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case ADDA:
            case CMP:
            case STORE:
                if(this.operands.get(0).type != OperandType.CONST){
                    inputs.add(this.operands.get(0).getValue());
                }
                if(this.operands.get(1).type != OperandType.CONST){
                    inputs.add(this.operands.get(1).getValue());
                }
                break;
            case LOAD:
            case BNE:
            case BEQ:
            case BLE:
            case BLT:
            case BGE:
            case BGT:
            case WRITE:
                if(this.operands.get(0).type != OperandType.CONST){
                    inputs.add(this.operands.get(0).getValue());
                }
                break;
            case FUNC:
                // track parameters excluding the last hidden one
                for(int i = 1, len = this.operands.size() - 1; i < len; i++){
                    if(this.operands.get(i).type != OperandType.CONST){
                        inputs.add(this.operands.get(i).getValue());
                    }
                }
                break;
            case RETURN:
                for(int i = 0, len = this.operands.size(); i < len; i++){
                    if(this.operands.get(i).type != OperandType.CONST){
                        inputs.add(this.operands.get(i).getValue());
                    }
                }
                break;
        }
        return inputs;
    }

//    // For RA
//    public String getOutput(){
//        switch (this.operator) {
//            case ADD:
//            case SUB:
//            case MUL:
//            case DIV:
//            case CMP:
//            case ADDA:
//            case LOAD:
//            case READ:
//            case LOADPARAM:
//            case FUNC:
//                return String.valueOf(this.id);
//            default:
//                return null;
//        }
//    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.operator.toString());
        builder.append(" ");
        for(Operand operand : this.operands){
            builder.append(operand.toString());
            builder.append(" ");
        }
        if(this.regno != -1){
            builder.append("= R" + this.regno);
        }
        if(this.operator == InstructionType.LOAD){
            builder.append(":" + this.reload);
        }
        return builder.toString();
    }
}
