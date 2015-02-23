package frontend;

import IR.BasicBlock;

import java.io.BufferedWriter;
import java.io.PrintWriter;

/**
 * Created by Ivan on 2/9/2015.
 */
public class ParserTest {

    static Parser p;

    public static void main(String[] args) {
        try {
            for(int i = 0; i <= 0; i++) {
                buildCFG("test0" + String.format("%02d", i));
                buildDom("test0" + String.format("%02d", i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean[] explored = new boolean[1000];

    public static void buildCFG(String s) throws Exception {
        PrintWriter pw = new PrintWriter("viz/"+s+".cfg.dot");
        pw.println("digraph "+s+" {");
        pw.println("node [shape=box]");
        p = new Parser("tests/"+s+".txt");
        BasicBlock b = p.computation();
        DFS_buildCFG(b, pw);
        pw.println("}");
        pw.close();

        p.print();
    }

    public static void DFS_buildCFG(BasicBlock b, PrintWriter pw) {
        if(explored[b.id]) {
            return;
        }
        if (b != null) {
            pw.println(b.id + "[label=\"" + b.instructions.toString() + "\"]");
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

    public static void buildDom(String s) throws Exception {
        PrintWriter pw = new PrintWriter("viz/"+s+".dom.dot");
        pw.println("digraph "+s+" {");
        pw.println("node [shape=box, rankdir=BT]");
        p = new Parser("tests/"+s+".txt");
        BasicBlock b = p.computation();
        DFS_buildDom(b, pw);
        pw.println("}");
        pw.close();
    }

    public static void DFS_buildDom(BasicBlock b, PrintWriter pw) {
        if(explored[b.id]) {
            return;
        }
        if (b != null) {
            pw.println(b.id + "[label=\"" + b.instructions.toString() + "\"]");
            if(b.myDom != null)
                pw.println(b.id + " -> " + b.myDom.id);
            explored[b.id] = true;
        }
        if (b.left != null) {
            DFS_buildDom(b.left, pw);
        }
        if (b.right != null) {
            DFS_buildDom(b.right, pw);
        }
    }

}
