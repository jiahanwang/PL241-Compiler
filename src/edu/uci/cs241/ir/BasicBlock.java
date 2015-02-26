package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.BasicBlockType;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanplusplus on 2/23/15.
 */
public class BasicBlock {

    public static int total;
    static {
        total = 0;
    }

    public int id;
    public BasicBlockType type;
    public List<Instruction> instructions;

    public BasicBlock left;
    public BasicBlock right;

    // Constructor for Normal type Basic Block
    public BasicBlock(BasicBlockType type) {
        this.id = total ++;
        this.type = type;
        this.instructions = new LinkedList<Instruction>();
        this.left = null;
        this.right = null;
    }

//    // Constructor for Function type Basic Block
//    public BasicBlock(BasicBlockType type, Function func){
//        this(type);
//        this.func = func;
//    }

    public boolean addInstruction(Instruction in) {
        instructions.add(in);
        return true;
    }

    public boolean merge(BasicBlock another){
        if(this.type != another.type) return false;
        instructions.addAll(another.instructions);
        return true;
    }
}
