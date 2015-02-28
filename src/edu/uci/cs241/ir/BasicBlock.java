package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.BasicBlockType;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanplusplus on 2/23/15.
 */
public class BasicBlock {

    public int id;

    // For CFG
    public BasicBlock left;
    public BasicBlock right;
    public boolean has_branching;

    // For IR
    public int start_line;
    public int end_line;

    // Constructor
    public BasicBlock() {
        this.left = null;
        this.right = null;
        this.has_branching = false;
        this.start_line = 0;
        this.end_line = 0;
    }

    public static boolean merge(BasicBlock one, BasicBlock another){
        //if(this.type != another.type) return false;
        //instructions.addAll(another.instructions);
        return true;
    }
}
