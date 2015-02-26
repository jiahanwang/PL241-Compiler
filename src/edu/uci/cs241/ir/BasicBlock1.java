package edu.uci.cs241.ir;

import java.util.List;

/**
 * Created by ivanleung on 2/11/15.
 */
public class BasicBlock1 {

    public int id;
    public static int count;

    public BasicBlock1() {
        id=count++;
    }

    public BasicBlock1 left;     // this default next step / fall through
    public BasicBlock1 right;    // this will be null if no branching
    public boolean hasBranching;

    public BasicBlock1 exit;     //for use of branching / joining

    public List<String> instructions;

    public String instruction;

    public void append(String b) {
        //instructions.add(b);
        instruction+="\n"+b;
    }



}
