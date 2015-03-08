package edu.uci.cs241.optimization;

import edu.uci.cs241.ir.BasicBlock;
import edu.uci.cs241.ir.Instruction;
import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ivan on 3/7/2015.
 */
public class CSE {

    // Keep track of visited blocks to avoid endless loops
    public static boolean[] explored = new boolean[1000000];

    public static void recursiveCSE(BasicBlock b, HashMap<InstructionType, ArrayList<Instruction>> parent) throws Exception {

        // avoid recursing on a visited bb
        if(explored[b.id]) {
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
            if (t == InstructionType.PHI) {
                continue;
            }
            boolean found = false;
            // check current block inst against anchor's
            if(anchor.containsKey(t)){
                for(Instruction j : anchor.get(t)) {
                    if(i.equals(j)) {
                        // found an existing inst that matches ours
                        i.operator = InstructionType.REF;
                        i.instRef = i.id;
                        i.clearOperands();
                        i.addOperand(OperandType.INST, Integer.toString(j.id));
                        found = true;
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


}
