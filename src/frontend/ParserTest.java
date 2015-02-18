package frontend;

import IR.BasicBlock;

import java.io.BufferedWriter;
import java.io.PrintWriter;

/**
 * Created by Ivan on 2/9/2015.
 */
public class ParserTest {

    public static void main(String[] args) {
        try {
            Parser p;
            for(int i = 1; i <= 31; i++) {

                PrintWriter pw = new PrintWriter("viz/test0"+String.format("%02d", i)+".dot");

                pw.println("digraph test0"+String.format("%02d", i)+" {");
                pw.println("node [shape=box]");
                p = new Parser("tests/test0"+String.format("%02d", i)+".txt");
                BasicBlock b = p.computation();
                DFS_buildCFG(b, pw);
                pw.println("}");

                pw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean[] explored = new boolean[1000];

    public static void DFS_buildCFG(BasicBlock b, PrintWriter pw) {
        if(explored[b.id]) {
            return;
        }
        if (b != null) {
            pw.println(b.id + "[label=\"" + b.instruction + "\"]");
            explored[b.id] = true;
        }
        if (b.left != null) {
            pw.println(b.id + " -> " + b.left.id);
            DFS_buildCFG(b.left, pw);
        }
        if (b.right != null) {
            pw.println(b.id + " -> " + b.right.id);
            DFS_buildCFG(b.right, pw);
        }
    }
}
