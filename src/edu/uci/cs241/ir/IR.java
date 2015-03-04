package edu.uci.cs241.ir;

import java.util.ArrayList;

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

    public String toString(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0, len = ins.size(); i < len; i++){
            builder.append(i + ": ");
            builder.append(ins.get(i));
            builder.append("\n");
        }
        return builder.toString();
    }

    public String toStringOfRange(int start, int end){
        if(start < 0 || end >= ins.size() || start > end)
            return null;
        StringBuilder builder = new StringBuilder();
        for(int i = start, len = end + 1; i < len; i++){
            builder.append(i + ": ");
            builder.append(ins.get(i));
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
