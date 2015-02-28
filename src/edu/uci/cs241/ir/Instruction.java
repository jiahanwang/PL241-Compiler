package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanplusplus on 2/20/15.
 */
public class Instruction {

    // static property to assign id

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

    public boolean addOperand(OperandType type, String input){
        if(op_count >= this.operator.operand_num) return false;
        Operand op = new Operand(type, input);
        this.operands.add(op);
        op_count++;
        return true;
    }

    // Operand Object
    public class Operand {
        // declaration
        public OperandType type = null;
        public String name = null;
        public String address = null;
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
                    this.name = input;
                    break;
                case ADDRESS:
                    this.address = input;
                    break;
                case FP:
                    this.name = input;
                    break;
                case FUNC_PARAM:
                    this.which_param = Integer.valueOf(input);
                    break;
                case INST:
                    this.line = Integer.valueOf(input);
                    break;
                default:
                    break;
            }
        }
    }
}
