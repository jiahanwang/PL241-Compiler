package edu.uci.cs241.optimization;

import edu.uci.cs241.ir.DefUseChain;
import edu.uci.cs241.ir.Function;
import edu.uci.cs241.ir.Instruction;
import edu.uci.cs241.ir.Operand;
import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by hanplusplus on 3/8/15.
 */
public class CP {

    public static void performCP(Function func){
        List<Instruction> ins = func.ir.ins;
        DefUseChain du = func.getDu();
        ListIterator<Instruction> i_iterator = ins.listIterator();
        List<Integer> to_delete = new LinkedList<Integer>();
        while(i_iterator.hasNext()){
            // only deal with MOVE
            Instruction in = i_iterator.next();
            if(in.operator != InstructionType.MOVE) continue;
            Operand replacer = in.operands.get(0);
            Operand to_replace = in.operands.get(1);
            // both operands cannot be non-local
            if(replacer.global || to_replace.global) continue;
            // propagate copy
            String name = to_replace.name;
            List<Instruction> uses = du.variables.get(name).uses;
            for(Instruction use : uses){
                ListIterator<Operand> o_iterator = use.operands.listIterator();
                while(o_iterator.hasNext()){
                    Operand operand = o_iterator.next();
                    if(operand.type == OperandType.VARIABLE && operand.name.equals(name)) {
                        o_iterator.set((Operand)replacer.clone());
                    }
                }
            }
            // remember this instruction
            to_delete.add(in.id);
            // no need to update def-use, cuz getDu() computes a new one every time it's called
        }
        // sort the list, just in case
        Collections.sort(to_delete);
        for(int i = 0, len = to_delete.size(); i < len; i++){
            func.ir.deleteInstruction(to_delete.get(i) - i);
        }
    }




}
