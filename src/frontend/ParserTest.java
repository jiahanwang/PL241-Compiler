package frontend;

import IR.BasicBlock;

/**
 * Created by Ivan on 2/9/2015.
 */
public class ParserTest {

    public static void main(String[] args) {
        try {
            Parser p;
            for(int i = 11; i <= 11; i++) {
                System.out.println("digraph test0"+String.format("%02d", i)+" {");
                System.out.println("node [shape=box]");
                p = new Parser("tests/test0"+String.format("%02d", i)+".txt");
                BasicBlock b = p.computation();
                DFS_buildCFG(b);
                System.out.println("}");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static boolean[] explored = new boolean[1000];

    public static void DFS_buildCFG(BasicBlock b) {
        if(explored[b.id]) {
            return;
        }
        if (b != null) {
            System.out.println(b.id + "[label=\"" + b.instruction + "\"]");
            explored[b.id] = true;
        }
        if (b.left != null) {
            System.out.println(b.id + " -> " + b.left.id);
            DFS_buildCFG(b.left);
        }
        if (b.right != null) {
            System.out.println(b.id + " -> " + b.right.id);
            DFS_buildCFG(b.right);
        }
    }
}
