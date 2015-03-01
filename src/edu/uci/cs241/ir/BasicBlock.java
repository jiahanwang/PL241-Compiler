package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.BasicBlockType;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanplusplus on 2/23/15.
 */
public class BasicBlock {

    public int id;
    public static int count;
    static {
        count = 0;
    }
    public String name;

    // For CFG
    public BasicBlock left;
    public BasicBlock right;
    public BasicBlock exit;
    public boolean has_branching;

    // For IR
    public int start_line;
    public int end_line;

    // Constructor
    public BasicBlock() {
        this.id = ++count;
        this.left = null;
        this.right = null;
        this.exit = this;
        this.has_branching = false;
        this.start_line = Integer.MIN_VALUE;
        this.end_line = Integer.MIN_VALUE;
    }

    public BasicBlock(String name){
        this();
        this.name = name;
    }

    public static boolean merge(BasicBlock one, BasicBlock another){
        if(one.start_line == Integer.MIN_VALUE){
            one.start_line = another.start_line;
        }
        one.end_line = another.end_line;
        return true;
    }

    public void setRange(int start, int end){
        if(start == Integer.MIN_VALUE) {
            this.start_line = end;
            this.end_line = end;
            return;
        }
        if(end == Integer.MIN_VALUE) {
            this.start_line = start;
            this.end_line = start;
            return;
        }
        this.start_line = start;
        this.end_line = end;
    }
}
