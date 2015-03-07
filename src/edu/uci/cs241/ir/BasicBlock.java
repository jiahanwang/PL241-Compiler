package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.BasicBlockType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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

    // For more concise CFG (especially for if after normal block)
    public BasicBlockType type;


    // For CFG
    public BasicBlock left;
    public BasicBlock right;
    public BasicBlock exit;
    public boolean has_branching;

    // For IR
    public int end_line;

    public List<Instruction> ins;

    // Constructor
    public BasicBlock() {
        this.id = ++count;
        this.type = BasicBlockType.NORAML;
        this.left = null;
        this.right = null;
        this.exit = this;
        this.has_branching = false;
        this.end_line = Integer.MIN_VALUE;
        this.ins = new ArrayList<Instruction>();
    }

    public BasicBlock(String name){
        this();
        this.name = name;
    }

    public void addInstruction(Instruction in){
        this.ins.add(in);
    }

    public void addInstructions(List<Instruction> ins){
        this.ins.addAll(ins);
    }

    public void insertInstructions(List<Instruction> ins, int start){
        this.ins.addAll(start, ins);
    }

    public int getStart(){
        if(ins.size() == 0){
            return Integer.MIN_VALUE;
        }else{
            return ins.get(0).id;
        }
    }

    public int getEnd(){
        if(ins.size() == 0){
            return this.end_line;
        }else{
            return ins.get(ins.size() - 1).id;
        }
    }

    public static boolean merge(BasicBlock one, BasicBlock another){
        one.ins.addAll(another.ins);
        return true;
    }

    public String toStringOfInstructions(){
        return IR.toStringOfRange(ins, 0, ins.size() - 1);
    }

}
