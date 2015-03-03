package edu.uci.cs241.frontend;

import edu.uci.cs241.ir.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 2/9/2015.
 */
public class ParserTest {

    public static void main(String[] args) {
        try {
            Parser parser;
            for(int i = 1; i <= 31; i++) {
                PrintWriter pw = new PrintWriter("viz/test0"+String.format("%02d", i)+".dot");
                pw.println("digraph test0"+String.format("%02d", i)+" {");
                pw.println("node [shape=box]");
                parser = new Parser("tests/test0"+String.format("%02d", i)+".txt");
                BasicBlock block = parser.computation();
                IR ir = parser.getIR();
                // print out IR
                System.out.print("test0" + String.format("%02d", i) + ".txt" + "\n======================\n");
                System.out.print(ir);
                System.out.print("======================\n\n");
                // print out basic blocks
                boolean[] explored = new boolean[1000000];
                DFS_buildCFG(block, ir, pw, explored);
                // print out all the functions
                List<Function> funcs = parser.getFunctions();
                for(Function func : funcs){
                    if(func.entry != null)
                        DFS_buildCFG(func.entry, ir, pw, explored);
                }
                pw.println("}");
                pw.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void DFS_buildCFG(BasicBlock b, IR ir, PrintWriter pw, boolean[] explored) {
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
            DFS_buildCFG(b.left, ir, pw, explored);
        }
        if (b.right != null) {
            pw.println(b.id + " -> " + b.right.id);
            DFS_buildCFG(b.right, ir, pw, explored);
        }
    }
}
