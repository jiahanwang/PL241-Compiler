package IR;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ivanleung on 2/17/15.
 */
public class DefUseChain {

    HashMap<String, int[]> du = new HashMap<String, int[]>();

    public void addWrite(String id, int line) {
        if(du.containsKey(id)) {
            du.get(id)[0] = line;
        } else {
            du.put(id, new int[] {line, -1});
        }
    }

    public void addRead(String id, int line) {
        if(du.containsKey(id)) {
            du.get(id)[1] = line;
        } else {
            du.put(id, new int[] {-1, line});
        }
    }
}
