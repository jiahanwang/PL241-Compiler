package edu.uci.cs241.ir;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanplusplus on 2/27/15.
 */
public class IR {

    public List<Instruction> ins;

    public static int count;
    static {
        count = 0;
    }

    public int pc;


    public IR () {
        this.ins = new LinkedList<Instruction>();
        this.pc = 0;
    }

    public int addInstruction(Instruction in){
        in.id = count ++;
        this.ins.add(in);
        return count - 1;
    }

}
