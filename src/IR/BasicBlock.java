package IR;

import java.util.List;

/**
 * Created by ivanleung on 2/11/15.
 */
public class BasicBlock {

    public BasicBlock left;     // this default next step / fall through
    public BasicBlock right;    // this will be null if no branching

    public BasicBlock exit;     //for use of branching / joining

    public List<String> instructions;

    public String instruction;

    public void add(String b) {
        instructions.add(b);
    }

}
