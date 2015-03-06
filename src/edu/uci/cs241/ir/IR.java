package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;

import java.util.*;

/**
 * Created by hanplusplus on 2/27/15.
 */
public class IR implements Cloneable{

    public ArrayList<Instruction> ins;

    public int count = 0;

    public int pc;


    public IR () {
        this.ins = new ArrayList<Instruction>();
        this.pc = 0;
    }


    public int addInstruction(Instruction in){
        in.id = count ++;
        this.ins.add(in);
        return count - 1;
    }

    public int addInstructions(List<Instruction> ins){
        int last_line = count;
        for(Instruction in : ins){
            last_line = addInstruction(in);
        }
        return last_line;
    }

    public void insertPhiInstructions(List<Instruction> ins, int start){
        Map<String, String> replace_map = new HashMap<String, String>();
        for(Instruction in : ins){
            String current = in.operands.get(0).name;
            String base = current.split("_")[0];
            if(!replace_map.containsKey(base)){
                replace_map.put(base, current);
            }
        }
        ListIterator<Instruction> iterator = this.ins.listIterator(start);
        while(iterator.hasNext()){
            Instruction in = iterator.next();
            in.id += ins.size();
            // Update all the line number
            for(Instruction.Operand operand : in.operands){
                switch(operand.type){
                    case ARR_ADDRESS:
                        operand.address += ins.size();
                        break;
                    case INST:
                    case JUMP_ADDRESS:
                    case FUNC_RETURN_PARAM:
                        operand.line += ins.size();
                        break;
                    // Update the usage in the while
                    case VARIABLE:
                        // do not replace the left side of assignment
                        if(in.operator == InstructionType.PHI || in.operator == InstructionType.MOVE && in.operands.indexOf(operand) == 1) break;
                        String base = operand.name.split("_")[0];
                        if(replace_map.containsKey(base)){
                            operand.name = replace_map.get(base);
                        }
                }
            }
        }
        // change id of phi in
        int i = start;
        for(Instruction in : ins){
            in.id = i++;
        }
        this.ins.addAll(start, ins);
        count += ins.size();
    }

    public Instruction getLastInstruction(){
        if(this.ins.size() == 0){
            return null;
        }
        return this.ins.get(this.ins.size() - 1);
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0, len = ins.size(); i < len; i++){
            builder.append(i + ": ");
            builder.append(ins.get(i));
            builder.append("\n");
        }
        return builder.toString();
    }

    public static String toStringOfRange(List<Instruction> ins, int start, int end){
        if(start < 0 || end >= ins.size() || start > end)
            return null;
        StringBuilder builder = new StringBuilder();
        for(int i = start, len = end + 1; i < len; i++){
            Instruction in = ins.get(i);
            builder.append(in.id + ": ");
            builder.append(in);
            builder.append("\n");
        }
        return builder.toString();
    }

    public Object clone () throws CloneNotSupportedException {
        IR newObject = new IR();
        newObject.ins = new ArrayList<Instruction>(this.ins);
        return newObject;
    }

}