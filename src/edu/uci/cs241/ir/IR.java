package edu.uci.cs241.ir;

import edu.uci.cs241.ir.types.OperandType;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    public void insertInstructions(List<Instruction> ins, int start){
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
                }
            }
        }
        this.ins.addAll(start, ins);
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
