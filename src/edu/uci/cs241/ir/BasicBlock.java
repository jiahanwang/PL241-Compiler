package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.BasicBlockType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.*;

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

    // For Dominator Tree
    public Set<BasicBlock> dom;
    public BasicBlock join;
    // this is for special case of merging if header into another statement sequence
    // dangling join block

    // For IR
    public int end_line;
    public BasicBlock loop_end;

    // For RA
    public List<Instruction> phis;
    public LinkedHashSet<BasicBlock> successor;
    public Set<String> live_for_while;

    public List<Instruction> ins;

    // For Live Ranges
    public HashSet<String> live;

    // For kill array reload
    public HashSet<Instruction> load;

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
        this.dom = new LinkedHashSet<BasicBlock>();
        this.live = new HashSet<String>();
        this.phis = new ArrayList<Instruction>();
        this.live_for_while = new LinkedHashSet<String>();
        this.load = new HashSet<Instruction>();
    }

    public BasicBlock(String name){
        this();
        this.name = name;
    }

    public Set<BasicBlock> getSuccessors() {
        if(successor == null) {
            // fill if has not been init
            successor = new LinkedHashSet<BasicBlock>();
            if(this.left != null) {
                successor.add(this.left);
            }
            if(this.right !=null ) {
                successor.add(this.right);
            }
        }
        return successor;
    }

    public List<String> getRelevantPhiInputs(BasicBlock b){
        if(this.phis == null || this.phis.size() == 0 || b == null || b.ins == null) return new ArrayList<String>(0);
        List<String> res = new LinkedList<String>();
        int start = b.ins.get(0).id;
        int end = b.ins.get(b.ins.size() - 1).id;
        for(Instruction phi : phis){
            for(Operand operand : phi.operands){
                if(operand.type == OperandType.INST){
                    if(operand.line >= start && operand.line <= end){
                        // this operand is relevant
                        res.add(String.valueOf(operand.line));
                    }
                }
            }
        }
        return res;
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
        for(Instruction in : another.ins){
            in.parent = one;
            one.ins.add(in);
        }
        return true;
    }

    public String toStringOfInstructions(){
        return IR.toStringOfRange(ins, 0, ins.size() - 1);
    }

    public String toString() {
        return toStringOfInstructions();
    }

}
