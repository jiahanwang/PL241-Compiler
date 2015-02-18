package IR;

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

    public BasicBlock left;     // this default next step / fall through
    public BasicBlock right;    // this will be null if no branching
    public boolean hasBranching;

    public BasicBlock exit;     //for use of branching / joining

    public List<String> instructions;

    public String instruction;

    public void append(String b) {
        //instructions.add(b);
        instruction+="\n"+b;
    }



}
