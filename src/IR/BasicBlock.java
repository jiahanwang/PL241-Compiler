package IR;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivanleung on 2/11/15.
 */
public class BasicBlock {

    public int id;
    public static int count;

    public BasicBlock() {
        id=count++;
    }

    public BasicBlock myDom;    // the block that dominates this one.
    public BasicBlock left;     // this default next step / fall through
    public BasicBlock right;    // this will be null if no branching
    public boolean hasBranching;

    public BasicBlock exit;     //for use of branching / joining

    public ArrayList<IRInstruction> instructions = new ArrayList<IRInstruction>();

//    public String instruction;

    public void append(IRInstruction b) {
        instructions.add(b);
//        instruction+="\n"+b;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(IRInstruction i : instructions) {
            sb.append(i.toString()+"\n");
        }
        return sb.toString();
    }


}
