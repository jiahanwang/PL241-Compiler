package edu.uci.cs241.optimization;

import edu.uci.cs241.ir.*;
import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ivan on 3/7/2015.
 */
public class CSE {

    // Keep track of visited blocks to avoid endless loops
    public static boolean[] explored = new boolean[1000];

    public static List<Integer> remove = new ArrayList<Integer>();

    public static void reset() {
        explored = new boolean[1000];
        eliminated = new HashMap<Integer, Integer>();
        remove = new ArrayList<Integer>();
    }

    public static HashMap<Integer, Integer> eliminated = new HashMap<Integer, Integer>();

    public static void recursiveCSE(BasicBlock b, HashMap<InstructionType, ArrayList<Instruction>> parent) throws Exception {

        // avoid recursing on a visited bb
        if(b == null || explored[b.id]) {
            return;
        }
        explored[b.id] = true;
        // create anchor for this block
        HashMap<InstructionType, ArrayList<Instruction>> anchor = new HashMap<InstructionType, ArrayList<Instruction>>();
        // take all existing dominator tree data from parent

//        anchor.putAll(parent);    // this passes by reference
        // Make full copy of the anchor hashmap
        for(InstructionType t : parent.keySet()) {
            ArrayList<Instruction> copy = new ArrayList<Instruction>();
            for(Instruction i : parent.get(t)) {
                copy.add(i);
            }
            anchor.put(t, copy);
        }

        for(Instruction i : b.ins) {
            // Grab instruction information to compare
            InstructionType t = i.operator;
            if (t == InstructionType.FUNC || t == InstructionType.RETURN || t == InstructionType.LOADPARAM
                    || t == InstructionType.READ) {
                // blacklisting instructions that we shouldn't bother checking
                continue;
            }
            // See if instruction is referring to an eliminated instruction
            for(Operand o : i.operands) {
                if(o.type == OperandType.INST) {
                    if(eliminated.containsKey(o.line)) {
                        o.line = eliminated.get(o.line);
                    }
                }
            }
            // Special case of LOAD-STORE-LOAD
            if(i.reload) {
                if(anchor.containsKey(t)) {
                    Instruction old = null;
                    for(Instruction i2 : anchor.get(t)) {
                        if(i.equals(i2)) {
                            // Have to replace older version of LOAD with this one.
                            old = i2;
                            break;
                        }
                    }
                    anchor.get(t).remove(old);
                    anchor.get(t).add(i);
                } else {
                    ArrayList<Instruction> a = new ArrayList<Instruction>();
                    a.add(i);
                    anchor.put(t, a);
                }
                continue; // do not replace.
            }
            boolean found = false;
            // check current block inst against anchor's
            if(anchor.containsKey(t)){
                for(Instruction j : anchor.get(t)) {
                    if(i.equals(j)) {
                        // found an existing inst that matches ours
                        i.operator = InstructionType.REF;
                        i.clearOperands();
                        i.addOperand(OperandType.INST, Integer.toString(j.id));
                        found = true;
                        eliminated.put(i.id, j.id);
                        remove.add(i.id-remove.size());
                        if(remove.get(remove.size()-1) < i.id-remove.size()) {
                            throw new Exception("Error in out of order parsing of Dominator Tree");
                            // Special case to prevent dominated blocks that come AFTER
                            // to be removed first which will cause the removal to fail.
                        }
                    }
                }
                if(!found) {
                    //append to the end of list
                    anchor.get(t).add(i);
                }
            } else {
                // Create an entry for it since
                // no key exists for this instruction type
                anchor.put(t, new ArrayList<Instruction>());
                anchor.get(t).add(i);
            }
        }

        for(BasicBlock d : b.dom) {
            recursiveCSE(d, anchor);
        }

    }

    public static int numRemovesBefore(int instr) {
        int count = 0;
        for(int i : remove) {
            if(instr > i) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }


}
