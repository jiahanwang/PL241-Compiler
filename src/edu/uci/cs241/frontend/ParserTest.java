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
                /** Get all functions **/
                Function main = parser.computation();
                List<Function> funcs = new ArrayList<Function>();
                funcs.add(main);
                funcs.addAll(main.getFunctions());
                boolean[] explored = new boolean[1000000];
                /** Print out all functions **/
                System.out.print("test0" + String.format("%02d", i) + ".txt" + "\n======================\n");
                for(Function func : funcs){
                    if(func.predefined) continue;
                    // print out ir
                    System.out.print(func.name + ":\n");
                    System.out.print(func.ir);
                    System.out.print("-----------------------\n");
                    // print out CFG
                    DFS_buildCFG(func.entry, func.ir, pw, explored);
                }
                System.out.print("======================\n\n\n");
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
