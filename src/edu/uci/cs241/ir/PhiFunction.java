package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanplusplus on 3/3/15.
 */
public class PhiFunction implements Cloneable {

    public String id;

    public int original;

    public int current;

    public int lead;

    public List<Integer> operands;

    public int last;

    public boolean first_use;

    public boolean left_changed;

    public PhiFunction(String id, int original, int current){
        this.id = id;
        this.original = original;
        this.current = current;
        this.lead = Integer.MIN_VALUE;
        this.operands = new ArrayList<Integer>();
        this.last = current;
        this.first_use = true;
        this.left_changed = false;
    }

    public PhiFunction(String id){
        this(id, 0, 0);
    }

    public Instruction toInstruction(){
        Instruction in = new Instruction(InstructionType.PHI);
        in.addOperand(OperandType.VARIABLE, this.id + "_" + this.lead);
        for(Integer operand : operands) {
            in.addOperand(OperandType.VARIABLE, this.id + "_" + operand);
        }
        return in;
    }

    public Object clone (){
        PhiFunction new_phi = new PhiFunction(this.id, current, current);
        return new_phi;
    }


}
