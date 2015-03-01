package edu.uci.cs241.frontend;

import edu.uci.cs241.ir.BasicBlock;
import edu.uci.cs241.ir.BasicBlock1;
import edu.uci.cs241.ir.IR;
import edu.uci.cs241.ir.Instruction;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by Ivan on 2/9/2015.
 */
public class ParserTest {

    public static void main(String[] args) {
        try {
            Parser parser;
            for(int i = 0; i <= 0; i++) {
                PrintWriter pw = new PrintWriter("viz/test0"+String.format("%02d", i)+".dot");
                pw.println("digraph test0"+String.format("%02d", i)+" {");
                pw.println("node [shape=box]");
                parser = new Parser("tests/test0"+String.format("%02d", i)+".txt");
                BasicBlock block = parser.computation();
                IR ir = parser.getIR();
                DFS_buildCFG(block, ir, pw);
                // print out all the functions
                pw.println("}");
                pw.close();
                System.out.print(ir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean[] explored = new boolean[1000];

    public static void DFS_buildCFG(BasicBlock b, IR ir, PrintWriter pw) {
        if(explored[b.id]) {
            return;
        }
        if (b != null) {
            String output = b.start_line == Integer.MIN_VALUE ? b.name : ir.toStringOfRange(b.start_line, b.end_line);
            pw.println(b.id + "[label=\"" + output + "\"]");
            explored[b.id] = true;
        }
        if (b.left != null) {
            pw.println(b.id + " -> " + b.left.id);
            DFS_buildCFG(b.left, ir, pw);
        }
        if (b.right != null) {
            pw.println(b.id + " -> " + b.right.id);
            DFS_buildCFG(b.right, ir, pw);
        }
    }
}
