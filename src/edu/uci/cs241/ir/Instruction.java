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

    public boolean equals(Instruction inst) {
        if (this.operator == inst.operator) {
            // Special case: for ADD / MUL (commutative)
            if (this.operator == InstructionType.ADD || this.operator == InstructionType.MUL) {
                // only for ADD x y == ADD y x or MUL
                if (operands.get(0).equals(inst.operands.get(1)) && operands.get(1).equals(inst.operands.get(0))) {
                    return true;
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
                return false;
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
    // Operand Object
    public class Operand {
        // declaration
        public OperandType type = null;
        public String name = null; // VARIABLE, BASE_ADDRESS, FP
        public int address = 0; // ARR_ADDRESS, MEM_ADDRESS
        public int value = 0; // CONST
        public int which_param = 0; // FUNC_PARAM
        public int line  = 0; // INST, JUMP_ADDRESS, FUNC_RETURN_PARAM
        public boolean global = false;  // Used to determine global var
        // constructor
        Operand(OperandType type, String input) {
            this.type = type;
            switch (this.type) {
                case CONST:
                    this.value = Integer.valueOf(input);
                    break;
                case VARIABLE:
                    global = (!input.contains("_"));
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
